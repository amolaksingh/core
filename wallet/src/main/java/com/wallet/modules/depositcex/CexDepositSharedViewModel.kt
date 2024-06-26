package com.wallet.modules.depositcex

import androidx.lifecycle.ViewModel
import com.wallet.core.providers.CexAsset
import com.wallet.core.providers.CexDepositNetwork
import com.wallet.modules.receive.ui.UsedAddressesParams

class CexDepositSharedViewModel : ViewModel() {

    var network: CexDepositNetwork? = null
    var cexAsset: CexAsset? = null
    var usedAddressesParams: UsedAddressesParams? = null

}