package com.melikenurozun.recipe_app.presentation.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.melikenurozun.recipe_app.presentation.components.RecipeCard

@Composable
fun FavoritesScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            Text("No favorites yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(favorites) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onNavigateToDetail(recipe.id) }
                    )
                }
            }
        }
    }
}
