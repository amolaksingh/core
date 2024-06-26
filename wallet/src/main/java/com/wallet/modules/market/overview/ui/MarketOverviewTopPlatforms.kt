package com.wallet.modules.market.overview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.wallet.modules.market.TimeDuration
import com.wallet.modules.market.overview.MarketOverviewModule
import com.wallet.modules.market.topplatforms.Platform
import com.wallet.modules.market.topplatforms.TopPlatformItem
import com.wallet.ui.compose.ComposeAppTheme

@Composable
fun TopPlatformsBoardView(
    board: MarketOverviewModule.TopPlatformsBoard,
    onSelectTimeDuration: (TimeDuration) -> Unit,
    onItemClick: (Platform) -> Unit,
    onClickSeeAll: () -> Unit
) {
    TopBoardHeader(
        title = board.title,
        iconRes = board.iconRes,
        select = board.timeDurationSelect,
        onSelect = onSelectTimeDuration,
        onClickSeeAll = onClickSeeAll
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ComposeAppTheme.colors.lawrence)
    ) {
        board.items.forEach {
            TopPlatformItem(it, onItemClick)
        }

        SeeAllButton(onClickSeeAll)
    }

    Spacer(modifier = Modifier.height(24.dp))
}
