package com.wallet.core.adapters

import com.wallet.core.IAdapter
import com.wallet.core.IBalanceAdapter
import com.wallet.core.IReceiveAdapter
import com.wallet.core.managers.SolanaKitWrapper
import io.horizontalsystems.solanakit.Signer

abstract class BaseSolanaAdapter(
        solanaKitWrapper: SolanaKitWrapper,
        val decimal: Int
) : IAdapter, IBalanceAdapter, IReceiveAdapter {

    val solanaKit = solanaKitWrapper.solanaKit
    protected val signer: Signer? = solanaKitWrapper.signer

    override val debugInfo: String
        get() = solanaKit.debugInfo()

    val statusInfo: Map<String, Any>
        get() = solanaKit.statusInfo()

    // IReceiveAdapter

    override val receiveAddress: String
        get() = solanaKit.receiveAddress

    override val isMainNet: Boolean
        get() = solanaKit.isMainnet

    companion object {
        const val confirmationsThreshold: Int = 12
    }

}
