package com.melikenurozun.recipe_app.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.melikenurozun.recipe_app.presentation.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Exclude Ingredients", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                var tempIngredient by remember { mutableStateOf("") }
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tempIngredient,
                        onValueChange = { tempIngredient = it },
                        label = { Text("Ingredient (e.g. Onion)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { 
                        if(tempIngredient.isNotBlank()) {
                            viewModel.addExcludedIngredient(tempIngredient)
                            tempIngredient = ""
                        }
                    }) {
                        Text("Add")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Active Exclusions:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (uiState.excludedIngredients.isEmpty()) {
                    Text("None", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                     // Using LazyRow for safety, FlowRow would be better but requires uncertain import
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.excludedIngredients) { ingredient ->
                            InputChip(
                                selected = true,
                                onClick = { viewModel.removeExcludedIngredient(ingredient) },
                                label = { Text(ingredient) },
                                trailingIcon = { Icon(Icons.Default.Close, null) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChanged(it) },
                placeholder = { Text("Search Recipes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { showFilterSheet = true }) {
                Icon(Icons.AutoMirrored.Filled.List, "Filter") // List or FilterList icon
            }
        }

        // Active Exclusions Display on Main Screen (Quick view)
        if (uiState.excludedIngredients.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
             androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.excludedIngredients) { ingredient ->
                    InputChip(
                        selected = true,
                        onClick = { viewModel.removeExcludedIngredient(ingredient) },
                        label = { Text(ingredient) },
                        trailingIcon = { Icon(Icons.Default.Close, null) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(
                    items = uiState.results,
                    key = { it.id }
                ) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onNavigateToDetail(recipe.id) }
                    )
                }
            }
        }
    }
}
