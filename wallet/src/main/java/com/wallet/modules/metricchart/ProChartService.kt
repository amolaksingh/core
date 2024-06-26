package com.wallet.modules.metricchart

import com.chartview.ChartViewType
import com.chartview.models.ChartPoint
import com.wallet.core.managers.CurrencyManager
import com.wallet.core.managers.MarketKitWrapper
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.stat
import com.wallet.core.stats.statPage
import com.wallet.core.stats.statPeriod
import com.wallet.entities.Currency
import com.wallet.modules.chart.AbstractChartService
import com.wallet.modules.chart.ChartPointsWrapper
import io.horizontalsystems.marketkit.models.HsTimePeriod
import io.reactivex.Single

class ProChartService(
    override val currencyManager: CurrencyManager,
    private val marketKit: MarketKitWrapper,
    private val coinUid: String,
    private val chartType: ProChartModule.ChartType
) : AbstractChartService() {

    override val initialChartInterval = HsTimePeriod.Month1
    override val hasVolumes = chartType == ProChartModule.ChartType.TxCount

    override val chartIntervals = when (chartType) {
        ProChartModule.ChartType.CexVolume,
        ProChartModule.ChartType.DexVolume,
        ProChartModule.ChartType.TxCount,
        ProChartModule.ChartType.AddressesCount,
        ProChartModule.ChartType.DexLiquidity -> listOf(
            HsTimePeriod.Week1,
            HsTimePeriod.Week2,
            HsTimePeriod.Month1,
            HsTimePeriod.Month3,
            HsTimePeriod.Month6,
            HsTimePeriod.Year1,
        )
        ProChartModule.ChartType.Tvl -> listOf(
            HsTimePeriod.Day1,
            HsTimePeriod.Week1,
            HsTimePeriod.Week2,
            HsTimePeriod.Month1,
            HsTimePeriod.Month3,
            HsTimePeriod.Month6,
            HsTimePeriod.Year1,
        )
    }

    override val chartViewType = when (chartType) {
        ProChartModule.ChartType.Tvl,
        ProChartModule.ChartType.AddressesCount,
        ProChartModule.ChartType.DexLiquidity -> ChartViewType.Line
        ProChartModule.ChartType.CexVolume,
        ProChartModule.ChartType.DexVolume,
        ProChartModule.ChartType.TxCount -> ChartViewType.Bar
    }

    override fun updateChartInterval(chartInterval: HsTimePeriod?) {
        super.updateChartInterval(chartInterval)

        stat(chartType.statPage, event = StatEvent.SwitchChartPeriod(chartInterval.statPeriod))
    }

    override fun getItems(
        chartInterval: HsTimePeriod,
        currency: Currency,
    ): Single<ChartPointsWrapper> {
        val chartDataSingle: Single<List<io.horizontalsystems.marketkit.models.ChartPoint>> = when (chartType) {
            ProChartModule.ChartType.CexVolume ->
                marketKit.cexVolumesSingle(coinUid, currency.code, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            io.horizontalsystems.marketkit.models.ChartPoint(
                                value = chartPoint.value,
                                timestamp = chartPoint.timestamp,
                                volume = chartPoint.volume
                            )
                        }
                    }


            ProChartModule.ChartType.DexVolume ->
                marketKit.dexVolumesSingle(coinUid, currency.code, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            io.horizontalsystems.marketkit.models.ChartPoint(
                                value = chartPoint.volume,
                                timestamp = chartPoint.timestamp,
                                volume = 0.toBigDecimal()
                            )
                        }
                    }

            ProChartModule.ChartType.DexLiquidity ->
                marketKit.dexLiquiditySingle(coinUid, currency.code, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            io.horizontalsystems.marketkit.models.ChartPoint(
                                value = chartPoint.volume,
                                timestamp = chartPoint.timestamp,
                                volume = 0.toBigDecimal()
                            )
                        }
                    }

            ProChartModule.ChartType.TxCount ->
                marketKit.transactionDataSingle(coinUid, chartInterval, null)
                    .map { response ->
                        response.map { chartPoint ->
                            io.horizontalsystems.marketkit.models.ChartPoint(
                                value = chartPoint.count.toBigDecimal(),
                                timestamp = chartPoint.timestamp,
                                volume = chartPoint.volume,
                            )
                        }
                    }

            ProChartModule.ChartType.AddressesCount ->
                marketKit.activeAddressesSingle(coinUid, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            io.horizontalsystems.marketkit.models.ChartPoint(
                                value = chartPoint.count.toBigDecimal(),
                                timestamp = chartPoint.timestamp,
                                volume = 0.toBigDecimal()
                            )
                        }
                    }

            ProChartModule.ChartType.Tvl ->
                marketKit.marketInfoTvlSingle(coinUid, currency.code, chartInterval)
                    .map { response ->
                        response.map { chartPoint ->
                            io.horizontalsystems.marketkit.models.ChartPoint(
                                value = chartPoint.value,
                                timestamp = chartPoint.timestamp,
                                volume = chartPoint.volume,
                            )
                        }
                    }
        }

        val isMovementChart = when (chartType) {
            ProChartModule.ChartType.DexLiquidity,
            ProChartModule.ChartType.AddressesCount,
            ProChartModule.ChartType.Tvl -> true
            ProChartModule.ChartType.CexVolume,
            ProChartModule.ChartType.DexVolume,
            ProChartModule.ChartType.TxCount -> false
        }

        return chartDataSingle.map {

            ChartPointsWrapper(it, isMovementChart)
        }
    }
}
