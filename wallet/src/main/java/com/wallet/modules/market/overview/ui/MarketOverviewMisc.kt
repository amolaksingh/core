package com.wallet.modules.market.overview.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wallet.R
import com.wallet.ui.compose.components.RowUniversal
import com.wallet.ui.compose.components.body_leah

@Composable
fun SeeAllButton(onClick: () -> Unit) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = onClick
    ) {
        body_leah(
            text = stringResource(R.string.Market_SeeAll),
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(id = com.icons.R.drawable.ic_arrow_right),
            contentDescription = "right arrow icon",
        )
    }
}
