package com.wallet.core.managers

import com.wallet.core.IAccountManager
import com.wallet.core.ILocalStorage
import com.wallet.core.IWalletManager
import com.core.IKeyStoreCleaner

class KeyStoreCleaner(
    private val localStorage: ILocalStorage,
    private val accountManager: IAccountManager,
    private val walletManager: IWalletManager
)
    : IKeyStoreCleaner {

    override var encryptedSampleText: String?
        get() = localStorage.encryptedSampleText
        set(value) {
            localStorage.encryptedSampleText = value
        }

    override fun cleanApp() {
        accountManager.clear()
        walletManager.clear()
        localStorage.clear()
    }
}
