package com.modernapps.dishcrafter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RecipeState {
    object Idle : RecipeState()
    object Loading : RecipeState()
    data class Success(val recipe: String) : RecipeState()
    data class Error(val message: String) : RecipeState()
}

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow<RecipeState>(RecipeState.Idle)
    val state: StateFlow<RecipeState> = _state

    fun generateRecipe(ingredients: String) {
        if (ingredients.isBlank()) return

        _state.value = RecipeState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val recipe = GeminiApi.generateRecipe(ingredients)
                _state.value = RecipeState.Success(recipe)
            } catch (e: Exception) {
                _state.value = RecipeState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}
