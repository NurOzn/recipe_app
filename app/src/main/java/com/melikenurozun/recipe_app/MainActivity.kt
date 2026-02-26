package com.melikenurozun.recipe_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.melikenurozun.recipe_app.data.repository.SettingsRepository
import com.melikenurozun.recipe_app.presentation.navigation.NavGraph
import com.melikenurozun.recipe_app.presentation.navigation.Screen
import com.melikenurozun.recipe_app.ui.theme.Recipe_appTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var authRepository: com.melikenurozun.recipe_app.domain.repository.AuthRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by settingsRepository.isDarkMode.collectAsState(initial = false)
            val isGuestMode by settingsRepository.isGuestMode.collectAsState(initial = false)
            val currentUser by authRepository.currentUser.collectAsState(initial = null)
            
            Recipe_appTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDest = navBackStackEntry?.destination
                
                
                // Automatic navigation: Go to Home if logged in and currently on Auth screen
                // Guest users should be able to access Auth screen to register/login
                LaunchedEffect(currentUser, currentDest) {
                    if (currentUser != null && currentDest?.hasRoute(Screen.Auth::class) == true) {
                        navController.navigate(Screen.Home()) {
                            popUpTo(Screen.Auth) { inclusive = true }
                        }
                    }
                }
                
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                
                val isAuth = currentDest?.hierarchy?.any { it.hasRoute(Screen.Auth::class) } == true

                ModalNavigationDrawer(
                    gesturesEnabled = !isAuth,
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text(
                                text = if (isGuestMode) "Guest" else currentUser?.username ?: currentUser?.email ?: "Recipe App",
                                modifier = Modifier.padding(16.dp),
                                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            NavigationDrawerItem(
                                label = { Text("Home", style = MaterialTheme.typography.titleMedium) },
                                selected = false,
                                icon = { Icon(Icons.Default.Home, null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Home())
                                }
                            )

                            NavigationDrawerItem(
                                label = { Text("Shopping List", style = MaterialTheme.typography.titleMedium) },
                                selected = false,
                                icon = { Icon(Icons.Default.ShoppingCart, null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (isGuestMode) {
                                        navController.navigate(Screen.Profile()) // Redirect to Profile to prompt login
                                    } else {
                                        navController.navigate(Screen.ShoppingList)
                                    }
                                }
                            )
                            
                            // Expandable Recipes Menu
                            var isRecipesExpanded by remember { androidx.compose.runtime.mutableStateOf(false) }
                            NavigationDrawerItem(
                                label = { Text("Recipes", style = MaterialTheme.typography.titleMedium) },
                                selected = false,
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.RestaurantMenu, null) },
                                onClick = { isRecipesExpanded = !isRecipesExpanded },
                                badge = { 
                                    if (isRecipesExpanded) Icon(androidx.compose.material.icons.Icons.Default.ExpandLess, null) 
                                    else Icon(androidx.compose.material.icons.Icons.Default.ExpandMore, null) 
                                }
                            )
                            
                            if (isRecipesExpanded) {
                                val categories = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack")
                                categories.forEach { category ->
                                    NavigationDrawerItem(
                                        label = { Text(category, style = MaterialTheme.typography.bodyLarge) },
                                        selected = false,
                                        modifier = Modifier.padding(start = 32.dp),
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate(Screen.Home(category = category))
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            NavigationDrawerItem(
                                label = { Text("Profile", style = MaterialTheme.typography.titleMedium) },
                                selected = false,
                                icon = { Icon(androidx.compose.material.icons.Icons.Default.Person, null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    // Always navigate to Profile. ProfileScreen handles Guest view.
                                    navController.navigate(Screen.Profile())
                                }
                            )
                             NavigationDrawerItem(
                                label = { Text("Favorites", style = MaterialTheme.typography.titleMedium) },
                                selected = false,
                                icon = { Icon(Icons.Default.FavoriteBorder, null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (isGuestMode) {
                                        navController.navigate(Screen.Profile()) // Redirect to Profile to prompt login
                                    } else {
                                        navController.navigate(Screen.Favorites)
                                    }
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            NavigationDrawerItem(
                                label = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                                selected = false,
                                icon = { Icon(Icons.Default.Settings, null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(Screen.Settings)
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (!isAuth) {
                                TopAppBar(
                                    title = { 
                                        Text(
                                            "Recipe App", 
                                            modifier = Modifier.clickable { navController.navigate(Screen.Home()) } 
                                        ) 
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = { navController.navigate(Screen.Search) }) {
                                            Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search")
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                            NavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }
}