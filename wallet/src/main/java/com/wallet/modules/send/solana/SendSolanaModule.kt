package com.wallet.modules.send.solana

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.core.App
import com.wallet.core.ISendSolanaAdapter
import com.wallet.core.adapters.SolanaAdapter
import com.wallet.core.isNative
import com.wallet.entities.Wallet
import com.wallet.modules.amount.AmountValidator
import com.wallet.modules.amount.SendAmountService
import com.wallet.modules.xrate.XRateService
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.solanakit.SolanaKit
import java.math.RoundingMode

object SendSolanaModule {

    class Factory(
        private val wallet: Wallet,
        private val predefinedAddress: String?,
    ) : ViewModelProvider.Factory {
        val adapter = (App.adapterManager.getAdapterForWallet(wallet) as? ISendSolanaAdapter) ?: throw IllegalStateException("SendSolanaAdapter is null")

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when (modelClass) {
                SendSolanaViewModel::class.java -> {
                    val amountValidator = AmountValidator()
                    val coinMaxAllowedDecimals = wallet.token.decimals

                    val amountService = SendAmountService(
                        amountValidator,
                        wallet.token.coin.code,
                        adapter.availableBalance.setScale(coinMaxAllowedDecimals, RoundingMode.DOWN),
                        wallet.token.type.isNative,
                    )
                    val solToken = App.coinManager.getToken(TokenQuery(BlockchainType.Solana, TokenType.Native)) ?: throw IllegalArgumentException()
                    val balance = App.solanaKitManager.solanaKitWrapper?.solanaKit?.balance ?: 0L
                    val solBalance = SolanaAdapter.balanceInBigDecimal(balance, solToken.decimals) - SolanaKit.accountRentAmount
                    val addressService = SendSolanaAddressService(predefinedAddress)
                    val xRateService = XRateService(App.marketKit, App.currencyManager.baseCurrency)

                    SendSolanaViewModel(
                        wallet,
                        wallet.token,
                        solToken,
                        solBalance,
                        adapter,
                        xRateService,
                        amountService,
                        addressService,
                        coinMaxAllowedDecimals,
                        App.contactsRepository,
                        predefinedAddress == null,
                        App.connectivityManager,
                    ) as T
                }

                else -> throw IllegalArgumentException()
            }
        }
    }

}