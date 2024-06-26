package com.wallet.entities.transactionrecords.solana

import com.wallet.entities.TransactionValue
import com.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.solanakit.models.Transaction

class SolanaOutgoingTransactionRecord(
        transaction: Transaction,
        baseToken: Token,
        source: TransactionSource,
        val to: String?,
        val value: TransactionValue,
        val sentToSelf: Boolean
): SolanaTransactionRecord(transaction, baseToken, source) {

    override val mainValue = value

}
