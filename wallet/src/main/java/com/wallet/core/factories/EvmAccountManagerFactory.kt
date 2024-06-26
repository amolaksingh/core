package com.wallet.core.factories

import com.wallet.core.IAccountManager
import com.wallet.core.IWalletManager
import com.wallet.core.managers.EvmAccountManager
import com.wallet.core.managers.EvmKitManager
import com.wallet.core.managers.MarketKitWrapper
import com.wallet.core.managers.TokenAutoEnableManager
import io.horizontalsystems.marketkit.models.BlockchainType

class EvmAccountManagerFactory(
    private val accountManager: IAccountManager,
    private val walletManager: IWalletManager,
    private val marketKit: MarketKitWrapper,
    private val tokenAutoEnableManager: TokenAutoEnableManager
) {

    fun evmAccountManager(blockchainType: BlockchainType, evmKitManager: EvmKitManager) =
        EvmAccountManager(
            blockchainType,
            accountManager,
            walletManager,
            marketKit,
            evmKitManager,
            tokenAutoEnableManager
        )

}
