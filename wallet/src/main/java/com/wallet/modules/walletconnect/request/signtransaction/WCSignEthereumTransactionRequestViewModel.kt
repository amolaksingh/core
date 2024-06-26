package com.wallet.modules.walletconnect.request.signtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.R
import com.wallet.core.App
import com.wallet.core.ViewModelUiState
import com.wallet.core.ethereum.CautionViewItem
import com.wallet.core.ethereum.EvmCoinService
import com.wallet.core.ethereum.EvmCoinServiceFactory
import com.wallet.core.managers.EvmKitWrapper
import com.wallet.core.providers.Translator
import com.wallet.core.toHexString
import com.wallet.modules.evmfee.GasData
import com.wallet.modules.multiswap.ui.DataField
import com.wallet.modules.multiswap.ui.DataFieldNonce
import com.wallet.modules.send.SendModule
import com.wallet.modules.sendevmtransaction.SectionViewItem
import com.wallet.modules.sendevmtransaction.SendEvmTransactionViewItemFactory
import com.wallet.modules.sendevmtransaction.ValueType
import com.wallet.modules.sendevmtransaction.ViewItem
import com.wallet.modules.walletconnect.WCDelegate
import com.wallet.modules.walletconnect.WCSessionManager
import com.wallet.modules.walletconnect.request.sendtransaction.WalletConnectTransaction
import io.horizontalsystems.ethereumkit.models.TransactionData
import io.horizontalsystems.marketkit.models.BlockchainType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WCSignEthereumTransactionRequestViewModel(
    private val evmKit: EvmKitWrapper,
    baseCoinService: EvmCoinService,
    private val sendEvmTransactionViewItemFactory: SendEvmTransactionViewItemFactory,
    private val dAppName: String,
    transaction: WalletConnectTransaction
) : ViewModelUiState<WCSignEthereumTransactionRequestUiState>() {

    private val transactionData = TransactionData(
        transaction.to,
        transaction.value,
        transaction.data
    )

    private var gasData: GasData? = null
    private var nonce: Long? = null
    private var feeAmountData: SendModule.AmountData?
    private var fields: List<DataField>

    init {
        val gasPrice = transaction.getGasPriceObj()

        feeAmountData = if (gasPrice != null && transaction.gasLimit != null) {
            GasData(gasLimit = transaction.gasLimit, gasPrice = gasPrice).let {
                gasData = it
                baseCoinService.amountData(
                    it.estimatedFee,
                    it.isSurcharged
                )
            }
        } else {
            null
        }

        nonce = transaction.nonce

        fields = if (transaction.nonce != null) {
            listOf(DataFieldNonce(transaction.nonce))
        } else {
            emptyList()
        }
    }

    override fun createState() = WCSignEthereumTransactionRequestUiState(
        networkFee = feeAmountData,
        cautions = emptyList(),
        transactionFields = fields,
        sectionViewItems = getSectionViewItems()
    )

    private fun getSectionViewItems(): List<SectionViewItem> {
        val items = sendEvmTransactionViewItemFactory.getItems(
            transactionData,
            null,
            evmKit.evmKit.decorate(transactionData)
        ) + SectionViewItem(
            buildList {
                add(
                    ViewItem.Value(
                        Translator.getString(R.string.WalletConnect_SignMessageRequest_dApp),
                        dAppName,
                        ValueType.Regular
                    )
                )
            }
        )

        return items
    }

    suspend fun sign() = withContext(Dispatchers.Default) {
        val signer = evmKit.signer ?: throw WCSessionManager.RequestDataError.NoSigner
        val gasData = gasData ?: throw WCSessionManager.RequestDataError.InvalidGasPrice
        val nonce = nonce ?: throw WCSessionManager.RequestDataError.InvalidNonce

        val signature = signer.signedTransaction(
            address = transactionData.to,
            value = transactionData.value,
            transactionInput = transactionData.input,
            gasPrice = gasData.gasPrice,
            gasLimit = gasData.gasLimit,
            nonce = nonce
        )

        WCDelegate.sessionRequestEvent?.let { sessionRequest ->
            WCDelegate.respondPendingRequest(sessionRequest.request.id, sessionRequest.topic, signature.toHexString())
        }
    }

    fun reject() {
        WCDelegate.sessionRequestEvent?.let { sessionRequest ->
            WCDelegate.rejectRequest(sessionRequest.topic, sessionRequest.request.id)
        }
    }

    class Factory(
        private val blockchainType: BlockchainType,
        private val transaction: WalletConnectTransaction,
        private val peerName: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val token = App.evmBlockchainManager.getBaseToken(blockchainType)!!
            val evmKitWrapper = App.evmBlockchainManager.getEvmKitManager(blockchainType).evmKitWrapper!!
            val coinServiceFactory = EvmCoinServiceFactory(
                token,
                App.marketKit,
                App.currencyManager,
                App.coinManager
            )

            val sendEvmTransactionViewItemFactory = SendEvmTransactionViewItemFactory(
                App.evmLabelManager,
                coinServiceFactory,
                App.contactsRepository,
                blockchainType
            )


            return WCSignEthereumTransactionRequestViewModel(
                evmKitWrapper,
                coinServiceFactory.baseCoinService,
                sendEvmTransactionViewItemFactory,
                peerName,
                transaction
            ) as T
        }
    }
}

data class WCSignEthereumTransactionRequestUiState(
    val networkFee: SendModule.AmountData?,
    val cautions: List<CautionViewItem>,
    val transactionFields: List<DataField>,
    val sectionViewItems: List<SectionViewItem>
)
