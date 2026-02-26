package com.melikenurozun.recipe_app.data.repository

import com.melikenurozun.recipe_app.data.local.FavoriteDao
import com.melikenurozun.recipe_app.data.remote.RecipeDto
import com.melikenurozun.recipe_app.data.remote.dto.FavoriteDto
import com.melikenurozun.recipe_app.data.remote.dto.FavoritePayload
import com.melikenurozun.recipe_app.data.remote.dto.RatingDto
import com.melikenurozun.recipe_app.data.remote.dto.RatingValueDto
import com.melikenurozun.recipe_app.data.remote.toDomain
import com.melikenurozun.recipe_app.data.remote.toDto
import com.melikenurozun.recipe_app.domain.model.Recipe
import com.melikenurozun.recipe_app.domain.repository.RecipeRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of [RecipeRepository] using Supabase as the remote data source.
 * Handles data operations for recipes, favorites, and ratings.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RecipeRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val favoriteDao: FavoriteDao
) : RecipeRepository {

    private val _favoritesRefresh = MutableSharedFlow<Unit>(replay = 1)
    
    init {
        _favoritesRefresh.tryEmit(Unit)
    }

    override suspend fun getRecipes(): List<Recipe> {
        return try {
            val recipes = supabase.from("recipes")
                .select(columns = Columns.ALL)
                .decodeList<RecipeDto>()
            
            val userIds = recipes.map { it.userId }.distinct()
            val userMap = if (userIds.isNotEmpty()) {
                 supabase.from("profiles")
                     .select(columns = Columns.list("id", "username")) {
                         filter { isIn("id", userIds) }
                     }
                     .decodeList<ProfileSubsetDto>()
                     .associate { it.id to it.username }
            } else {
                 emptyMap()
            }
            
            val favoriteIds = getFavoriteIds()

            recipes.map { 
                it.toDomain().copy(
                    username = userMap[it.userId],
                    isFavorite = favoriteIds.contains(it.id ?: "")
                ) 
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getRecipeById(id: String): Recipe? {
        return try {
            val recipe = supabase.from("recipes")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingleOrNull<RecipeDto>()
            recipe?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createRecipe(recipe: Recipe) {
        supabase.from("recipes").insert(recipe.toDto())
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        supabase.from("recipes").update(recipe.toDto()) {
            filter {
                eq("id", recipe.id)
            }
        }
    }

    override suspend fun deleteRecipe(id: String) {
        supabase.from("recipes").delete {
            filter {
                eq("id", id)
            }
        }
    }

    override suspend fun searchRecipes(query: String): List<Recipe> {
        return try {
            // Try Fuzzy Search RPC (Remote Procedure Call)
            // This requires 'fuzzy_search_recipes' function in Supabase
            val recipes = supabase.postgrest.rpc(
                "fuzzy_search_recipes",
                kotlinx.serialization.json.buildJsonObject {
                    put("search_query", kotlinx.serialization.json.JsonPrimitive(query))
                }
            ).decodeList<RecipeDto>()
            recipes.map { it.toDomain() }
        } catch (e: Exception) {
            // Fallback to standard ILIKE search if RPC fails
            try {
                val recipes = supabase.from("recipes")
                    .select(columns = Columns.ALL) {
                        filter {
                            or {
                                ilike("title", "%$query%")
                                ilike("ingredients", "%$query%")
                            }
                        }
                    }.decodeList<RecipeDto>()
                recipes.map { it.toDomain() }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }

    override fun getFavorites(): Flow<List<Recipe>> {
        return _favoritesRefresh.flatMapLatest {
            flow {
                try {
                    val userId = supabase.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        val favorites = supabase.from("favorites")
                            .select(columns = Columns.list("recipes(*)")) {
                                filter {
                                    eq("user_id", userId)
                                }
                            }
                            .decodeList<FavoriteDto>()
                        emit(favorites.mapNotNull { it.recipes?.toDomain() })
                    } else {
                        emit(emptyList())
                    }
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }
    }

    override suspend fun isFavorite(id: String): Boolean {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return false
            val count = supabase.from("favorites")
                .select {
                    count(Count.EXACT)
                    filter {
                        eq("user_id", userId)
                        eq("recipe_id", id)
                    }
                }.countOrNull()
            (count ?: 0) > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun toggleFavorite(recipe: Recipe) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        if (isFavorite(recipe.id)) {
            supabase.from("favorites").delete {
                filter {
                    eq("user_id", userId)
                    eq("recipe_id", recipe.id)
                }
            }
        } else {
            supabase.from("favorites").insert(
                FavoritePayload(user_id = userId, recipe_id = recipe.id)
            )
        }
        _favoritesRefresh.emit(Unit)
    }

    override suspend fun rateRecipe(recipeId: String, rating: Int): Result<Unit> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")
            val ratingDto = RatingDto(
                userId = userId,
                recipeId = recipeId,
                rating = rating
            )
            supabase.from("ratings").upsert(ratingDto) {
                 onConflict = "user_id, recipe_id"
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserRating(recipeId: String): Int {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return 0
            val rating = supabase.from("ratings")
                .select(columns = Columns.list("rating")) {
                    filter {
                        eq("user_id", userId)
                        eq("recipe_id", recipeId)
                    }
                }.decodeSingleOrNull<RatingValueDto>()
            rating?.rating ?: 0
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun uploadRecipeImage(imageBytes: ByteArray, fileExtension: String): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.$fileExtension"
            val bucket = supabase.storage.from("recipes")
            bucket.upload(fileName, imageBytes)
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    private suspend fun getFavoriteIds(): Set<String> {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return emptySet()
            supabase.from("favorites")
                .select(columns = Columns.list("recipe_id")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<FavoritePayload>() // Reusing payload or dict? Payload has recipe_id.
                .map { it.recipe_id }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}

@kotlinx.serialization.Serializable
data class ProfileSubsetDto(
    val id: String,
    val username: String? = null
)
