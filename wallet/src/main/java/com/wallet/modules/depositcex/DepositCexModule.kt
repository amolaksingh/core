package com.wallet.modules.depositcex

import com.wallet.core.providers.CexAsset
import com.wallet.modules.market.ImageSource

object DepositCexModule {

    data class CexCoinViewItem(
        val title: String,
        val subtitle: String,
        val coinIconUrl: String?,
        val alternativeCoinUrl: String?,
        val coinIconPlaceholder: Int,
        val cexAsset: CexAsset,
        val depositEnabled: Boolean,
        val withdrawEnabled: Boolean,
    )

    data class NetworkViewItem(
        val title: String,
        val imageSource: ImageSource,
    )

}
