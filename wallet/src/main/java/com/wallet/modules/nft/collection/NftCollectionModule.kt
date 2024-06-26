package com.wallet.modules.nft.collection

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.R
import com.wallet.core.App
import com.wallet.modules.nft.collection.overview.NftCollectionOverviewService
import com.wallet.modules.nft.collection.overview.NftCollectionOverviewViewModel
import com.wallet.modules.xrate.XRateService
import io.horizontalsystems.marketkit.models.BlockchainType

object NftCollectionModule {

    class Factory(
        private val blockchainType: BlockchainType,
        private val collectionUid: String
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val service = NftCollectionOverviewService(blockchainType, collectionUid, App.nftMetadataManager.provider(blockchainType), App.marketKit)
            return NftCollectionOverviewViewModel(
                service,
                App.numberFormatter,
                XRateService(App.marketKit, App.currencyManager.baseCurrency),
                App.marketKit
            ) as T
        }

    }

    enum class Tab(@StringRes val titleResId: Int) {
        Overview(R.string.NftCollection_Overview),
        Items(R.string.NftCollection_Items),
        Activity(R.string.NftCollection_Activity)
    }

}
