package com.wallet.modules.chart

import com.chartview.models.ChartIndicator
import io.horizontalsystems.marketkit.models.ChartPoint


data class ChartPointsWrapper(
    val items: List<ChartPoint>,
    val isMovementChart: Boolean = true,
    val indicators: Map<String, ChartIndicator> = mapOf(),
)
