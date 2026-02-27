package com.melikenurozun.recipe_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.melikenurozun.recipe_app.domain.model.Recipe
import com.melikenurozun.recipe_app.ui.theme.ChampagneGold
import com.melikenurozun.recipe_app.ui.theme.WarmGray

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onToggleFavorite: (() -> Unit)? = null,
    currentUserId: String? = null,
    followedUserIds: Set<String> = emptySet(),
    onToggleFollow: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp) // Fixed height to ensure uniform magazine look
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Image Section (70% height)
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth()
            ) {
                if (recipe.image_url != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(recipe.image_url)
                            .crossfade(true)
                            .size(800) // Load higher quality
                            .build(),
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when(recipe.category?.lowercase()) {
                                "breakfast" -> "🍳"
                                "dessert" -> "🍰"
                                "dinner" -> "🍽️"
                                "lunch" -> "🥪"
                                "snack" -> "🥨"
                                else -> "🥘"
                            },
                            fontSize = 64.sp * 1.5f
                        )
                    }
                }

                // Gradient Overlay for text protection (Subtle)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                                startY = 300f
                            )
                        )
                )

                // Category Chip (Top Left - Floating)
                if (!recipe.category.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = recipe.category.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp * 1.5f,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Favorite Button (Top Right - Floating)
                if (onToggleFavorite != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .clickable { onToggleFavorite() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (recipe.isFavorite) Color(0xFFD66A6A) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Time Badge (Bottom Right)
                if (recipe.timeMinutes > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${recipe.timeMinutes} min",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.5f),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Content Section (30% height)
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Title
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp * 1.5f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp * 1.5f
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Metadata Row (Rating + Author)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Rating
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (recipe.average_rating > 0) String.format("%.1f", recipe.average_rating) else "New",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.5f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (recipe.rating_count > 0) {
                         Text(
                            text = " (${recipe.rating_count})",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.5f),
                            color = WarmGray
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Separator dot
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(WarmGray, CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))

                    // Author
                    val isOwnRecipe = currentUserId != null && currentUserId == recipe.user_id
                    val isFollowing = followedUserIds.contains(recipe.user_id)
                    
                    Text(
                        text = recipe.username ?: "Unknown",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.5f),
                        color = WarmGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (!isOwnRecipe && currentUserId != null && recipe.user_id.isNotEmpty() && onToggleFollow != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onToggleFollow(recipe.user_id) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(26.dp),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Following" else "Follow",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.2f, // slightly smaller scalar for card fit
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
