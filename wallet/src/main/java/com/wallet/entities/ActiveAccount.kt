package com.wallet.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ActiveAccount(
    @PrimaryKey
    val level: Int,
    val accountId: String,
)
