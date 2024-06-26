package com.wallet.modules.coin

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.core.getInput
import com.wallet.core.slideFromBottom
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.stat
import com.wallet.core.stats.statTab
import com.wallet.modules.coin.analytics.CoinAnalyticsScreen
import com.wallet.modules.coin.coinmarkets.CoinMarketsScreen
import com.wallet.modules.coin.overview.ui.CoinOverviewScreen
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.ListEmptyView
import com.wallet.ui.compose.components.MenuItem
import com.wallet.ui.compose.components.TabItem
import com.wallet.ui.compose.components.Tabs
import com.core.helpers.HudHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class CoinFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()
        val coinUid = input?.coinUid ?: ""

        CoinScreen(
            coinUid,
            coinViewModel(coinUid),
            navController,
            childFragmentManager
        )
    }

    private fun coinViewModel(coinUid: String): CoinViewModel? = try {
        val viewModel by navGraphViewModels<CoinViewModel>(R.id.coinFragment) {
            CoinModule.Factory(coinUid)
        }
        viewModel
    } catch (e: Exception) {
        null
    }

    @Parcelize
    data class Input(val coinUid: String) : Parcelable
}

@Composable
fun CoinScreen(
    coinUid: String,
    coinViewModel: CoinViewModel?,
    navController: NavController,
    fragmentManager: FragmentManager
) {
    if (coinViewModel != null) {
        CoinTabs(coinViewModel, navController, fragmentManager)
    } else {
        CoinNotFound(coinUid, navController)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoinTabs(
    viewModel: CoinViewModel,
    navController: NavController,
    fragmentManager: FragmentManager
) {
    val tabs = viewModel.tabs
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = viewModel.fullCoin.coin.code,
            navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack() })
            },
            menuItems = buildList {
                if (viewModel.isWatchlistEnabled) {
                    if (viewModel.isFavorite) {
                        add(
                            MenuItem(
                                title = TranslatableString.ResString(R.string.CoinPage_Unfavorite),
                                icon = com.icons.R.drawable.ic_filled_star_24,
                                tint = ComposeAppTheme.colors.jacob,
                                onClick = {
                                    viewModel.onUnfavoriteClick()

                                    stat(page = StatPage.CoinPage, event = StatEvent.RemoveFromWatchlist(viewModel.fullCoin.coin.uid))
                                }
                            )
                        )
                    } else {
                        add(
                            MenuItem(
                                title = TranslatableString.ResString(R.string.CoinPage_Favorite),
                                icon = com.icons.R.drawable.ic_star_24,
                                onClick = {
                                    viewModel.onFavoriteClick()

                                    stat(page = StatPage.CoinPage, event = StatEvent.AddToWatchlist(viewModel.fullCoin.coin.uid))
                                }
                            )
                        )
                    }
                }
            }
        )

        val selectedTab = tabs[pagerState.currentPage]
        val tabItems = tabs.map {
            TabItem(stringResource(id = it.titleResId), it == selectedTab, it)
        }
        Tabs(tabItems, onClick = { tab ->
            coroutineScope.launch {
                pagerState.scrollToPage(tab.ordinal)

                stat(page = StatPage.CoinPage, event = StatEvent.SwitchTab(tab.statTab))

                if (tab == CoinModule.Tab.Details && viewModel.shouldShowSubscriptionInfo()) {
                    viewModel.subscriptionInfoShown()

                    delay(1000)
                    navController.slideFromBottom(R.id.subscriptionInfoFragment)
                }
            }
        })

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false
        ) { page ->
            when (tabs[page]) {
                CoinModule.Tab.Overview -> {
                    CoinOverviewScreen(
                        fullCoin = viewModel.fullCoin,
                        navController = navController
                    )
                }

                CoinModule.Tab.Market -> {
                    CoinMarketsScreen(fullCoin = viewModel.fullCoin)
                }

                CoinModule.Tab.Details -> {
                    CoinAnalyticsScreen(
                        fullCoin = viewModel.fullCoin,
                        navController = navController,
                        fragmentManager = fragmentManager
                    )
                }
//                CoinModule.Tab.Tweets -> {
//                    CoinTweetsScreen(fullCoin = viewModel.fullCoin)
//                }
            }
        }

        viewModel.successMessage?.let {
            HudHelper.showSuccessMessage(view, it)

            viewModel.onSuccessMessageShown()
        }
    }
}

@Composable
fun CoinNotFound(coinUid: String, navController: NavController) {
    Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = coinUid,
            navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack() })
            }
        )

        ListEmptyView(
            text = stringResource(R.string.CoinPage_CoinNotFound, coinUid),
            icon = R.drawable.ic_not_available
        )

    }
}
