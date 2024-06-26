package com.wallet.modules.market.platform

import androidx.lifecycle.viewModelScope
import com.wallet.R
import com.wallet.core.ViewModelUiState
import com.wallet.core.iconUrl
import com.wallet.core.managers.MarketFavoritesManager
import com.wallet.core.providers.Translator
import com.wallet.entities.ViewState
import com.wallet.modules.market.ImageSource
import com.wallet.modules.market.MarketItem
import com.wallet.modules.market.MarketModule
import com.wallet.modules.market.MarketViewItem
import com.wallet.modules.market.SortingField
import com.wallet.modules.market.sort
import com.wallet.modules.market.topplatforms.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MarketPlatformViewModel(
    platform: Platform,
    private val repository: MarketPlatformCoinsRepository,
    private val favoritesManager: MarketFavoritesManager,
) : ViewModelUiState<MarketPlatformUiState>() {

    val sortingFields = listOf(
        SortingField.HighestCap,
        SortingField.LowestCap,
        SortingField.TopGainers,
        SortingField.TopLosers,
    )

    private var sortingField: SortingField = SortingField.HighestCap
    private var viewState: ViewState = ViewState.Loading
    private var viewItems: List<MarketViewItem> = listOf()
    private var cache: List<MarketItem> = emptyList()
    private var isRefreshing = false

    val header = MarketModule.Header(
        Translator.getString(R.string.MarketPlatformCoins_PlatformEcosystem, platform.name),
        Translator.getString(
            R.string.MarketPlatformCoins_PlatformEcosystemDescription,
            platform.name
        ),
        ImageSource.Remote(platform.iconUrl)
    )

    init {
        sync()
    }

    override fun createState() = MarketPlatformUiState(
        viewItems = viewItems,
        viewState = viewState,
        sortingField = sortingField,
        isRefreshing = isRefreshing,
    )

    fun refresh() {
        refreshWithMinLoadingSpinnerPeriod()
    }

    fun onErrorClick() {
        refreshWithMinLoadingSpinnerPeriod()
    }

    fun onSelectSortingField(sortingField: SortingField) {
        this.sortingField = sortingField
        sync()
    }

    fun onAddFavorite(coinUid: String) {
        favoritesManager.add(coinUid)
        sync()
    }

    fun onRemoveFavorite(coinUid: String) {
        favoritesManager.remove(coinUid)
        sync()
    }

    private fun sync(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!forceRefresh && cache.isNotEmpty()) {
                viewItems = cache
                    .sort(sortingField)
                    .map { item ->
                        marketViewItem(item)
                    }
                viewState = ViewState.Success
                emitState()
            } else {
                fetchFromRepository(forceRefresh)
            }
        }
    }

    private suspend fun fetchFromRepository(forceRefresh: Boolean) {
        try {
            viewItems = repository.get(sortingField, forceRefresh)?.map {
                marketViewItem(it)
            } ?: listOf()

            viewState = ViewState.Success
        } catch (e: Throwable) {
            viewState = ViewState.Error(e)
        }
        emitState()
    }

    private fun marketViewItem(item: MarketItem): MarketViewItem = MarketViewItem.create(
        marketItem = item,
        favorited = favoritesManager.getAll().map { it.coinUid }.contains(item.fullCoin.coin.uid)
    )

    private fun refreshWithMinLoadingSpinnerPeriod() {
        viewModelScope.launch {
            sync(true)

            isRefreshing = true
            delay(1000)
            isRefreshing = false
            emitState()
        }
    }
}

data class MarketPlatformUiState(
    val viewItems: List<MarketViewItem>,
    val viewState: ViewState,
    val sortingField: SortingField,
    val isRefreshing: Boolean,
)