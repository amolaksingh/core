package com.wallet.modules.restorelocal

import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.wallet.R
import com.wallet.core.IAccountFactory
import com.wallet.core.ViewModelUiState
import com.wallet.core.providers.Translator
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.stat
import com.wallet.core.stats.statAccountType
import com.wallet.entities.AccountType
import com.wallet.entities.DataState
import com.wallet.modules.backuplocal.BackupLocalModule.WalletBackup
import com.wallet.modules.backuplocal.fullbackup.BackupProvider
import com.wallet.modules.backuplocal.fullbackup.BackupViewItemFactory
import com.wallet.modules.backuplocal.fullbackup.DecryptedFullBackup
import com.wallet.modules.backuplocal.fullbackup.FullBackup
import com.wallet.modules.backuplocal.fullbackup.RestoreException
import com.wallet.modules.backuplocal.fullbackup.SelectBackupItemsViewModel.OtherBackupViewItem
import com.wallet.modules.backuplocal.fullbackup.SelectBackupItemsViewModel.WalletBackupViewItem
import com.wallet.modules.restorelocal.RestoreLocalModule.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RestoreLocalViewModel(
    private val backupJsonString: String?,
    private val accountFactory: IAccountFactory,
    private val backupProvider: BackupProvider,
    private val backupViewItemFactory: BackupViewItemFactory,
    private val statPage: StatPage,
    fileName: String?,
) : ViewModelUiState<UiState>() {

    private var passphrase = ""
    private var passphraseState: DataState.Error? = null
    private var showButtonSpinner = false
    private var walletBackup: WalletBackup? = null
    private var fullBackup: FullBackup? = null
    private var parseError: Exception? = null
    private var showSelectCoins: AccountType? = null
    private var manualBackup = false
    private var restored = false

    private var decryptedFullBackup: DecryptedFullBackup? = null
    private var walletBackupViewItems: List<WalletBackupViewItem> = emptyList()
    private var otherBackupViewItems: List<OtherBackupViewItem> = emptyList()
    private var showBackupItems = false

    val accountName by lazy {
        fileName?.let { name ->
            return@lazy name
                .replace(".json", "")
                .replace("UW_Backup_", "")
                .replace("_", " ")
        }
        accountFactory.getNextAccountName()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gson = GsonBuilder()
                    .disableHtmlEscaping()
                    .enableComplexMapKeySerialization()
                    .create()

                fullBackup = try {
                    val backup = gson.fromJson(backupJsonString, FullBackup::class.java)
                    backup.settings.language // if single walletBackup it will throw exception
                    backup
                } catch (ex: Exception) {
                    null
                }

                walletBackup = gson.fromJson(backupJsonString, WalletBackup::class.java)
                manualBackup = walletBackup?.manualBackup ?: false
            } catch (e: Exception) {
                parseError = e
                emitState()
            }
        }
    }

    override fun createState() = UiState(
        passphraseState = passphraseState,
        showButtonSpinner = showButtonSpinner,
        parseError = parseError,
        showSelectCoins = showSelectCoins,
        manualBackup = manualBackup,
        restored = restored,
        walletBackupViewItems = walletBackupViewItems,
        otherBackupViewItems = otherBackupViewItems,
        showBackupItems = showBackupItems
    )

    fun onChangePassphrase(v: String) {
        passphrase = v
        passphraseState = null
        emitState()
    }

    fun onImportClick() {
        when {
            fullBackup != null -> {
                fullBackup?.let { showFullBackupItems(it) }
            }

            walletBackup != null -> {
                walletBackup?.let { restoreSingleWallet(it, accountName) }
            }
        }
    }

    private fun showFullBackupItems(it: FullBackup): Job {
        showButtonSpinner = true
        emitState()

        return viewModelScope.launch(Dispatchers.IO) {
            try {
                val decrypted = backupProvider.decryptedFullBackup(it, passphrase)
                val backupItems = backupProvider.fullBackupItems(decrypted)
                val backupViewItems = backupViewItemFactory.backupViewItems(backupItems)

                walletBackupViewItems = backupViewItems.first
                otherBackupViewItems = backupViewItems.second
                decryptedFullBackup = decrypted
                showBackupItems = true
            } catch (keyException: RestoreException.EncryptionKeyException) {
                parseError = keyException
            } catch (invalidPassword: RestoreException.InvalidPasswordException) {
                passphraseState = DataState.Error(Exception(Translator.getString(R.string.ImportBackupFile_Error_InvalidPassword)))
            } catch (e: Exception) {
                parseError = e
            }

            withContext(Dispatchers.Main) {
                showButtonSpinner = false
                emitState()
            }
        }
    }

    fun shouldShowReplaceWarning(): Boolean {
        return backupProvider.shouldShowReplaceWarning(decryptedFullBackup)
    }

    fun restoreFullBackup() {
        decryptedFullBackup?.let { restoreFullBackup(it) }
    }

    private fun restoreFullBackup(decryptedFullBackup: DecryptedFullBackup) {
        showButtonSpinner = true
        emitState()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                backupProvider.restoreFullBackup(decryptedFullBackup, passphrase)
                restored = true

                stat(page = statPage, event = StatEvent.ImportFull)
            } catch (keyException: RestoreException.EncryptionKeyException) {
                parseError = keyException
            } catch (invalidPassword: RestoreException.InvalidPasswordException) {
                passphraseState = DataState.Error(Exception(Translator.getString(R.string.ImportBackupFile_Error_InvalidPassword)))
            } catch (e: Exception) {
                parseError = e
            }

            showButtonSpinner = false
            withContext(Dispatchers.Main) {
                emitState()
            }
        }
    }

    @Throws
    private fun restoreSingleWallet(backup: WalletBackup, accountName: String) {
        showButtonSpinner = true
        emitState()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val type = backupProvider.accountType(backup, passphrase)
                if (type is AccountType.Cex) {
                    backupProvider.restoreCexAccount(type, accountName)
                    restored = true
                } else if (backup.enabledWallets.isNullOrEmpty()) {
                    showSelectCoins = type
                } else {
                    backupProvider.restoreSingleWalletBackup(type, accountName, backup)
                    restored = true
                }

                stat(page = statPage, event = StatEvent.ImportWallet(type.statAccountType))
            } catch (keyException: RestoreException.EncryptionKeyException) {
                parseError = keyException
            } catch (invalidPassword: RestoreException.InvalidPasswordException) {
                passphraseState = DataState.Error(Exception(Translator.getString(R.string.ImportBackupFile_Error_InvalidPassword)))
            } catch (e: Exception) {
                parseError = e
            }
            showButtonSpinner = false
            withContext(Dispatchers.Main) {
                emitState()
            }
        }
    }

    fun onSelectCoinsShown() {
        showSelectCoins = null
        emitState()
    }

    fun onBackupItemsShown() {
        showBackupItems = false
        emitState()
    }

}
