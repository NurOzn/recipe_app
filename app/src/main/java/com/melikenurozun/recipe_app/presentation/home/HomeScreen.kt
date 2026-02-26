package com.melikenurozun.recipe_app.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.melikenurozun.recipe_app.presentation.components.RecipeCard
import com.melikenurozun.recipe_app.presentation.home.HomeViewModel
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    initialCategory: String? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(initialCategory) {
        if (initialCategory != null && uiState.selectedCategory != initialCategory) {
            viewModel.onCategorySelected(initialCategory)
        }
    }
    
    var showGuestDialog by remember { mutableStateOf(false) }

    if (showGuestDialog) {
        com.melikenurozun.recipe_app.presentation.components.GuestLoginDialog(
            onDismissRequest = { showGuestDialog = false },
            onLoginClick = { 
                showGuestDialog = false
                onNavigateToAuth() 
            },
            text = "I thought you were just here to have a look?"
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.isGuest) {
                        showGuestDialog = true
                    } else {
                        onNavigateToAdd()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Feed Tabs (stays fixed at top)
            PrimaryTabRow(
                selectedTabIndex = if (uiState.selectedFilter == FeedFilter.ALL) 0 else 1,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                Tab(
                    selected = uiState.selectedFilter == FeedFilter.ALL,
                    onClick = { viewModel.onFilterSelected(FeedFilter.ALL) },
                    text = { Text("All Recipes", style = MaterialTheme.typography.titleMedium) }
                )
                Tab(
                    selected = uiState.selectedFilter == FeedFilter.FOLLOWING,
                    onClick = { 
                        if (uiState.isGuest) {
                            showGuestDialog = true
                        } else {
                            viewModel.onFilterSelected(FeedFilter.FOLLOWING)
                        }
                    },
                    text = { Text("Following", style = MaterialTheme.typography.titleMedium) }
                )
            }

            // Categories FilterChips
            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                 items(uiState.categories) { category ->
                     FilterChip(
                         selected = uiState.selectedCategory == category,
                         onClick = { viewModel.onCategorySelected(category) },
                         label = { Text(category) },
                         colors = FilterChipDefaults.filterChipColors(
                             selectedContainerColor = MaterialTheme.colorScheme.secondary,
                             selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                         )
                     )
                 }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Header as first item - scrolls with content
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 16.dp)
                        ) {
                            val greeting = remember {
                                val hour = java.time.LocalTime.now().hour
                                when (hour) {
                                    in 5..11 -> "Good morning,"
                                    in 12..17 -> "Good afternoon,"
                                    else -> "Good evening,"
                                }
                            }
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "What are we\ncooking today?",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    
                    items(
                        items = uiState.recipes,
                        key = { it.id }
                    ) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onNavigateToDetail(recipe.id) },
                            onToggleFavorite = {
                                if (uiState.isGuest) {
                                    showGuestDialog = true
                                } else {
                                    viewModel.toggleFavorite(recipe.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
