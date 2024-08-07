package com.wallet.modules.market

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
import com.wallet.core.stats.statPage
import com.wallet.core.stats.statTab
import com.wallet.modules.coin.CoinFragment
import com.wallet.modules.market.MarketModule.Tab
import com.wallet.modules.market.favorites.MarketFavoritesScreen
import com.wallet.modules.market.posts.MarketPostsScreen
import com.wallet.modules.market.topcoins.TopCoins
import com.wallet.modules.market.toppairs.TopPairsScreen
import com.wallet.modules.market.topplatforms.TopPlatforms
import com.wallet.modules.metricchart.MetricsType
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.HSpacer
import com.wallet.ui.compose.components.MenuItem
import com.wallet.ui.compose.components.ScrollableTabs
import com.wallet.ui.compose.components.TabItem
import com.wallet.ui.compose.components.caption_bran
import com.wallet.ui.compose.components.caption_grey
import com.wallet.ui.compose.components.caption_lucian
import com.wallet.ui.compose.components.caption_remus

@Composable
fun MarketScreen(navController: NavController) {
    val viewModel = viewModel<MarketViewModel>(factory = MarketModule.Factory())
    val uiState = viewModel.uiState
    val tabs = viewModel.tabs

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Market_Title),
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Market_Search),
                        icon = R.drawable.icon_search,
                        tint = ComposeAppTheme.colors.jacob,
                        onClick = {
                            navController.slideFromRight(R.id.marketSearchFragment)

                            stat(
                                page = StatPage.Markets,
                                event = StatEvent.Open(StatPage.MarketSearch)
                            )
                        },
                    ),
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Market_Filters),
                        icon = R.drawable.ic_manage_2_24,
                        onClick = {
                            navController.slideFromRight(R.id.marketAdvancedSearchFragment)

                            stat(
                                page = StatPage.Markets,
                                event = StatEvent.Open(StatPage.AdvancedSearch)
                            )
                        },
                    ),
                )
            )
        }
    ) {
        Column(
            Modifier
                .padding(it)
                .background(ComposeAppTheme.colors.tyler)
        ) {
            MetricsBoard(navController, uiState.marketOverviewItems)
            Divider(
                color = ComposeAppTheme.colors.steel10,
                thickness = 1.dp
            )
            TabsSection(navController, tabs, uiState.selectedTab) { tab ->
                viewModel.onSelect(tab)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabsSection(
    navController: NavController,
    tabs: Array<Tab>,
    selectedTab: Tab,
    onTabClick: (Tab) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = selectedTab.ordinal) { tabs.size }

    LaunchedEffect(key1 = selectedTab, block = {
        pagerState.scrollToPage(selectedTab.ordinal)

        stat(page = StatPage.Markets, event = StatEvent.SwitchTab(selectedTab.statTab))
    })
    val tabItems = tabs.map {
        TabItem(stringResource(id = it.titleResId), it == selectedTab, it)
    }

    ScrollableTabs(tabItems) {
        onTabClick(it)
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false
    ) { page ->
        when (tabs[page]) {
            Tab.Coins -> {
                TopCoins(onCoinClick = { onCoinClick(it, navController) })
            }

            Tab.Watchlist -> {
                MarketFavoritesScreen(navController)
            }

            Tab.Posts -> {
                MarketPostsScreen()
            }

            Tab.Platform -> {
                TopPlatforms(navController)
            }

            Tab.Pairs -> {
                TopPairsScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetricsBoard(
    navController: NavController,
    marketOverviewItems: List<MarketModule.MarketOverviewViewItem>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(ComposeAppTheme.colors.tyler)
            .basicMarquee(
                iterations = Int.MAX_VALUE,
                spacing = MarqueeSpacing(0.dp)
            ),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        HSpacer(4.dp)
        marketOverviewItems.forEach { item ->
            Row(
                modifier = Modifier
                    .clickable {
                        openMetricsPage(item.metricsType, navController)
                    }
                    .padding(8.dp)
            ) {
                HSpacer(12.dp)
                caption_grey(text = item.title)
                HSpacer(4.dp)
                caption_bran(text = item.value)
                HSpacer(4.dp)
                if (item.changePositive) {
                    caption_remus(text = item.change)
                } else {
                    caption_lucian(text = item.change)
                }
                HSpacer(12.dp)
            }
        }
        HSpacer(4.dp)
    }
}

private fun openMetricsPage(metricsType: MetricsType, navController: NavController) {
    when (metricsType) {
        MetricsType.TvlInDefi -> {
            navController.slideFromBottom(R.id.tvlFragment)
        }
        MetricsType.Etf -> {
            navController.slideFromBottom(R.id.etfFragment)
        }
        else -> {
            navController.slideFromBottom(R.id.metricsPageFragment, metricsType)
        }
    }

    stat(page = StatPage.Markets, event = StatEvent.Open(metricsType.statPage))
}

private fun onCoinClick(coinUid: String, navController: NavController) {
    val arguments = CoinFragment.Input(coinUid)

    navController.slideFromRight(R.id.coinFragment, arguments)

    stat(page = StatPage.Markets, section = StatSection.Coins, event = StatEvent.OpenCoin(coinUid))
}
