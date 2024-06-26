package com.wallet.core.managers

import android.os.Handler
import android.os.HandlerThread
import com.wallet.core.IAdapter
import com.wallet.core.IAdapterManager
import com.wallet.core.IBalanceAdapter
import com.wallet.core.IReceiveAdapter
import com.wallet.core.IWalletManager
import com.wallet.core.factories.AdapterFactory
import com.wallet.entities.Wallet
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import java.util.concurrent.ConcurrentHashMap

class AdapterManager(
    private val walletManager: IWalletManager,
    private val adapterFactory: AdapterFactory,
    private val btcBlockchainManager: BtcBlockchainManager,
    private val evmBlockchainManager: EvmBlockchainManager,
    private val binanceKitManager: BinanceKitManager,
    private val solanaKitManager: SolanaKitManager,
    private val tronKitManager: TronKitManager
) : IAdapterManager, HandlerThread("A") {

    private val handler: Handler
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val adaptersReadySubject = PublishSubject.create<Map<Wallet, IAdapter>>()
    private val adaptersMap = ConcurrentHashMap<Wallet, IAdapter>()

    override val adaptersReadyObservable: Flowable<Map<Wallet, IAdapter>> =
        adaptersReadySubject.toFlowable(BackpressureStrategy.BUFFER)

    init {
        start()
        handler = Handler(looper)
    }

    override fun startAdapterManager() {
        coroutineScope.launch {
            walletManager.activeWalletsUpdatedObservable.asFlow().collect { wallets ->
                initAdapters(wallets)
            }
        }
        coroutineScope.launch {
            btcBlockchainManager.restoreModeUpdatedObservable.asFlow().collect {
                handleUpdatedRestoreMode(it)
            }
        }
        coroutineScope.launch {
            solanaKitManager.kitStoppedObservable.asFlow().collect {
                handleUpdatedKit(BlockchainType.Solana)
            }
        }
        for (blockchain in evmBlockchainManager.allBlockchains) {
            coroutineScope.launch {
                evmBlockchainManager.getEvmKitManager(blockchain.type).evmKitUpdatedObservable.asFlow()
                    .collect {
                        handleUpdatedKit(blockchain.type)
                    }
            }
        }
    }

    private fun handleUpdatedKit(blockchainType: BlockchainType) {
        val wallets = adaptersMap.keys().toList().filter {
            it.token.blockchain.type == blockchainType
        }

        if (wallets.isEmpty()) return

        wallets.forEach {
            adaptersMap[it]?.stop()
            adaptersMap.remove(it)
        }

        initAdapters(walletManager.activeWallets)
    }

    private fun handleUpdatedRestoreMode(blockchainType: BlockchainType) {
        val wallets = adaptersMap.keys().toList().filter {
            it.token.blockchainType == blockchainType
        }

        if (wallets.isEmpty()) return

        wallets.forEach {
            adaptersMap[it]?.stop()
            adaptersMap.remove(it)
        }

        initAdapters(walletManager.activeWallets)
    }

    override fun refresh() {
        handler.post {
            adaptersMap.values.forEach { it.refresh() }
        }

        for (blockchain in evmBlockchainManager.allBlockchains) {
            evmBlockchainManager.getEvmKitManager(blockchain.type).evmKitWrapper?.evmKit?.refresh()
        }

        binanceKitManager.binanceKit?.refresh()
        solanaKitManager.solanaKitWrapper?.solanaKit?.refresh()
        tronKitManager.tronKitWrapper?.tronKit?.refresh()
    }

    @Synchronized
    private fun initAdapters(wallets: List<Wallet>) {
        val currentAdapters = adaptersMap.toMutableMap()
        adaptersMap.clear()

        wallets.forEach { wallet ->
            var adapter = currentAdapters.remove(wallet)
            if (adapter == null) {
                adapterFactory.getAdapterOrNull(wallet)?.let {
                    it.start()

                    adapter = it
                }
            }

            adapter?.let {
                adaptersMap[wallet] = it
            }
        }

        adaptersReadySubject.onNext(adaptersMap)

        currentAdapters.forEach { (wallet, adapter) ->
            adapter.stop()
            adapterFactory.unlinkAdapter(wallet)
        }
    }

    /**
     * Partial refresh of adapters
     * For the given list of wallets do:
     * - remove corresponding adapters from adaptersMap and stop them
     * - create new adapters, start and add them to adaptersMap
     * - trigger adaptersReadySubject
     */
    @Synchronized
    override fun refreshAdapters(wallets: List<Wallet>) {
        handler.post {
            val walletsToRefresh = wallets.filter { adaptersMap.containsKey(it) }

            //remove and stop adapters
            walletsToRefresh.forEach { wallet ->
                adaptersMap.remove(wallet)?.let { previousAdapter ->
                    previousAdapter.stop()
                    adapterFactory.unlinkAdapter(wallet)
                }
            }

            //add and start new adapters
            walletsToRefresh.forEach { wallet ->
                adapterFactory.getAdapterOrNull(wallet)?.let { adapter ->
                    adaptersMap[wallet] = adapter
                    adapter.start()
                }
            }

            adaptersReadySubject.onNext(adaptersMap)
        }
    }

    override fun refreshByWallet(wallet: Wallet) {
        val blockchain = evmBlockchainManager.getBlockchain(wallet.token)

        if (blockchain != null) {
            evmBlockchainManager.getEvmKitManager(blockchain.type).evmKitWrapper?.evmKit?.refresh()
        } else {
            adaptersMap[wallet]?.refresh()
        }
    }

    override fun getAdapterForWallet(wallet: Wallet): IAdapter? {
        return adaptersMap[wallet]
    }

    override fun getAdapterForToken(token: Token): IAdapter? {
        return walletManager.activeWallets.firstOrNull { it.token == token }
            ?.let { wallet ->
                adaptersMap[wallet]
            }
    }

    override fun getBalanceAdapterForWallet(wallet: Wallet): IBalanceAdapter? {
        return adaptersMap[wallet]?.let { it as? IBalanceAdapter }
    }

    override fun getReceiveAdapterForWallet(wallet: Wallet): IReceiveAdapter? {
        return adaptersMap[wallet]?.let { it as? IReceiveAdapter }
    }

}
