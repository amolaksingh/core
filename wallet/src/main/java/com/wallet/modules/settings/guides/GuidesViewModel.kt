package com.wallet.modules.settings.guides

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.entities.DataState
import com.wallet.entities.Guide
import com.wallet.entities.GuideCategory
import com.wallet.entities.ViewState
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class GuidesViewModel(private val repository: GuidesRepository) : ViewModel() {

    var categories by mutableStateOf<List<GuideCategory>>(listOf())
        private set
    var selectedCategory by mutableStateOf<GuideCategory?>(null)
        private set
    var guides by mutableStateOf<List<Guide>>(listOf())
        private set

    var viewState by mutableStateOf<ViewState>(ViewState.Loading)
        private set

    init {
        viewModelScope.launch {
            repository.guideCategories.asFlow().collect { dataState ->
                viewModelScope.launch {
                    dataState.viewState?.let {
                        viewState = it
                    }

                    if (dataState is DataState.Success) {
                        didFetchGuideCategories(dataState.data)
                    }
                }
            }
        }
    }

    fun onSelectCategory(category: GuideCategory) {
        selectedCategory = category
        guides = category.guides
    }

    override fun onCleared() {
        repository.clear()
    }

    private fun didFetchGuideCategories(guideCategories: List<GuideCategory>) {
        categories = guideCategories
        onSelectCategory(guideCategories.first())
    }

}
