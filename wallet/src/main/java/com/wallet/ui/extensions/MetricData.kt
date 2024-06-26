package com.wallet.ui.extensions

import io.horizontalsystems.bankwallet.modules.metricchart.MetricsType
import com.chartview.ChartData
import java.math.BigDecimal

data class MetricData(
    val value: String?,
    val diff: BigDecimal?,
    val chartData: ChartData?,
    val type: MetricsType
)
