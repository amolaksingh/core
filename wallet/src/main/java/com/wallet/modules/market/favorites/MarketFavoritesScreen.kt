package com.wallet.modules.market.favorites

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.slideFromBottom
import com.wallet.core.slideFromRight
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.StatSection
import com.wallet.core.stats.stat
import com.wallet.core.stats.statPeriod
import com.wallet.core.stats.statSortType
import com.wallet.entities.ViewState
import com.wallet.modules.coin.CoinFragment
import com.wallet.modules.coin.overview.ui.Loading
import com.wallet.modules.market.topcoins.OptionController
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.HSSwipeRefresh
import com.wallet.ui.compose.Select
import com.wallet.ui.compose.components.AlertGroup
import com.wallet.ui.compose.components.ButtonSecondaryCircle
import com.wallet.ui.compose.components.ButtonSecondaryDefault
import com.wallet.ui.compose.components.ButtonSecondaryYellow
import com.wallet.ui.compose.components.CoinListOrderable
import com.wallet.ui.compose.components.HSpacer
import com.wallet.ui.compose.components.HeaderSorting
import com.wallet.ui.compose.components.ListEmptyView
import com.wallet.ui.compose.components.ListErrorView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarketFavoritesScreen(
    navController: NavController
) {
    val currentBackStackEntry = remember { navController.currentBackStackEntry }
    val viewModel = viewModel<MarketFavoritesViewModel>(
        viewModelStoreOwner = currentBackStackEntry!!,
        factory = MarketFavoritesModule.Factory()
    )
    val uiState = viewModel.uiState
    var openSortingSelector by rememberSaveable { mutableStateOf(false) }
    var openPeriodSelector by rememberSaveable { mutableStateOf(false) }
    var scrollToTopAfterUpdate by rememberSaveable { mutableStateOf(false) }
    var manualOrderEnabled by rememberSaveable { mutableStateOf(false) }

    HSSwipeRefresh(
        refreshing = uiState.isRefreshing,
        onRefresh = {
            viewModel.refresh()

            stat(page = StatPage.Markets,  section = StatSection.Watchlist, event = StatEvent.Refresh)
        }
    ) {
        Crossfade(
            targetState = uiState.viewState,
            label = ""
        ) { viewState ->
            when (viewState) {
                ViewState.Loading -> {
                    Loading()
                }

                is ViewState.Error -> {
                    ListErrorView(stringResource(R.string.SyncError), viewModel::onErrorClick)
                }

                ViewState.Success -> {
                    if (uiState.viewItems.isEmpty()) {
                        ListEmptyView(
                            text = stringResource(R.string.Market_Tab_Watchlist_EmptyList),
                            icon = com.icons.R.drawable.ic_rate_24
                        )
                    } else {
                        if (uiState.showSignalsInfo) {
                            viewModel.onSignalsInfoShown()

                            navController.slideFromBottom(R.id.marketSignalsFragment)
                        }

                        CoinListOrderable(
                            items = uiState.viewItems,
                            scrollToTop = scrollToTopAfterUpdate,
                            onAddFavorite = { /*not used */ },
                            onRemoveFavorite = { uid ->
                                viewModel.removeFromFavorites(uid)

                                stat(page = StatPage.Markets,  section = StatSection.Watchlist, event = StatEvent.RemoveFromWatchlist(uid))
                            },
                            onCoinClick = { coinUid ->
                                val arguments = CoinFragment.Input(coinUid)
                                navController.slideFromRight(R.id.coinFragment, arguments)

                                stat(page = StatPage.Markets, section = StatSection.Watchlist, event = StatEvent.OpenCoin(coinUid))
                            },
                            onReorder = { from, to ->
                                viewModel.reorder(from, to)
                            },
                            canReorder = uiState.sortingField == WatchlistSorting.Manual,
                            showReorderArrows = uiState.sortingField == WatchlistSorting.Manual && manualOrderEnabled,
                            enableManualOrder = {
                                manualOrderEnabled = true
                            },
                            preItems = {
                                stickyHeader {
                                    HeaderSorting(
                                        borderBottom = true,
                                    ) {
                                        HSpacer(width = 16.dp)
                                        OptionController(
                                            uiState.sortingField.titleResId,
                                            onOptionClick = {
                                                openSortingSelector = true
                                            }
                                        )
                                        if (uiState.sortingField == WatchlistSorting.Manual) {
                                            HSpacer(width = 12.dp)
                                            ButtonSecondaryCircle(
                                                icon = R.drawable.ic_edit_20,
                                                tint = if (manualOrderEnabled) ComposeAppTheme.colors.dark else ComposeAppTheme.colors.leah,
                                                background = if (manualOrderEnabled) ComposeAppTheme.colors.jacob else ComposeAppTheme.colors.steel20,
                                            ) {
                                                manualOrderEnabled = !manualOrderEnabled
                                            }
                                        }
                                        HSpacer(width = 12.dp)
                                        OptionController(
                                            uiState.period.titleResId,
                                            onOptionClick = {
                                                openPeriodSelector = true
                                            }
                                        )
                                        HSpacer(width = 12.dp)
                                        SignalButton(
                                            turnedOn = uiState.showSignal,
                                            onClick = {
                                                viewModel.onToggleSignal()
                                            })
                                        HSpacer(width = 16.dp)
                                    }
                                }
                            }
                        )
                        if (scrollToTopAfterUpdate) {
                            scrollToTopAfterUpdate = false
                        }
                    }
                }

                null -> {}
            }
        }
    }

    if (openSortingSelector) {
        AlertGroup(
            title = R.string.Market_Sort_PopupTitle,
            select = Select(uiState.sortingField, viewModel.sortingOptions),
            onSelect = { selected ->
                manualOrderEnabled = false
                openSortingSelector = false
                scrollToTopAfterUpdate = true
                viewModel.onSelectSortingField(selected)

                stat(page = StatPage.Markets, section = StatSection.Watchlist, event = StatEvent.SwitchSortType(selected.statSortType))
            },
            onDismiss = {
                openSortingSelector = false
            }
        )
    }
    if (openPeriodSelector) {
        AlertGroup(
            title = R.string.CoinPage_Period,
            select = Select(uiState.period, viewModel.periods),
            onSelect = { selected ->
                openPeriodSelector = false
                scrollToTopAfterUpdate = true
                viewModel.onSelectPeriod(selected)

                stat(page = StatPage.Markets, section = StatSection.Watchlist, event = StatEvent.SwitchPeriod(selected.statPeriod))
            },
            onDismiss = {
                openPeriodSelector = false
            }
        )
    }

}

@Composable
fun SignalButton(turnedOn: Boolean, onClick: () -> Unit) {
    val title = stringResource(id = R.string.Market_Signals)
    if (turnedOn) {
        ButtonSecondaryYellow(
            title = title,
            onClick = onClick
        )
    } else {
        ButtonSecondaryDefault(
            title = title,
            onClick = onClick
        )
    }
}
