package com.wallet.modules.markdown

sealed class LoadStatus {
    object Initial : LoadStatus()
    object Loading : LoadStatus()
    object Loaded : LoadStatus()
    class Failed(val e: Throwable) : LoadStatus()
}
