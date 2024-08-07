package com.wallet.modules.market.toppairs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wallet.core.App
import com.wallet.core.ViewModelUiState
import com.wallet.core.managers.CurrencyManager
import com.wallet.core.managers.MarketKitWrapper
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.StatSection
import com.wallet.core.stats.StatSortType
import com.wallet.core.stats.stat
import com.wallet.entities.ViewState
import com.wallet.modules.market.overview.TopPairViewItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext

class TopPairsViewModel(
    private val marketKit: MarketKitWrapper,
    private val currencyManager: CurrencyManager,
) : ViewModelUiState<TopPairsUiState>() {
    private var isRefreshing = false
    private var items = listOf<TopPairViewItem>()
    private var viewState: ViewState = ViewState.Loading
    private var sortDescending = true

    init {
        viewModelScope.launch {
            currencyManager.baseCurrencyUpdatedSignal.asFlow().collect {
                fetchItems()
                emitState()
            }
        }

        viewModelScope.launch {
            fetchItems()
            emitState()
        }
    }

    override fun createState() = TopPairsUiState(
        isRefreshing = isRefreshing,
        items = items,
        viewState = viewState,
        sortDescending = sortDescending,
    )

    private fun sortItems(items: List<TopPairViewItem>) =
        if (sortDescending) items.sortedByDescending { it.volume } else items.sortedBy { it.volume }

    private suspend fun fetchItems() = withContext(Dispatchers.Default) {
        try {
            val topPairs =
                marketKit.topPairsSingle(currencyManager.baseCurrency.code, 1, 100).await()
            val pairs = topPairs.map {
                TopPairViewItem.createFromTopPair(it, currencyManager.baseCurrency.symbol)
            }
            items = sortItems(pairs)
            viewState = ViewState.Success
        } catch (e: CancellationException) {
            // no-op
        } catch (e: Throwable) {
            viewState = ViewState.Error(e)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            emitState()

            fetchItems()
            delay(1000)
            isRefreshing = false
            emitState()
        }

        stat(
            page = StatPage.Markets,
            section = StatSection.Pairs,
            event = StatEvent.Refresh
        )

    }

    fun onErrorClick() {
        refresh()
    }

    fun toggleSorting() {
        sortDescending = !sortDescending
        emitState()
        viewModelScope.launch {
            items = sortItems(items)
            emitState()
        }

        stat(
            page = StatPage.Markets,
            section = StatSection.Pairs,
            event = StatEvent.SwitchSortType(if (sortDescending) StatSortType.HighestVolume else StatSortType.LowestVolume)
        )
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TopPairsViewModel(App.marketKit, App.currencyManager) as T
        }
    }

}

data class TopPairsUiState(
    val isRefreshing: Boolean,
    val items: List<TopPairViewItem>,
    val viewState: ViewState,
    val sortDescending: Boolean,
)
