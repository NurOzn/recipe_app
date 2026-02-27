package com.melikenurozun.recipe_app.presentation.add_edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onEvent(AddEditEvent.ImageSelected(uri))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (uiState.isEditMode) "Edit Recipe" else "Add Recipe") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Image Picker Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.imageUri != null || uiState.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = uiState.imageUri ?: uiState.imageUrl,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Overlay to indicate editability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                             Surface(
                                 shape = MaterialTheme.shapes.small,
                                 color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                             ) {
                                 Icon(
                                     imageVector = Icons.Default.Add, // Or Edit icon
                                     contentDescription = "Change Image",
                                     modifier = Modifier.padding(8.dp)
                                 )
                             }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Add Recipe Photo",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(AddEditEvent.TitleChanged(it)) },
                    label = { Text("Recipe Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                // Category Dropdown
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        uiState.availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category) },
                                onClick = {
                                    viewModel.onEvent(AddEditEvent.CategoryChanged(category))
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = if (uiState.timeMinutes > 0) uiState.timeMinutes.toString() else "",
                    onValueChange = { viewModel.onEvent(AddEditEvent.TimeChanged(it.toIntOrNull() ?: 0)) },
                    label = { Text("Cooking Time (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = uiState.ingredients,
                    onValueChange = { viewModel.onEvent(AddEditEvent.IngredientsChanged(it)) },
                    label = { Text("Ingredients (one per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = uiState.instructions,
                    onValueChange = { viewModel.onEvent(AddEditEvent.InstructionsChanged(it)) },
                    label = { Text("Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    shape = MaterialTheme.shapes.medium
                )
            }

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onEvent(AddEditEvent.SaveRecipe) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        if (uiState.isEditMode) "Update Recipe" else "Create Recipe",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
