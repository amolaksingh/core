package com.wallet.modules.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.modules.info.ui.InfoBody
import com.wallet.modules.info.ui.InfoSubHeader
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.MenuItem

class TransactionStatusInfoFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        InfoScreen(
            navController
        )
    }

}

@Composable
private fun InfoScreen(
    navController: NavController
) {
    Surface(color = ComposeAppTheme.colors.tyler) {
        Column {
            AppBar(
                title = stringResource(R.string.TransactionInfo_Status),
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Close),
                        icon = R.drawable.ic_close,
                        onClick = { navController.popBackStack() }
                    )
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                InfoSubHeader(R.string.StatusInfo_Pending)
                InfoBody(R.string.StatusInfo_PendingDescription)
                InfoSubHeader(R.string.StatusInfo_Processing)
                InfoBody(R.string.StatusInfo_ProcessingDescription)
                InfoSubHeader(R.string.StatusInfo_Confirmed)
                InfoBody(R.string.StatusInfo_ConfirmedDescription)
                InfoSubHeader(R.string.StatusInfo_Failed)
                InfoBody(R.string.StatusInfo_FailedDescription)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
