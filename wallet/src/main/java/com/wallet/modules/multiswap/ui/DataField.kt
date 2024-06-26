package com.wallet.modules.multiswap.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

interface DataField {
    @Composable
    fun GetContent(navController: NavController, borderTop: Boolean)
}
