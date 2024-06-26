package com.wallet.modules.chart

import com.chartview.ChartViewType
import com.wallet.entities.ViewState
import com.wallet.modules.coin.ChartInfoData
import com.wallet.ui.compose.components.TabItem
import io.horizontalsystems.marketkit.models.HsTimePeriod

data class ChartUiState(
    val tabItems: List<TabItem<HsTimePeriod?>>,
    val chartHeaderView: ChartModule.ChartHeaderView?,
    val chartInfoData: ChartInfoData?,
    val loading: Boolean,
    val viewState: ViewState,
    val hasVolumes: Boolean,
    val chartViewType: ChartViewType,
)
