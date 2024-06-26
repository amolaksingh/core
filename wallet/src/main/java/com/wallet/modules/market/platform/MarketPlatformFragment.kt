package com.wallet.modules.market.platform

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.core.getInput
import com.wallet.core.slideFromRight
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.stat
import com.wallet.core.stats.statSortType
import com.wallet.entities.ViewState
import com.wallet.modules.chart.ChartViewModel
import com.wallet.modules.coin.CoinFragment
import com.wallet.modules.coin.overview.ui.Chart
import com.wallet.modules.coin.overview.ui.Loading
import com.wallet.modules.market.ImageSource
import com.wallet.modules.market.topcoins.OptionController
import com.wallet.modules.market.topplatforms.Platform
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.HSSwipeRefresh
import com.wallet.ui.compose.Select
import com.wallet.ui.compose.components.AlertGroup
import com.wallet.ui.compose.components.CoinList
import com.wallet.ui.compose.components.HSpacer
import com.wallet.ui.compose.components.HeaderSorting
import com.wallet.ui.compose.components.ListErrorView
import com.wallet.ui.compose.components.TopCloseButton
import com.wallet.ui.compose.components.subhead2_grey
import com.wallet.ui.compose.components.title3_leah

class MarketPlatformFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {

        val platform = navController.getInput<Platform>()

        if (platform == null) {
            navController.popBackStack()
            return
        }

        val factory = MarketPlatformModule.Factory(platform)

        PlatformScreen(
            factory = factory,
            onCloseButtonClick = { navController.popBackStack() },
            onCoinClick = { coinUid ->
                val arguments = CoinFragment.Input(coinUid)
                navController.slideFromRight(R.id.coinFragment, arguments)

                stat(page = StatPage.TopPlatform, event = StatEvent.OpenCoin(coinUid))
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlatformScreen(
    factory: ViewModelProvider.Factory,
    onCloseButtonClick: () -> Unit,
    onCoinClick: (String) -> Unit,
    viewModel: MarketPlatformViewModel = viewModel(factory = factory),
    chartViewModel: ChartViewModel = viewModel(factory = factory),
) {

    val uiState = viewModel.uiState
    var scrollToTopAfterUpdate by rememberSaveable { mutableStateOf(false) }
    var openSortingSelector by rememberSaveable { mutableStateOf(false) }

    Surface(color = ComposeAppTheme.colors.tyler) {
        Column {
            TopCloseButton(onCloseButtonClick)

            HSSwipeRefresh(
                refreshing = uiState.isRefreshing,
                onRefresh = {
                    viewModel.refresh()

                    stat(page = StatPage.TopPlatform, event = StatEvent.Refresh)
                }
            ) {
                Crossfade(uiState.viewState, label = "") { state ->
                    when (state) {
                        ViewState.Loading -> {
                            Loading()
                        }

                        is ViewState.Error -> {
                            ListErrorView(
                                stringResource(R.string.SyncError),
                                viewModel::onErrorClick
                            )
                        }

                        ViewState.Success -> {
                            uiState.viewItems.let { viewItems ->
                                CoinList(
                                    items = viewItems,
                                    scrollToTop = scrollToTopAfterUpdate,
                                    onAddFavorite = { uid ->
                                        viewModel.onAddFavorite(uid)

                                        stat(
                                            page = StatPage.TopPlatform,
                                            event = StatEvent.AddToWatchlist(uid)
                                        )
                                    },
                                    onRemoveFavorite = { uid ->
                                        viewModel.onRemoveFavorite(uid)

                                        stat(
                                            page = StatPage.TopPlatform,
                                            event = StatEvent.RemoveFromWatchlist(uid)
                                        )
                                    },
                                    onCoinClick = onCoinClick,
                                    preItems = {
                                        viewModel.header.let {
                                            item {
                                                HeaderContent(it.title, it.description, it.icon)
                                            }
                                        }
                                        item {
                                            Chart(chartViewModel = chartViewModel)
                                        }
                                        stickyHeader {
                                            HeaderSorting(borderTop = true, borderBottom = true) {
                                                HSpacer(width = 16.dp)
                                                OptionController(
                                                    uiState.sortingField.titleResId,
                                                    onOptionClick = {
                                                        openSortingSelector = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                                if (scrollToTopAfterUpdate) {
                                    scrollToTopAfterUpdate = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (openSortingSelector) {
        AlertGroup(
            R.string.Market_Sort_PopupTitle,
            Select(uiState.sortingField, viewModel.sortingFields),
            { selected ->
                scrollToTopAfterUpdate = true
                viewModel.onSelectSortingField(selected)
                openSortingSelector = false
                stat(
                    page = StatPage.TopPlatform,
                    event = StatEvent.SwitchSortType(selected.statSortType)
                )
            },
            { openSortingSelector = false }
        )
    }
}

@Composable
private fun HeaderContent(title: String, description: String, image: ImageSource) {
    Column {
        Row(
            modifier = Modifier
                .height(100.dp)
                .padding(horizontal = 16.dp)
                .background(ComposeAppTheme.colors.tyler)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .weight(1f)
            ) {
                title3_leah(
                    text = title,
                )
                subhead2_grey(
                    text = description,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Image(
                painter = image.painter(),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 24.dp)
                    .size(32.dp),
            )
        }
    }
}

@Preview
@Composable
fun HeaderContentPreview() {
    ComposeAppTheme {
        HeaderContent(
            "Solana Ecosystem",
            "Market cap of all protocols on the Solana chain",
            ImageSource.Local(R.drawable.logo_ethereum_24)
        )
    }
}
