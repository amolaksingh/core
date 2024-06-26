package com.wallet.modules.restoreaccount

import androidx.compose.runtime.Composable
import com.wallet.modules.restoreaccount.restoremenu.RestoreMenuModule.RestoreOption
import com.wallet.modules.restoreaccount.restoremenu.RestoreMenuViewModel
import com.wallet.modules.restoreaccount.restoremnemonic.RestorePhrase
import com.wallet.modules.restoreaccount.restoreprivatekey.RestorePrivateKey

@Composable
fun AdvancedRestoreScreen(
    restoreMenuViewModel: RestoreMenuViewModel,
    mainViewModel: RestoreViewModel,
    openSelectCoinsScreen: () -> Unit,
    openNonStandardRestore: () -> Unit,
    onBackClick: () -> Unit,
) {
    when (restoreMenuViewModel.restoreOption) {
        RestoreOption.RecoveryPhrase -> {
            RestorePhrase(
                advanced = true,
                restoreMenuViewModel = restoreMenuViewModel,
                mainViewModel = mainViewModel,
                openSelectCoins = openSelectCoinsScreen,
                openNonStandardRestore = openNonStandardRestore,
                onBackClick = onBackClick,
            )
        }
        RestoreOption.PrivateKey -> {
            RestorePrivateKey(
                restoreMenuViewModel = restoreMenuViewModel,
                mainViewModel = mainViewModel,
                openSelectCoinsScreen = openSelectCoinsScreen,
                onBackClick = onBackClick,
            )
        }
    }
}
