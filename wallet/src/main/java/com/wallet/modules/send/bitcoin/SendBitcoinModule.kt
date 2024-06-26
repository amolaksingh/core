package com.wallet.modules.send.bitcoin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.core.App
import com.wallet.core.ISendBitcoinAdapter
import com.wallet.core.factories.FeeRateProviderFactory
import com.wallet.entities.Wallet
import com.wallet.modules.amount.AmountValidator
import com.wallet.modules.xrate.XRateService

object SendBitcoinModule {
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val wallet: Wallet,
        private val predefinedAddress: String?,
    ) : ViewModelProvider.Factory {
        val adapter =
            (App.adapterManager.getAdapterForWallet(wallet) as? ISendBitcoinAdapter) ?: throw IllegalStateException("SendBitcoinAdapter is null")

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val provider = FeeRateProviderFactory.provider(wallet.token.blockchainType)!!
            val feeService = SendBitcoinFeeService(adapter)
            val feeRateService = SendBitcoinFeeRateService(provider)
            val amountService = SendBitcoinAmountService(adapter, wallet.coin.code, AmountValidator())
            val addressService = SendBitcoinAddressService(adapter, predefinedAddress)
            val pluginService = SendBitcoinPluginService(wallet.token.blockchainType)
            return SendBitcoinViewModel(
                adapter,
                wallet,
                feeRateService,
                feeService,
                amountService,
                addressService,
                pluginService,
                XRateService(App.marketKit, App.currencyManager.baseCurrency),
                App.btcBlockchainManager,
                App.contactsRepository,
                predefinedAddress == null,
                App.localStorage
            ) as T
        }
    }

    data class UtxoData(
        val type: UtxoType? = null,
        val value: String = "0 / 0",
    )

    enum class UtxoType {
        Auto,
        Manual
    }

}