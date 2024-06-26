package com.wallet.modules.depositcex

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wallet.R
import com.wallet.core.imageUrl
import com.wallet.core.providers.CexDepositNetwork
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.Badge
import com.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.wallet.ui.compose.components.HSpacer
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.HsImage
import com.wallet.ui.compose.components.InfoText
import com.wallet.ui.compose.components.RowUniversal
import com.wallet.ui.compose.components.VSpacer
import com.wallet.ui.compose.components.body_leah

@Composable
fun SelectNetworkScreen(
    networks: List<CexDepositNetwork>,
    onSelectNetwork: (CexDepositNetwork) -> Unit,
    onNavigateBack: (() -> Unit),
) {
    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Cex_ChooseNetwork),
                navigationIcon = { HsBackButton(onClick = onNavigateBack) },
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            InfoText(text = stringResource(R.string.Cex_ChooseNetwork_Description))
            VSpacer(20.dp)
            CellUniversalLawrenceSection(networks) { cexNetwork ->
                NetworkCell(
                    item = cexNetwork,
                    onItemClick = {
                        onSelectNetwork.invoke(cexNetwork)
                    },
                )
            }
            VSpacer(32.dp)
        }
    }
}

@Composable
private fun NetworkCell(
    item: CexDepositNetwork,
    onItemClick: () -> Unit,
) {
    RowUniversal(
        onClick = if (item.enabled) onItemClick else null,
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalPadding = 0.dp
    ) {
        HsImage(
            url = item.blockchain?.type?.imageUrl,
            placeholder = R.drawable.ic_platform_placeholder_24,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(32.dp)
        )
        HSpacer(width = 16.dp)
        body_leah(
            modifier = Modifier.weight(1f),
            text = item.networkName,
            maxLines = 1,
        )
        HSpacer(width = 16.dp)
        if (item.enabled) {
            Icon(
                painter = painterResource(id = com.icons.R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = ComposeAppTheme.colors.grey
            )
        } else {
            Badge(text = stringResource(R.string.Suspended))
        }
    }
}
