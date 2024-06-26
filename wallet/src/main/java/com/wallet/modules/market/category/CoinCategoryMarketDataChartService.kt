package com.wallet.modules.market.category

import com.chartview.ChartViewType
import com.chartview.models.ChartPoint
import com.wallet.core.managers.CurrencyManager
import com.wallet.core.managers.MarketKitWrapper
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.stat
import com.wallet.core.stats.statPeriod
import com.wallet.entities.Currency
import com.wallet.modules.chart.AbstractChartService
import com.wallet.modules.chart.ChartPointsWrapper
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class CoinCategoryMarketDataChartService(
    override val currencyManager: CurrencyManager,
    private val marketKit: MarketKitWrapper,
    private val categoryUid: String,
) : AbstractChartService() {

    override val initialChartInterval = HsTimePeriod.Day1
    override val chartIntervals = listOf(HsTimePeriod.Day1, HsTimePeriod.Week1, HsTimePeriod.Month1)
    override val chartViewType = ChartViewType.Line

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency
    ): Single<ChartPointsWrapper> = try {
        marketKit.coinCategoryMarketPointsSingle(categoryUid, chartInterval, currency.code)
            .map { info ->
                info.map { io.horizontalsystems.marketkit.models.ChartPoint(it.marketCap, it.timestamp,0.toBigDecimal()) }
            }
            .map { ChartPointsWrapper(it) }
    } catch (e: Exception) {
        Single.error(e)
    }

    override fun updateChartInterval(chartInterval: HsTimePeriod?) {
        super.updateChartInterval(chartInterval)

        stat(page = StatPage.CoinCategory, event = StatEvent.SwitchChartPeriod(chartInterval.statPeriod))
    }
}
