package com.wallet.entities.transactionrecords.evm

import com.wallet.core.managers.SpamManager
import com.wallet.entities.TransactionValue
import com.wallet.modules.transactions.TransactionSource
import io.horizontalsystems.ethereumkit.models.Transaction
import io.horizontalsystems.marketkit.models.Token

class EvmIncomingTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    spamManager: SpamManager,
    val from: String,
    val value: TransactionValue
) : EvmTransactionRecord(
    transaction = transaction,
    baseToken = baseToken,
    source = source,
    foreignTransaction = true,
    spam = spamManager.isIncomingSpam(value)
) {

    override val mainValue = value

}
