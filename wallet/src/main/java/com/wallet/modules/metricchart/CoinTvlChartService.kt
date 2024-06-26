package com.wallet.modules.metricchart

import com.chartview.ChartViewType
import com.wallet.core.managers.CurrencyManager
import com.wallet.core.managers.MarketKitWrapper
import com.wallet.entities.Currency
import com.wallet.modules.chart.AbstractChartService
import com.wallet.modules.chart.ChartPointsWrapper
import io.horizontalsystems.marketkit.models.ChartPoint

import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class CoinTvlChartService(
    override val currencyManager: CurrencyManager,
    private val marketKit: MarketKitWrapper,
    private val coinUid: String,
) : AbstractChartService() {

    override val initialChartInterval = HsTimePeriod.Month1
    override val chartIntervals = HsTimePeriod.values().toList()
    override val chartViewType = ChartViewType.Line

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency
    ): Single<ChartPointsWrapper> = try {
        marketKit.marketInfoTvlSingle(coinUid, currency.code, chartInterval)
            .map { info ->
                info.map { ChartPoint(it.value.toFloat().toBigDecimal(), it.timestamp,0.toBigDecimal()) }
            }
            .map { ChartPointsWrapper(it) }
    } catch (e: Exception) {
        Single.error(e)
    }

}
