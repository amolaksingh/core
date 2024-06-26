package com.wallet.modules.market

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.wallet.R
import com.wallet.core.App
import com.wallet.core.ILocalStorage
import com.wallet.core.IMarketStorage
import com.wallet.core.ViewModelUiState
import com.wallet.core.managers.CurrencyManager
import com.wallet.core.managers.MarketKitWrapper
import com.wallet.core.providers.Translator
import com.wallet.entities.Currency
import com.wallet.entities.LaunchPage
import com.wallet.modules.market.MarketModule.MarketOverviewViewItem
import com.wallet.modules.market.MarketModule.Tab
import com.wallet.modules.metricchart.MetricsType
import io.horizontalsystems.marketkit.models.MarketGlobal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import java.math.BigDecimal

class MarketViewModel(
    private val marketStorage: IMarketStorage,
    private val marketKit: MarketKitWrapper,
    private val currencyManager: CurrencyManager,
    localStorage: ILocalStorage
) : ViewModelUiState<MarketModule.UiState>() {

    val tabs = Tab.entries.toTypedArray()
    private var marketOverviewJob: Job? = null
    private var marketOverviewItems: List<MarketOverviewViewItem> = listOf()
    private var selectedTab: Tab = getInitialTab(localStorage.launchPage)

    init {
        updateMarketOverview()

        viewModelScope.launch {
            currencyManager.baseCurrencyUpdatedFlow.collect {
                updateMarketOverview()
            }
        }
    }

    override fun createState(): MarketModule.UiState {
        return MarketModule.UiState(
            selectedTab,
            marketOverviewItems
        )
    }

    private fun updateMarketOverview() {
        marketOverviewJob?.cancel()
        marketOverviewJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val marketGlobal =
                    marketKit.marketGlobalSingle(currencyManager.baseCurrency.code).await()
                marketOverviewItems = getMarketMetrics(marketGlobal, currencyManager.baseCurrency)
                emitState()
            } catch (e: Throwable) {
                Log.e("TAG", "updateMarketOverview: ", e)
            }
        }
    }

    fun onSelect(tab: Tab) {
        selectedTab = tab
        marketStorage.currentMarketTab = tab
        emitState()
    }

    private fun getMarketMetrics(
        globalMarket: MarketGlobal,
        baseCurrency: Currency
    ): List<MarketOverviewViewItem> {
        val metrics: List<MarketOverviewViewItem> = listOf(
            MarketOverviewViewItem(
                Translator.getString(R.string.MarketGlobalMetrics_TotalMarketCap),
                globalMarket.marketCap?.let { formatFiatShortened(it, baseCurrency.symbol) } ?: "-",
                globalMarket.marketCapChange?.let { getDiff(it) } ?: "----",
                globalMarket.marketCapChange?.let { it > BigDecimal.ZERO } ?: false,
                MetricsType.TotalMarketCap
            ),
            MarketOverviewViewItem(
                Translator.getString(R.string.MarketGlobalMetrics_Volume),
                globalMarket.volume?.let { formatFiatShortened(it, baseCurrency.symbol) } ?: "-",
                globalMarket.volumeChange?.let { getDiff(it) } ?: "----",
                globalMarket.volumeChange?.let { it > BigDecimal.ZERO } ?: false,
                MetricsType.Volume24h
            ),
            MarketOverviewViewItem(
                Translator.getString(R.string.MarketGlobalMetrics_BtcDominance),
                globalMarket.btcDominance?.let {
                    App.numberFormatter.format(it, 0, 2, suffix = "%")
                } ?: "-",
                globalMarket.btcDominanceChange?.let { getDiff(it) } ?: "----",
                globalMarket.btcDominanceChange?.let { it > BigDecimal.ZERO } ?: false,
                MetricsType.TotalMarketCap
            ),
            MarketOverviewViewItem(
                Translator.getString(R.string.MarketGlobalMetrics_EtfInflow),
                globalMarket.etfTotalInflow?.let { formatFiatShortened(it, baseCurrency.symbol) }
                    ?: "-",
                globalMarket.etfDailyInflow?.let {
                    val sign = if (it >= BigDecimal.ZERO) "+" else "-"
                    "$sign${formatFiatShortened(it.abs(), baseCurrency.symbol)}"
                } ?: "----",
                globalMarket.etfDailyInflow?.let { it > BigDecimal.ZERO } ?: false,
                MetricsType.Etf
            ),
            MarketOverviewViewItem(
                Translator.getString(R.string.MarketGlobalMetrics_TvlInDefi),
                globalMarket.tvl?.let { formatFiatShortened(it, baseCurrency.symbol) } ?: "-",
                globalMarket.tvlChange?.let { getDiff(it) } ?: "----",
                globalMarket.tvlChange?.let { it > BigDecimal.ZERO } ?: false,
                MetricsType.TvlInDefi
            )
        )

        return metrics
    }

    private fun getDiff(it: BigDecimal): String {
        val sign = if (it >= BigDecimal.ZERO) "+" else "-"
        return App.numberFormatter.format(it.abs(), 0, 2, sign, "%")
    }

    private fun formatFiatShortened(value: BigDecimal, symbol: String): String {
        return App.numberFormatter.formatFiatShort(value, symbol, 2)
    }

    private fun diff(sourceValue: BigDecimal, targetValue: BigDecimal): BigDecimal =
        if (sourceValue.compareTo(BigDecimal.ZERO) != 0)
            ((targetValue - sourceValue) * BigDecimal(100)) / sourceValue
        else BigDecimal.ZERO


    private fun getInitialTab(launchPage: LaunchPage?) = when (launchPage) {
        LaunchPage.Watchlist -> Tab.Watchlist
        else -> marketStorage.currentMarketTab ?: Tab.Coins
    }
}