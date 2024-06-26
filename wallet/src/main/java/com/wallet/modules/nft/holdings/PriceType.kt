package com.wallet.modules.nft.holdings

import com.wallet.R
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.WithTranslatableTitle

enum class PriceType(override val title: TranslatableString) : WithTranslatableTitle {
    LastSale(TranslatableString.ResString(R.string.Nfts_PriceType_LastSale)),
    Days7(TranslatableString.ResString(R.string.Nfts_PriceType_Days_7)),
    Days30(TranslatableString.ResString(R.string.Nfts_PriceType_Days_30))
}