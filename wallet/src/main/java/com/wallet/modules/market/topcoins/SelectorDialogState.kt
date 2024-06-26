package com.wallet.modules.market.topcoins

import com.wallet.modules.market.SortingField
import com.wallet.ui.compose.Select

sealed class SelectorDialogState {
    object Closed : SelectorDialogState()
    class Opened(val select: Select<SortingField>) : SelectorDialogState()
}
