package com.wallet.modules.walletconnect.request

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.modules.walletconnect.request.sendtransaction.WCSendEthereumTransactionRequestViewModel

class WCEvmTransactionSettingsFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        WCEvmTransactionSettingsScreen(navController)
    }
}

@Composable
fun WCEvmTransactionSettingsScreen(navController: NavController) {
    val viewModelStoreOwner = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(R.id.wcRequestFragment)
    }

    val viewModel = viewModel<WCSendEthereumTransactionRequestViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
    )

    val sendTransactionService = viewModel.sendTransactionService

    sendTransactionService.GetSettingsContent(navController)
}
