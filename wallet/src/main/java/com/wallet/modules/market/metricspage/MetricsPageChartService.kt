package com.wallet.modules.market.metricspage

import com.chartview.ChartViewType
import com.wallet.core.managers.CurrencyManager
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.stat
import com.wallet.core.stats.statPage
import com.wallet.core.stats.statPeriod
import com.wallet.entities.Currency
import com.wallet.modules.chart.AbstractChartService
import com.wallet.modules.chart.ChartPointsWrapper
import com.wallet.modules.market.tvl.GlobalMarketRepository
import com.wallet.modules.metricchart.MetricsType
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class MetricsPageChartService(
    override val currencyManager: CurrencyManager,
    private val metricsType: MetricsType,
    private val globalMarketRepository: GlobalMarketRepository,
) : AbstractChartService() {

    override val initialChartInterval: HsTimePeriod = HsTimePeriod.Day1

    override val chartIntervals = listOf(
        HsTimePeriod.Day1,
        HsTimePeriod.Week1,
        HsTimePeriod.Month1,
        HsTimePeriod.Month3,
        HsTimePeriod.Month6,
        HsTimePeriod.Year1,
        HsTimePeriod.Year2,
    )

    override val chartViewType = ChartViewType.Line

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency,
    ): Single<ChartPointsWrapper> {
        return globalMarketRepository.getGlobalMarketPoints(
            currency.code,
            chartInterval,
            metricsType
        ).map {
            ChartPointsWrapper(it)
        }
    }

    override fun updateChartInterval(chartInterval: HsTimePeriod?) {
        super.updateChartInterval(chartInterval)

        stat(page = metricsType.statPage, event = StatEvent.SwitchChartPeriod(chartInterval.statPeriod))
    }
}
