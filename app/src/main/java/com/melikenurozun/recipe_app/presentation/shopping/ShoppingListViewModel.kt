package com.melikenurozun.recipe_app.presentation.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikenurozun.recipe_app.domain.model.ShoppingItem
import com.melikenurozun.recipe_app.domain.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init {
        getItems()
    }

    private fun getItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            shoppingRepository.getItems().collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    fun onEvent(event: ShoppingListEvent) {
        when (event) {
            is ShoppingListEvent.AddItem -> {
                viewModelScope.launch {
                    if (event.itemName.isNotBlank()) {
                        shoppingRepository.addItem(event.itemName)
                    }
                }
            }
            is ShoppingListEvent.ToggleItem -> {
                viewModelScope.launch {
                    shoppingRepository.toggleItem(event.item)
                }
            }
            is ShoppingListEvent.DeleteItem -> {
                viewModelScope.launch {
                    shoppingRepository.deleteItem(event.id)
                }
            }
        }
    }
}

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ShoppingListEvent {
    data class AddItem(val itemName: String) : ShoppingListEvent()
    data class ToggleItem(val item: ShoppingItem) : ShoppingListEvent()
    data class DeleteItem(val id: String) : ShoppingListEvent()
}
