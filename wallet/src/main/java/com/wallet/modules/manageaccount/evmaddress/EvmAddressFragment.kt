package com.wallet.modules.manageaccount.evmaddress

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.core.getInput
import com.wallet.core.managers.FaqManager
import com.wallet.core.stats.StatEntity
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.stat
import com.wallet.modules.manageaccount.ui.ActionButton
import com.wallet.modules.manageaccount.ui.HidableContent
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.MenuItem
import com.wallet.ui.helpers.TextHelper
import com.core.helpers.HudHelper
import kotlinx.parcelize.Parcelize

class EvmAddressFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        val evmAddress = navController.getInput<Input>()?.evmAddress ?: ""
        EvmAddressScreen(evmAddress, navController)
    }

    @Parcelize
    data class Input(val evmAddress: String) : Parcelable

}

@Composable
private fun EvmAddressScreen(evmAddress: String, navController: NavController) {
    val view = LocalView.current
    Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = stringResource(R.string.PublicKeys_EvmAddress),
            navigationIcon = {
                HsBackButton(onClick = navController::popBackStack)
            },
            menuItems = listOf(
                MenuItem(
                    title = TranslatableString.ResString(R.string.Info_Title),
                    icon = R.drawable.ic_info_24,
                    onClick = {
                        FaqManager.showFaqPage(navController, FaqManager.faqPathPrivateKeys)

                        stat(page = StatPage.EvmAddress, event = StatEvent.Open(StatPage.Info))
                    }
                )
            )
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(12.dp))
            HidableContent(evmAddress)
            Spacer(Modifier.height(24.dp))
        }
        ActionButton(R.string.Alert_Copy) {
            TextHelper.copyText(evmAddress)
            HudHelper.showSuccessMessage(view, R.string.Hud_Text_Copied)

            stat(page = StatPage.EvmAddress, event = StatEvent.Copy(StatEntity.EvmAddress))
        }
    }
}