package com.wallet.entities.transactionrecords.evm

import com.wallet.entities.TransactionValue
import com.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.ethereumkit.models.Transaction
import io.horizontalsystems.marketkit.models.Token

class SwapTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val exchangeAddress: String,
    val amountIn: Amount,
    val amountOut: Amount?,
    val recipient: String?
) : EvmTransactionRecord(transaction, baseToken, source) {

    sealed class Amount(val value: TransactionValue) {
        class Exact(value: TransactionValue) : Amount(value)
        class Extremum(value: TransactionValue) : Amount(value)
    }

    val valueIn: TransactionValue
        get() = amountIn.value

    val valueOut: TransactionValue?
        get() = amountOut?.value

}
