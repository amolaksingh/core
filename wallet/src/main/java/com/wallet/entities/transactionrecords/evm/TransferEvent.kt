package com.wallet.entities.transactionrecords.evm

import com.wallet.entities.TransactionValue

data class TransferEvent(
    val address: String?,
    val value: TransactionValue
)
