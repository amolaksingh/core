package com.wallet.modules.receive.viewmodels

import androidx.lifecycle.ViewModel
import com.wallet.core.App
import com.wallet.entities.Account
import com.wallet.entities.Wallet
import com.wallet.modules.receive.ui.UsedAddressesParams
import io.horizontalsystems.marketkit.models.FullCoin

class ReceiveSharedViewModel : ViewModel() {

    var wallet: Wallet? = null
    var coinUid: String? = null
    var usedAddressesParams: UsedAddressesParams? = null

    val activeAccount: Account?
        get() = App.accountManager.activeAccount

    fun fullCoin(): FullCoin? {
        val coinUid = coinUid ?: return null
        return App.marketKit.fullCoins(listOf(coinUid)).firstOrNull()
    }

}