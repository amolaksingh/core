package com.wallet.modules.restorelocal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.core.App
import com.wallet.core.stats.StatPage
import com.wallet.entities.AccountType
import com.wallet.entities.DataState
import com.wallet.modules.backuplocal.fullbackup.BackupViewItemFactory
import com.wallet.modules.backuplocal.fullbackup.SelectBackupItemsViewModel.OtherBackupViewItem
import com.wallet.modules.backuplocal.fullbackup.SelectBackupItemsViewModel.WalletBackupViewItem

object RestoreLocalModule {

    class Factory(
        private val backupJsonString: String?,
        private val fileName: String?,
        private val statPage: StatPage
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RestoreLocalViewModel(
                backupJsonString = backupJsonString,
                accountFactory = App.accountFactory,
                backupProvider = App.backupProvider,
                backupViewItemFactory = BackupViewItemFactory(),
                statPage = statPage,
                fileName = fileName
            ) as T
        }
    }

    data class UiState(
        val passphraseState: DataState.Error?,
        val showButtonSpinner: Boolean,
        val parseError: Exception?,
        val showSelectCoins: AccountType?,
        val manualBackup: Boolean,
        val restored: Boolean,
        var walletBackupViewItems: List<WalletBackupViewItem>,
        var otherBackupViewItems: List<OtherBackupViewItem>,
        val showBackupItems: Boolean
    )
}