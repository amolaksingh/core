package com.wallet.modules.balance.cex

import com.wallet.R
import com.wallet.core.AdapterState
import com.wallet.core.managers.CexAssetManager
import com.wallet.core.managers.ConnectivityManager
import com.wallet.core.providers.CexAsset
import com.wallet.core.providers.ICexProvider
import com.wallet.core.providers.Translator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class BalanceCexRepositoryWrapper(
    private val cexAssetManager: CexAssetManager,
    private val connectivityManager: ConnectivityManager
) {
    val itemsFlow = MutableStateFlow<Pair<List<CexAsset>?, AdapterState>>(Pair(null, AdapterState.Syncing(null)))

    private var cexProvider: ICexProvider? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var collectCexRepoItemsJob: Job? = null

    fun start() {
        coroutineScope.launch {
            connectivityManager.networkAvailabilityFlow.collect {
                refresh()
            }
        }
    }

    fun stop() {
        coroutineScope.cancel()
    }

    fun refresh() {
        collectCexRepoItemsJob?.cancel()
        val provider = cexProvider ?: return

        collectCexRepoItemsJob = coroutineScope.launch {
            try {
                cexAssetManager.saveAllForAccount(provider.getAssets(), provider.account)
                itemsFlow.update {
                    val assets = cexAssetManager.getAllForAccount(provider.account)
                        .filter { it.freeBalance > BigDecimal.ZERO || it.lockedBalance > BigDecimal.ZERO }
                    Pair(assets, AdapterState.Synced)
                }
            } catch (t: Throwable) {
                itemsFlow.update { (assets, _) ->
                    val adapterState = if (connectivityManager.isConnected) {
                        AdapterState.NotSynced(t)
                    } else {
                        AdapterState.NotSynced(Exception(Translator.getString(R.string.Hud_Text_NoInternet)))
                    }
                    Pair(assets, adapterState)
                }
            }
        }
    }

    fun setCexProvider(cexProvider: ICexProvider?) {
        this.cexProvider = cexProvider

        itemsFlow.update {
            cexProvider?.let { provider ->
                val assets = cexAssetManager.getAllForAccount(provider.account)
                    .filter { it.freeBalance > BigDecimal.ZERO || it.lockedBalance > BigDecimal.ZERO }
                Pair(assets, AdapterState.Synced)
            } ?: Pair(listOf(), AdapterState.NotSynced(Exception("No Provider")))
        }

        refresh()
    }
}