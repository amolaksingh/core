package com.wallet.modules.balance.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.entities.AccountType
import com.wallet.modules.balance.BalanceAccountsViewModel
import com.wallet.modules.balance.BalanceModule
import com.wallet.modules.balance.BalanceScreenState
import com.wallet.modules.balance.cex.BalanceForAccountCex

@Composable
fun BalanceScreen(navController: NavController) {
    val viewModel = viewModel<BalanceAccountsViewModel>(factory = BalanceModule.AccountsFactory())

    when (val tmpAccount = viewModel.balanceScreenState) {
        BalanceScreenState.NoAccount -> BalanceNoAccount(navController)
        is BalanceScreenState.HasAccount -> when (tmpAccount.accountViewItem.type) {
            is AccountType.Cex -> {
                BalanceForAccountCex(navController, tmpAccount.accountViewItem)
            }

            else -> {
                BalanceForAccount(navController, tmpAccount.accountViewItem)
            }
        }

        else -> {}
    }
}