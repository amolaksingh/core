package com.wallet.ui.extensions

import com.wallet.modules.metricchart.MetricsType
import com.chartview.ChartData
import java.math.BigDecimal

data class MetricData(
    val value: String?,
    val diff: BigDecimal?,
    val chartData: ChartData?,
    val type: MetricsType
)
