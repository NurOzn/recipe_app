package com.melikenurozun.recipe_app.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.melikenurozun.recipe_app.presentation.components.RecipeCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun ProfileScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onNavigateToAuth()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.isMe) "My Profile" else uiState.username,
                style = MaterialTheme.typography.headlineMedium
            )
            
            if (uiState.isMe) {
                IconButton(onClick = { viewModel.onEvent(ProfileEvent.SignOut) }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sign Out")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val avatarLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    viewModel.onEvent(ProfileEvent.AvatarSelected(uri))
                }
            }

            androidx.compose.foundation.shape.CircleShape.let { shape ->
                if (uiState.avatarUrl != null) {
                     AsyncImage(
                        model = uiState.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(shape)
                            .then(if(uiState.isMe && !uiState.isGuest) Modifier.clickable { 
                                avatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else Modifier)
                            .border(2.dp, MaterialTheme.colorScheme.primary, shape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.primary)
                            .then(if(uiState.isMe && !uiState.isGuest) Modifier.clickable { 
                                avatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else Modifier)
                            .border(2.dp, MaterialTheme.colorScheme.primary, shape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.username.isNotBlank()) uiState.username.first().uppercaseChar().toString() else "?",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
             if (uiState.isAvatarUploading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }

        // Stats Row
        if (!uiState.isGuest) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.followerCount.toString(), style = MaterialTheme.typography.titleLarge)
                        Text(text = "Followers", style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.followingCount.toString(), style = MaterialTheme.typography.titleLarge)
                        Text(text = "Following", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Buttons (Follow or Edit)
        if (!uiState.isMe && !uiState.isGuest) {
            Button(
                onClick = { viewModel.onEvent(ProfileEvent.ToggleFollow) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (uiState.isFollowing) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors(),
                border = if (uiState.isFollowing) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Text(if (uiState.isFollowing) "Unfollow" else "Follow")
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else if (uiState.isMe && !uiState.isGuest) {
             // Edit Username logic
             Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.isEditing) {
                    OutlinedTextField(
                        value = uiState.tempUsername,
                        onValueChange = { viewModel.onEvent(ProfileEvent.UsernameChanged(it)) },
                        label = { Text("Username") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.onEvent(ProfileEvent.SaveUsername) }) {
                         Icon(androidx.compose.material.icons.Icons.Default.Check, "Save")
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Username", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = uiState.username.ifBlank { "No username set" },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(ProfileEvent.ToggleEditMode) }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Edit, "Edit")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isGuest) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("You are viewing as a Guest")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.onEvent(ProfileEvent.SignOut) }) { 
                        Text("Register / Login")
                    }
                }
            }
        }
        


        Text(
            text = if (uiState.isMe) "My Recipes" else "Recipes",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(uiState.myRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onNavigateToDetail(recipe.id) }
                    )
                }
            }
        }
    }
}
