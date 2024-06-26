package com.wallet.modules.send.binance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.core.App
import com.wallet.core.ISendBinanceAdapter
import com.wallet.entities.Wallet
import com.wallet.modules.amount.AmountValidator
import com.wallet.modules.amount.SendAmountService
import com.wallet.modules.xrate.XRateService

object SendBinanceModule {

    class Factory(
        private val wallet: Wallet,
        private val predefinedAddress: String?,
    ) : ViewModelProvider.Factory {
        val adapter = (App.adapterManager.getAdapterForWallet(wallet) as? ISendBinanceAdapter) ?: throw IllegalStateException("SendBinanceAdapter is null")

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val amountValidator = AmountValidator()
            val amountService = SendAmountService(amountValidator, wallet.coin.code, adapter.availableBalance)
            val addressService = SendBinanceAddressService(adapter, predefinedAddress)
            val feeService = SendBinanceFeeService(adapter, wallet.token, App.feeCoinProvider)
            val xRateService = XRateService(App.marketKit, App.currencyManager.baseCurrency)

            return SendBinanceViewModel(
                wallet,
                adapter,
                amountService,
                addressService,
                feeService,
                xRateService,
                App.contactsRepository,
                predefinedAddress == null,
            ) as T
        }

    }

}
