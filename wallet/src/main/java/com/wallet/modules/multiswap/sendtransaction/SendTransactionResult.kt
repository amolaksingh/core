package com.wallet.modules.multiswap.sendtransaction

import io.horizontalsystems.ethereumkit.models.FullTransaction

sealed class SendTransactionResult {
    data class Evm(val fullTransaction: FullTransaction) : SendTransactionResult()
}
