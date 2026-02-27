package com.melikenurozun.recipe_app.presentation.shopping

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onNavigateBack: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var newItemName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shopping List") },
                navigationIcon = {
                     IconButton(onClick = onNavigateBack) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                     }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = uiState.items.joinToString("\n") { 
                            "${if(it.isChecked) "[x]" else "[ ]"} ${it.itemName}" 
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "My Shopping List:\n\n$shareText")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, "Share List")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Add Item Input
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("Add Item (e.g. Milk)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { 
                        if (newItemName.isNotBlank()) {
                            viewModel.onEvent(ShoppingListEvent.AddItem(newItemName))
                            newItemName = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(56.dp) // Square button matching height
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCart, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp), 
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your list is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = uiState.items, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggle = { viewModel.onEvent(ShoppingListEvent.ToggleItem(item)) },
                            onDelete = { viewModel.onEvent(ShoppingListEvent.DeleteItem(item.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: com.melikenurozun.recipe_app.domain.model.ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (item.isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (item.isChecked) 0.dp else 2.dp,
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha=0.7f))
            }
        }
    }
}
