package com.melikenurozun.recipe_app.di

import com.melikenurozun.recipe_app.data.repository.AuthRepositoryImpl
import com.melikenurozun.recipe_app.data.repository.RecipeRepositoryImpl
import com.melikenurozun.recipe_app.domain.repository.AuthRepository
import com.melikenurozun.recipe_app.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        recipeRepositoryImpl: RecipeRepositoryImpl
    ): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: com.melikenurozun.recipe_app.data.repository.ProfileRepositoryImpl
    ): com.melikenurozun.recipe_app.domain.repository.ProfileRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: com.melikenurozun.recipe_app.data.repository.CommentRepositoryImpl
    ): com.melikenurozun.recipe_app.domain.repository.CommentRepository
    @Binds
    @Singleton
    abstract fun bindShoppingRepository(
        shoppingRepositoryImpl: com.melikenurozun.recipe_app.data.repository.ShoppingRepositoryImpl
    ): com.melikenurozun.recipe_app.domain.repository.ShoppingRepository
}
