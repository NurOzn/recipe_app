package com.melikenurozun.recipe_app.presentation.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.melikenurozun.recipe_app.ui.theme.ChampagneGold
import com.melikenurozun.recipe_app.ui.theme.WarmGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToCooking: (String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                     android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }
    
    var showGuestDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showGuestDialog) {
        com.melikenurozun.recipe_app.presentation.components.GuestLoginDialog(
            onDismissRequest = { showGuestDialog = false },
            onLoginClick = null
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Recipe?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = { 
                Button(
                    onClick = { 
                        showDeleteConfirm = false
                        viewModel.onEvent(DetailEvent.DeleteRecipe) 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { 
                    Text("Delete") 
                } 
            },
            dismissButton = { 
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } 
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // Removed FAB from Scaffold bottom right, moving to Image
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.recipe != null) {
            val recipe = uiState.recipe!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Immersive Hero Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp) // ~40% height
                ) {
                    if (recipe.image_url != null) {
                        AsyncImage(
                            model = recipe.image_url,
                            contentDescription = recipe.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                         Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    // Gradient for status bar visibility
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                                )
                            )
                    )

                    // Back Button
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    // Share Button (Top Right)
                    IconButton(
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${recipe.title}\n\n${recipe.ingredients}\n\n${recipe.instructions}")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .padding(top = 48.dp, end = 16.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }

                    // Floating Favorite Button (On Image, Bottom Right)
                    FloatingActionButton(
                        onClick = { 
                            if (uiState.isGuest) showGuestDialog = true 
                            else viewModel.onEvent(DetailEvent.ToggleFavorite) 
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .offset(y = 24.dp), // Half overlapping content
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = if (uiState.isFavorite) Color(0xFFD66A6A) else Color.Gray,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Content Section
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    
                    // Title
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = MaterialTheme.typography.headlineLarge.fontSize * 1.5f, lineHeight = MaterialTheme.typography.headlineLarge.lineHeight * 1.5f),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Meta Row (Rating + Author + Time)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         // Rating
                        Icon(Icons.Default.Star, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (recipe.average_rating > 0) String.format("%.1f", recipe.average_rating) else "New",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.5f),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (recipe.rating_count > 0) {
                            Text(
                                text = " (${recipe.rating_count})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f),
                                color = WarmGray
                            )
                        }
                        
                         Spacer(modifier = Modifier.width(16.dp))
                         Box(modifier = Modifier.size(4.dp).background(WarmGray, CircleShape))
                         Spacer(modifier = Modifier.width(16.dp))
                        
                        // Author
                        if (recipe.username != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "By ${recipe.username}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = MaterialTheme.typography.labelLarge.fontSize * 1.5f), // Sans
                                    color = WarmGray,
                                    modifier = Modifier.clickable { 
                                        if (uiState.isGuest) showGuestDialog = true 
                                        else onNavigateToProfile(recipe.user_id) 
                                    }
                                )
                                
                                if (!uiState.isOwnRecipe) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = { 
                                            if (uiState.isGuest) showGuestDialog = true 
                                            else viewModel.onEvent(DetailEvent.ToggleFollow) 
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                        modifier = Modifier.height(32.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (uiState.isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                            contentColor = if (uiState.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color.White
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                                    ) {
                                        Text(
                                            text = if (uiState.isFollowing) "Following" else "Follow",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.5f,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Time (if exists)
                    if (recipe.timeMinutes > 0) {
                        Text(
                             text = "${recipe.timeMinutes} mins cooking time",
                             style = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f),
                             color = WarmGray
                        )
                    }

                    // Owner Actions
                     if (uiState.isOwnRecipe) {
                         Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            OutlinedButton(
                                onClick = { onNavigateToEdit(recipe.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(32.dp))

                    // Ingredients
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize * 1.5f),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    recipe.ingredients.lineSequence().forEach { ingredient ->
                        if (ingredient.isNotBlank()) {
                             Row(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(vertical = 6.dp),
                                 verticalAlignment = Alignment.Top
                             ) {
                                 Text(text = "•", style = MaterialTheme.typography.headlineSmall.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.5f), color = ChampagneGold)
                                 Spacer(modifier = Modifier.width(12.dp))
                                 Text(
                                     text = ingredient.trim(), 
                                     style = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.5f),
                                     modifier = Modifier.weight(1f),
                                     color = MaterialTheme.colorScheme.onBackground
                                 )
                                 // Add to Shopping List Button (Subtle)
                                 IconButton(
                                     onClick = { 
                                         if (uiState.isGuest) showGuestDialog = true 
                                         else viewModel.onEvent(DetailEvent.AddToShoppingList(ingredient.trim())) 
                                     },
                                     modifier = Modifier.size(24.dp)
                                 ) {
                                     Icon(
                                         imageVector = Icons.Default.Add, 
                                         contentDescription = "Add to Shopping List",
                                         tint = MaterialTheme.colorScheme.primary.copy(alpha=0.6f),
                                         modifier = Modifier.size(18.dp)
                                     )
                                 }
                             }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onNavigateToCooking(recipe.id) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), // Olive Green
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Cooking Mode", style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.5f))
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Instructions
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize * 1.5f),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = recipe.instructions,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp * 1.5f, fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.5f),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Rating Section
                    Text(
                        text = "Rate this recipe",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.5f)
                    )
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                         repeat(5) { index ->
                            val starIndex = index + 1
                             Icon(
                                imageVector = if (starIndex <= uiState.userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate $starIndex",
                                modifier = Modifier
                                    .clickable { 
                                        if (uiState.isGuest) showGuestDialog = true 
                                        else viewModel.onEvent(DetailEvent.RateRecipe(starIndex)) 
                                    }
                                    .padding(8.dp)
                                    .size(32.dp),
                                tint = ChampagneGold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Comments Section (Collapsible/Preview style suggested, but keeping list for now with better header)
                     Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize * 1.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Add Review Section
                    var commentContent by remember { mutableStateOf("") }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = commentContent,
                                onValueChange = { commentContent = it },
                                label = { Text("Write a review...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { 
                                    if (uiState.isGuest) showGuestDialog = true else {
                                        viewModel.onEvent(DetailEvent.AddComment(commentContent))
                                        commentContent = ""
                                    }
                                },
                                enabled = !uiState.isCommentLoading && commentContent.isNotBlank(),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                if (uiState.isCommentLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                } else {
                                    Text("Post Review")
                                }
                            }
                        }
                    }

                    // Reviews List
                    if (uiState.comments.isEmpty()) {
                        Text("No reviews yet. Be the first!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        uiState.comments.forEach { comment ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = comment.username ?: "Unknown User",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.5f),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = comment.created_at?.take(10) ?: "",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = comment.content, 
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize * 1.5f),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        
                                        // Like Button
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = if (comment.is_liked_by_me) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Like Comment",
                                                tint = if (comment.is_liked_by_me) Color(0xFFD66A6A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clickable { 
                                                        if (uiState.isGuest) showGuestDialog = true 
                                                        else viewModel.onEvent(DetailEvent.ToggleCommentLike(comment.id)) 
                                                    }
                                            )
                                            if (comment.like_count > 0) {
                                                Text(
                                                    text = comment.like_count.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp)) // Bottom padding
                }
            }
        }
    }
}
