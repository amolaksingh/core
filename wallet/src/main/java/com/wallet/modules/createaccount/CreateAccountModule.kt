package com.wallet.modules.createaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.R
import com.wallet.core.App
import com.wallet.core.managers.PassphraseValidator
import com.wallet.core.providers.PredefinedBlockchainSettingsProvider
import com.wallet.core.providers.Translator

object CreateAccountModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateAccountViewModel(
                App.accountFactory,
                App.wordsManager,
                App.accountManager,
                App.walletActivator,
                PassphraseValidator(),
                PredefinedBlockchainSettingsProvider(
                    App.restoreSettingsManager,
                    App.zcashBirthdayProvider
                )
            ) as T
        }
    }

    enum class Kind(val wordsCount: Int) {
        Mnemonic12(12),
        Mnemonic15(15),
        Mnemonic18(18),
        Mnemonic21(21),
        Mnemonic24(24);

        val title = Translator.getString(R.string.CreateWallet_N_Words, wordsCount)

        val titleLong: String
            get() = if (this == Mnemonic12) Translator.getString(R.string.CreateWallet_N_WordsRecommended, wordsCount)
            else title
    }
}
