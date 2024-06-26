package com.wallet.core.managers

import com.wallet.core.IAccountCleaner
import com.wallet.core.adapters.BinanceAdapter
import com.wallet.core.adapters.BitcoinAdapter
import com.wallet.core.adapters.BitcoinCashAdapter
import com.wallet.core.adapters.DashAdapter
import com.wallet.core.adapters.ECashAdapter
import com.wallet.core.adapters.Eip20Adapter
import com.wallet.core.adapters.EvmAdapter
import com.wallet.core.adapters.SolanaAdapter
import com.wallet.core.adapters.TronAdapter
import com.wallet.core.adapters.zcash.ZcashAdapter

class AccountCleaner : IAccountCleaner {

    override fun clearAccounts(accountIds: List<String>) {
        accountIds.forEach { clearAccount(it) }
    }

    private fun clearAccount(accountId: String) {
        BinanceAdapter.clear(accountId)
        BitcoinAdapter.clear(accountId)
        BitcoinCashAdapter.clear(accountId)
        ECashAdapter.clear(accountId)
        DashAdapter.clear(accountId)
        EvmAdapter.clear(accountId)
        Eip20Adapter.clear(accountId)
        ZcashAdapter.clear(accountId)
        SolanaAdapter.clear(accountId)
        TronAdapter.clear(accountId)
    }

}
