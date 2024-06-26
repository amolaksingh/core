package com.wallet.modules.restoreaccount.restoreblockchains

import com.wallet.core.Clearable
import com.wallet.core.IAccountFactory
import com.wallet.core.IAccountManager
import com.wallet.core.IWalletManager
import com.wallet.core.isDefault
import com.wallet.core.managers.MarketKitWrapper
import com.wallet.core.managers.RestoreSettings
import com.wallet.core.managers.TokenAutoEnableManager
import com.wallet.core.nativeTokenQueries
import com.wallet.core.order
import com.wallet.core.restoreSettingTypes
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.stat
import com.wallet.core.stats.statAccountType
import com.wallet.core.supported
import com.wallet.core.supports
import com.wallet.entities.AccountOrigin
import com.wallet.entities.AccountType
import com.wallet.entities.Wallet
import com.wallet.modules.enablecoin.blockchaintokens.BlockchainTokensService
import com.wallet.modules.enablecoin.restoresettings.RestoreSettingsService
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class RestoreBlockchainsService(
    private val accountName: String,
    private val accountType: AccountType,
    private val manualBackup: Boolean,
    private val fileBackup: Boolean,
    private val accountFactory: IAccountFactory,
    private val accountManager: IAccountManager,
    private val walletManager: IWalletManager,
    private val marketKit: MarketKitWrapper,
    private val tokenAutoEnableManager: TokenAutoEnableManager,
    private val blockchainTokensService: BlockchainTokensService,
    private val restoreSettingsService: RestoreSettingsService,
    private val statPage: StatPage
) : Clearable {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var tokens = listOf<Token>()
    private val enabledTokens = mutableListOf<Token>()

    private var restoreSettingsMap = mutableMapOf<Token, RestoreSettings>()

    val cancelEnableBlockchainObservable = PublishSubject.create<Blockchain>()
    val canRestore = BehaviorSubject.createDefault(false)

    val itemsObservable = BehaviorSubject.create<List<Item>>()
    var items: List<Item> = listOf()
        private set(value) {
            field = value
            itemsObservable.onNext(value)
        }

    init {
        coroutineScope.launch {
            blockchainTokensService.approveTokensObservable.asFlow().collect {
                handleApproveTokens(it.blockchain, it.tokens)
            }
        }
        coroutineScope.launch {
            blockchainTokensService.rejectApproveTokensObservable.asFlow().collect {
                handleCancelEnable(it)
            }
        }
        coroutineScope.launch {
            restoreSettingsService.approveSettingsObservable.asFlow().collect {
                handleApproveRestoreSettings(it.token, it.settings)
            }
        }
        coroutineScope.launch {
            restoreSettingsService.rejectApproveSettingsObservable.asFlow().collect {
                handleCancelEnable(it.blockchain)
            }
        }

        syncInternalItems()
        syncState()
    }

    private fun syncInternalItems() {
        val allowedBlockchainTypes = BlockchainType.supported.filter { it.supports(accountType) }
        val tokenQueries = allowedBlockchainTypes
            .map { it.nativeTokenQueries }
            .flatten()

        tokens = marketKit.tokens(tokenQueries)
            .filter { it.supports(accountType) }
            .sortedBy { it.type.order }
    }

    private fun handleApproveTokens(blockchain: Blockchain, tokens: List<Token>) {
        val existingTokens = enabledTokens.filter { it.blockchain == blockchain }

        val newTokens = tokens.minus(existingTokens)
        val removedTokens = existingTokens.minus(tokens)

        enabledTokens.addAll(newTokens)
        enabledTokens.removeAll(removedTokens)

        syncCanRestore()
        syncState()
    }
    private fun handleApproveRestoreSettings(
        token: Token,
        restoreSettings: RestoreSettings
    ) {
        if (restoreSettings.isNotEmpty()) {
            restoreSettingsMap[token] = restoreSettings
        }

        enabledTokens.add(token)

        syncCanRestore()
        syncState()
    }

    private fun handleCancelEnable(blockchain: Blockchain) {
        if (!isEnabled(blockchain)) {
            cancelEnableBlockchainObservable.onNext(blockchain)
        }
    }

    private fun isEnabled(blockchain: Blockchain): Boolean {
        return enabledTokens.any { it.blockchain == blockchain }
    }

    private fun item(blockchain: Blockchain): Item {
        val enabled = isEnabled(blockchain)
        val hasSettings = enabled && hasSettings(blockchain)
        return Item(blockchain, enabled, hasSettings)
    }

    private fun hasSettings(blockchain: Blockchain): Boolean {
        return tokens.count { it.blockchain == blockchain } > 1
    }

    private fun syncState() {
        val blockchains = tokens.map { it.blockchain }.toSet()
        items = blockchains.sortedBy { it.type.order }.map { item(it) }
    }

    private fun syncCanRestore() {
        canRestore.onNext(enabledTokens.isNotEmpty())
    }

    fun enable(blockchain: Blockchain) {
        val tokens = tokens.filter { it.blockchain == blockchain }
        val token = tokens.firstOrNull() ?: return

        if (tokens.size == 1) {
            if (token.blockchainType.restoreSettingTypes.isNotEmpty()) {
                restoreSettingsService.approveSettings(token)
            } else {
                handleApproveRestoreSettings(token, RestoreSettings())
            }
        } else {
            blockchainTokensService.approveTokens(blockchain, tokens, tokens.filter { it.type.isDefault })
        }
    }

    fun disable(blockchain: Blockchain) {
        enabledTokens.removeIf { it.blockchain == blockchain }

        syncState()
        syncCanRestore()
    }

    fun configure(blockchain: Blockchain) {
        val tokens = tokens.filter { it.blockchain == blockchain }
        if (tokens.isEmpty()) return

        val enabledTokens = enabledTokens.filter { it.blockchain == blockchain }

        blockchainTokensService.approveTokens(blockchain, tokens, enabledTokens, true)
    }

    fun restore() {
        val account = accountFactory.account(
            accountName,
            accountType,
            AccountOrigin.Restored,
            manualBackup,
            fileBackup,
        )
        accountManager.save(account)

        restoreSettingsMap.forEach { (token, settings) ->
            restoreSettingsService.save(settings, account, token.blockchainType)
        }

        items.filter { it.enabled }.forEach { item ->
            tokenAutoEnableManager.markAutoEnable(account, item.blockchain.type)
        }

        if (enabledTokens.isEmpty()) return

        val wallets = enabledTokens.map { Wallet(it, account) }
        walletManager.save(wallets)

        stat(page = statPage, event = StatEvent.ImportWallet(accountType.statAccountType))
    }

    override fun clear() {
        coroutineScope.cancel()
    }

    data class Item(
        val blockchain: Blockchain,
        val enabled: Boolean,
        val hasSettings: Boolean
    )

}
