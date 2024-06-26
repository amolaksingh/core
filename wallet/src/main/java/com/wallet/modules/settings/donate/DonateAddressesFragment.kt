package com.wallet.modules.settings.donate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.wallet.R
import com.wallet.core.App
import com.wallet.core.BaseComposeFragment
import com.wallet.core.imageUrl
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.stat
import com.wallet.core.title
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.ButtonSecondaryCircle
import com.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.wallet.ui.compose.components.HSpacer
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.InfoText
import com.wallet.ui.compose.components.RowUniversal
import com.wallet.ui.compose.components.VSpacer
import com.wallet.ui.compose.components.subhead2_leah
import com.wallet.ui.helpers.TextHelper
import com.core.helpers.HudHelper

class DonateAddressesFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        DonateScreen(
            onBackPress = { navController.popBackStack() }
        )
    }

}

@Composable
fun DonateScreen(
    onBackPress: () -> Unit
) {
    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Settings_Donate_Addresses),
                navigationIcon = {
                    HsBackButton(onClick = onBackPress)
                },
            )
        }
    ) {
        Column(Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                VSpacer(12.dp)
                App.appConfigProvider.donateAddresses.forEach { (blockchainType, address) ->
                    DonateAddress(
                        coinImageUrl = blockchainType.imageUrl,
                        coinName = blockchainType.title,
                        address = address,
                        chainUid = blockchainType.uid
                    )
                    VSpacer(24.dp)
                }

                VSpacer(8.dp)
            }
        }
    }
}

@Composable
private fun DonateAddress(
    coinImageUrl: String,
    coinName: String,
    address: String,
    chainUid: String
) {
    val localView = LocalView.current

    InfoText(text = coinName.uppercase())
    CellUniversalLawrenceSection() {
        RowUniversal(
            modifier = Modifier.padding(horizontal = 16.dp),
            onClick = {
                TextHelper.copyText(address)
                HudHelper.showSuccessMessage(localView, R.string.Hud_Text_Copied)

                stat(page = StatPage.DonateAddressList, event = StatEvent.CopyAddress(chainUid))
            }
        ) {
            Image(
                modifier = Modifier.size(32.dp),
                painter = rememberAsyncImagePainter(
                    model = coinImageUrl,
                    error = painterResource(R.drawable.ic_platform_placeholder_32)
                ),
                contentDescription = "platform"
            )
            HSpacer(16.dp)
            subhead2_leah(
                modifier = Modifier.weight(1f),
                text = address,
            )

            HSpacer(16.dp)
            ButtonSecondaryCircle(
                icon = R.drawable.ic_copy_20,
                contentDescription = stringResource(R.string.Button_Copy),
                onClick = {
                    TextHelper.copyText(address)
                    HudHelper.showSuccessMessage(localView, R.string.Hud_Text_Copied)

                    stat(page = StatPage.DonateAddressList, event = StatEvent.CopyAddress(chainUid))
                }
            )
        }
    }
}

@Preview
@Composable
fun DonateScreenPreview() {
    ComposeAppTheme {
        DonateScreen {}
    }
}