package com.wallet.modules.transactionInfo

import com.wallet.R
import com.wallet.core.IAppNumberFormatter
import com.wallet.core.adapters.TonTransactionRecord
import com.wallet.core.isCustom
import com.wallet.core.managers.EvmLabelManager
import com.wallet.core.providers.Translator
import com.wallet.core.stats.StatSection
import com.wallet.entities.CurrencyValue
import com.wallet.entities.LastBlockInfo
import com.wallet.entities.TransactionValue
import com.wallet.entities.nft.NftAssetBriefMetadata
import com.wallet.entities.nft.NftUid
import com.wallet.entities.transactionrecords.TransactionRecord
import com.wallet.entities.transactionrecords.binancechain.BinanceChainIncomingTransactionRecord
import com.wallet.entities.transactionrecords.binancechain.BinanceChainOutgoingTransactionRecord
import com.wallet.entities.transactionrecords.bitcoin.BitcoinIncomingTransactionRecord
import com.wallet.entities.transactionrecords.bitcoin.BitcoinOutgoingTransactionRecord
import com.wallet.entities.transactionrecords.bitcoin.BitcoinTransactionRecord
import com.wallet.entities.transactionrecords.bitcoin.TransactionLockState
import com.wallet.entities.transactionrecords.evm.ApproveTransactionRecord
import com.wallet.entities.transactionrecords.evm.ContractCallTransactionRecord
import com.wallet.entities.transactionrecords.evm.ContractCreationTransactionRecord
import com.wallet.entities.transactionrecords.evm.EvmIncomingTransactionRecord
import com.wallet.entities.transactionrecords.evm.EvmOutgoingTransactionRecord
import com.wallet.entities.transactionrecords.evm.EvmTransactionRecord
import com.wallet.entities.transactionrecords.evm.ExternalContractCallTransactionRecord
import com.wallet.entities.transactionrecords.evm.SwapTransactionRecord
import com.wallet.entities.transactionrecords.evm.UnknownSwapTransactionRecord
import com.wallet.entities.transactionrecords.solana.SolanaIncomingTransactionRecord
import com.wallet.entities.transactionrecords.solana.SolanaOutgoingTransactionRecord
import com.wallet.entities.transactionrecords.solana.SolanaUnknownTransactionRecord
import com.wallet.entities.transactionrecords.tron.TronApproveTransactionRecord
import com.wallet.entities.transactionrecords.tron.TronContractCallTransactionRecord
import com.wallet.entities.transactionrecords.tron.TronExternalContractCallTransactionRecord
import com.wallet.entities.transactionrecords.tron.TronIncomingTransactionRecord
import com.wallet.entities.transactionrecords.tron.TronOutgoingTransactionRecord
import com.wallet.entities.transactionrecords.tron.TronTransactionRecord
import com.wallet.modules.contacts.ContactsRepository
import com.wallet.modules.contacts.model.Contact
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.Address
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.Amount
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.ContactItem
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.DoubleSpend
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.Explorer
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.LockState
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.NftAmount
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.RawTransaction
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.SentToSelf
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.SpeedUpCancel
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.Status
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.Transaction
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.TransactionHash
import com.wallet.modules.transactionInfo.TransactionInfoViewItem.Value
import com.wallet.modules.transactions.TransactionStatus
import com.wallet.modules.transactions.TransactionViewItem
import com.core.helpers.DateHelper
import io.horizontalsystems.marketkit.models.BlockchainType
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import kotlin.math.min

class TransactionInfoViewItemFactory(
    private val numberFormatter: IAppNumberFormatter,
    private val translator: Translator,
    private val dateHelper: DateHelper,
    private val evmLabelManager: EvmLabelManager,
    private val resendEnabled: Boolean,
    private val contactsRepo: ContactsRepository,
    private val blockchainType: BlockchainType
) {
    private val zeroAddress = "0x0000000000000000000000000000000000000000"

    fun getViewItemSections(transactionItem: TransactionInfoItem): List<List<TransactionInfoViewItem>> {
        val transaction = transactionItem.record
        val rates = transactionItem.rates
        val nftMetadata = transactionItem.nftMetadata

        val status = transaction.status(transactionItem.lastBlockInfo?.height)
        val itemSections = mutableListOf<List<TransactionInfoViewItem>>()
        val miscItemsSection = mutableListOf<TransactionInfoViewItem>()

        var sentToSelf = false

        if (transactionItem.record.spam) {
            itemSections.add(listOf(TransactionInfoViewItem.WarningMessage(translator.getString(R.string.TransactionInfo_SpamWarning))))
        }

        when (transaction) {
            is ContractCreationTransactionRecord -> {
                itemSections.add(getContractCreationItems(transaction))
            }

            is TonTransactionRecord -> {
                when (transaction.type) {
                    TonTransactionRecord.Type.Incoming -> {
                        transaction.transfers.forEach { transfer ->
                            itemSections.add(
                                getReceiveSectionItems(
                                    value = transfer.amount,
                                    fromAddress = transfer.src,
                                    coinPrice = rates[transfer.amount.coinUid],
                                    hideAmount = transactionItem.hideAmount,
                                )
                            )
                        }
                    }
                    TonTransactionRecord.Type.Outgoing -> {
                        transaction.transfers.forEach { transfer ->
                            itemSections.add(
                                getSendSectionItems(
                                    value = transfer.amount,
                                    toAddress = transfer.dest,
                                    coinPrice = rates[transfer.amount.coinUid],
                                    hideAmount = transactionItem.hideAmount,
                                )
                            )
                        }
                    }
                    TonTransactionRecord.Type.Unknown -> {
                    }
                }
                addMemoItem(transaction.memo, miscItemsSection)
            }
            is EvmIncomingTransactionRecord ->
                itemSections.add(
                    getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                    )
                )

            is TronIncomingTransactionRecord ->
                itemSections.add(
                    getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                    )
                )

            is EvmOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                        nftMetadata = nftMetadata
                    )
                )
            }

            is TronOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                        nftMetadata = nftMetadata
                    )
                )
            }

            is SwapTransactionRecord -> {
                itemSections.add(
                    getSwapEventSectionItems(
                        valueIn = transaction.valueIn,
                        valueOut = transaction.valueOut,
                        rates = rates,
                        amount = transaction.amountIn,
                        hideAmount = transactionItem.hideAmount,
                        hasRecipient = transaction.recipient != null
                    )
                )

                itemSections.add(
                    getSwapDetailsSectionItems(
                        rates,
                        transaction.exchangeAddress,
                        transaction.valueOut,
                        transaction.valueIn
                    )
                )
            }

            is UnknownSwapTransactionRecord -> {
                itemSections.add(
                    getSwapEventSectionItems(
                        valueIn = transaction.valueIn,
                        valueOut = transaction.valueOut,
                        amount = null,
                        rates = rates,
                        hideAmount = transactionItem.hideAmount,
                        hasRecipient = false
                    )
                )

                itemSections.add(
                    getSwapDetailsSectionItems(
                        rates,
                        transaction.exchangeAddress,
                        transaction.valueOut,
                        transaction.valueIn,
                    )
                )
            }

            is ApproveTransactionRecord ->
                itemSections.add(
                    getApproveSectionItems(
                        value = transaction.value,
                        coinPrice = rates[transaction.value.coinUid],
                        spenderAddress = transaction.spender,
                        hideAmount = transactionItem.hideAmount,
                    )
                )

            is TronApproveTransactionRecord ->
                itemSections.add(
                    getApproveSectionItems(
                        value = transaction.value,
                        coinPrice = rates[transaction.value.coinUid],
                        spenderAddress = transaction.spender,
                        hideAmount = transactionItem.hideAmount,
                    )
                )

            is ContractCallTransactionRecord -> {
                itemSections.add(getContractMethodSectionItems(transaction.method, transaction.contractAddress, transaction.blockchainType))

                for (event in transaction.outgoingEvents) {
                    itemSections.add(
                        getSendSectionItems(
                            value = event.value,
                            toAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }

                for (event in transaction.incomingEvents) {
                    itemSections.add(
                        getReceiveSectionItems(
                            value = event.value,
                            fromAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }
            }

            is TronContractCallTransactionRecord -> {
                itemSections.add(getContractMethodSectionItems(transaction.method, transaction.contractAddress, transaction.blockchainType))

                for (event in transaction.outgoingEvents) {
                    itemSections.add(
                        getSendSectionItems(
                            value = event.value,
                            toAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }

                for (event in transaction.incomingEvents) {
                    itemSections.add(
                        getReceiveSectionItems(
                            value = event.value,
                            fromAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }
            }

            is ExternalContractCallTransactionRecord -> {
                for (event in transaction.outgoingEvents) {
                    itemSections.add(
                        getSendSectionItems(
                            value = event.value,
                            toAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }

                for (event in transaction.incomingEvents) {
                    itemSections.add(
                        getReceiveSectionItems(
                            value = event.value,
                            fromAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }
            }

            is TronExternalContractCallTransactionRecord -> {
                for (event in transaction.outgoingEvents) {
                    itemSections.add(
                        getSendSectionItems(
                            value = event.value,
                            toAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }

                for (event in transaction.incomingEvents) {
                    itemSections.add(
                        getReceiveSectionItems(
                            value = event.value,
                            fromAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }
            }

            is TronTransactionRecord -> {
                itemSections.add(
                    listOf(
                        Transaction(
                            transaction.transaction.contract?.label ?: getString(R.string.Transactions_ContractCall),
                            "",
                            TransactionViewItem.Icon.Platform(transaction.blockchainType).iconRes
                        )
                    )
                )
            }

            is BitcoinIncomingTransactionRecord -> {
                itemSections.add(
                    getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                    )
                )

                miscItemsSection.addAll(getBitcoinSectionItems(transaction, transactionItem.lastBlockInfo))
                addMemoItem(transaction.memo, miscItemsSection)
            }

            is BitcoinOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(getSendSectionItems(
                    value = transaction.value,
                    toAddress = transaction.to,
                    coinPrice = rates[transaction.value.coinUid],
                    hideAmount = transactionItem.hideAmount,
                    sentToSelf = transaction.sentToSelf
                ))

                miscItemsSection.addAll(getBitcoinSectionItems(transaction, transactionItem.lastBlockInfo))
                addMemoItem(transaction.memo, miscItemsSection)
            }

            is BinanceChainIncomingTransactionRecord -> {
                itemSections.add(
                    getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                    )
                )

                addMemoItem(transaction.memo, miscItemsSection)
            }

            is BinanceChainOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                    )
                )

                addMemoItem(transaction.memo, miscItemsSection)
            }

            is SolanaIncomingTransactionRecord ->
                itemSections.add(
                    getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        nftMetadata = nftMetadata
                    )
                )

            is SolanaOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                        nftMetadata = nftMetadata
                    )
                )
            }

            is SolanaUnknownTransactionRecord -> {
                for (transfer in transaction.outgoingTransfers) {
                    itemSections.add(
                        getSendSectionItems(
                            value = transfer.value,
                            toAddress = transfer.address,
                            coinPrice = rates[transfer.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }

                for (transfer in transaction.incomingTransfers) {
                    itemSections.add(
                        getReceiveSectionItems(
                            value = transfer.value,
                            fromAddress = transfer.address,
                            coinPrice = rates[transfer.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata
                        )
                    )
                }
            }

            else -> {}
        }

        if (sentToSelf) {
            miscItemsSection.add(SentToSelf)
        }
        if (miscItemsSection.isNotEmpty()) {
            itemSections.add(miscItemsSection)
        }

        itemSections.add(getStatusSectionItems(transaction, status, rates))
        if (transaction is EvmTransactionRecord && !transaction.foreignTransaction && status == TransactionStatus.Pending && resendEnabled) {
            itemSections.add(listOf(SpeedUpCancel(transactionHash = transaction.transactionHash, blockchainType = transaction.blockchainType)))
            itemSections.add(listOf(TransactionInfoViewItem.Description(translator.getString(R.string.TransactionInfo_SpeedUpDescription))))
        } else if (transaction is BitcoinOutgoingTransactionRecord && transaction.replaceable && resendEnabled) {
            itemSections.add(listOf(SpeedUpCancel(transactionHash = transaction.transactionHash, blockchainType = transaction.blockchainType)))
            itemSections.add(listOf(TransactionInfoViewItem.Description(translator.getString(R.string.TransactionInfo_SpeedUpDescription))))
        }
        itemSections.add(getExplorerSectionItems(transactionItem.explorerData))

        return itemSections
    }

    private fun getContact(address: String?): Contact? {
        return contactsRepo.getContactsFiltered(blockchainType, addressQuery = address).firstOrNull()
    }

    private fun addMemoItem(
        memo: String?,
        miscItemsSection: MutableList<TransactionInfoViewItem>
    ) {
        if (!memo.isNullOrBlank()) {
            miscItemsSection.add(
                Value(getString(R.string.TransactionInfo_Memo), memo)
            )
        }
    }

    private fun getReceiveSectionItems(
        value: TransactionValue,
        fromAddress: String?,
        coinPrice: CurrencyValue?,
        hideAmount: Boolean,
        nftMetadata: Map<NftUid, NftAssetBriefMetadata> = mapOf()
    ): List<TransactionInfoViewItem> {
        val mint = fromAddress == zeroAddress
        val title: String = if (mint) getString(R.string.Transactions_Mint) else getString(R.string.Transactions_Receive)

        val amount: TransactionInfoViewItem
        val rate: TransactionInfoViewItem?

        when (value) {
            is TransactionValue.NftValue -> {
                amount = getNftAmount(title, value, true, hideAmount, nftMetadata[value.nftUid])
                rate = null
            }

            else -> {
                amount = getAmount(coinPrice, value, true, hideAmount, AmountType.Received)
                rate = getHistoricalRate(coinPrice, value)
            }
        }

        val items: MutableList<TransactionInfoViewItem> = mutableListOf(
            amount
        )

        if (!mint && fromAddress != null) {
            val contact = getContact(fromAddress)
            items.add(
                Address(getString(R.string.TransactionInfo_From), fromAddress, contact == null, blockchainType, StatSection.AddressFrom)
            )
            contact?.let {
                items.add(
                    ContactItem(it)
                )
            }
        }

        rate?.let { items.add(it) }

        return items
    }

    private fun getSendSectionItems(
        value: TransactionValue,
        toAddress: String?,
        coinPrice: CurrencyValue?,
        hideAmount: Boolean,
        sentToSelf: Boolean = false,
        nftMetadata: Map<NftUid, NftAssetBriefMetadata> = mapOf()
    ): List<TransactionInfoViewItem> {
        val burn = toAddress == zeroAddress

        val title: String = if (burn) getString(R.string.Transactions_Burn) else getString(R.string.Transactions_Send)

        val amount: TransactionInfoViewItem
        val rate: TransactionInfoViewItem?

        when (value) {
            is TransactionValue.NftValue -> {
                amount = getNftAmount(title, value, if (sentToSelf) null else false, hideAmount, nftMetadata[value.nftUid])
                rate = null
            }

            else -> {
                amount = getAmount(coinPrice, value, if (sentToSelf) null else false, hideAmount, AmountType.Sent)
                rate = getHistoricalRate(coinPrice, value)
            }
        }

        val items: MutableList<TransactionInfoViewItem> = mutableListOf(amount)

        if (!burn && toAddress != null) {
            val contact = getContact(toAddress)
            items.add(
                Address(getString(R.string.TransactionInfo_To), toAddress, contact == null, blockchainType, StatSection.AddressTo)
            )

            contact?.let {
                items.add(ContactItem(it))
            }
        }

        rate?.let { items.add(it) }

        return items
    }

    private fun getSwapEventSectionItems(
        valueIn: TransactionValue?,
        valueOut: TransactionValue?,
        rates: Map<String, CurrencyValue>,
        amount: SwapTransactionRecord.Amount?,
        hideAmount: Boolean,
        hasRecipient: Boolean
    ) = buildList {
        valueIn?.let {
            add(
                getAmount(
                    rates[valueIn.coinUid],
                    valueIn,
                    false,
                    hideAmount,
                    AmountType.YouSent,
                    amount
                )
            )
        }
        valueOut?.let {
            add(
                getAmount(
                    rates[valueOut.coinUid],
                    valueOut,
                    true,
                    hideAmount,
                    AmountType.YouGot,
                    hasRecipient = hasRecipient,
                )
            )
        }
    }

    private fun getSwapDetailsSectionItems(
        rates: Map<String, CurrencyValue>,
        exchangeAddress: String,
        valueOut: TransactionValue?,
        valueIn: TransactionValue?
    ): List<TransactionInfoViewItem> {
        val items: MutableList<TransactionInfoViewItem> = mutableListOf(
            Value(
                getString(R.string.TransactionInfo_Service),
                evmLabelManager.mapped(exchangeAddress)
            )
        )

        if (valueIn == null || valueOut == null) {
            return items
        }

        val decimalValueIn = valueIn.decimalValue
        val decimalValueOut = valueOut.decimalValue
        val valueOutDecimals = valueOut.decimals
        val valueInDecimals = valueIn.decimals

        if (decimalValueIn == null || decimalValueOut == null || valueInDecimals == null || valueOutDecimals == null) {
            return items
        }

        val priceValueOne = if (decimalValueOut.compareTo(BigDecimal.ZERO) == 0) {
            Translator.getString(R.string.NotAvailable)
        } else {
            val price = decimalValueIn.divide(
                decimalValueOut,
                min(valueOutDecimals, valueInDecimals),
                RoundingMode.HALF_EVEN
            ).abs()
            val formattedPrice = numberFormatter.formatCoinFull(price, valueIn.coinCode, 8)
            val formattedFiatPrice = rates[valueIn.coinUid]?.let { rate ->
                numberFormatter.formatFiatFull(price * rate.value, rate.currency.symbol).let {
                    " ($it)"
                }
            } ?: ""
            "${valueOut.coinCode} = $formattedPrice$formattedFiatPrice"
        }

        val priceValueTwo = if (decimalValueIn.compareTo(BigDecimal.ZERO) == 0) {
            Translator.getString(R.string.NotAvailable)
        } else {
            val price = decimalValueOut.divide(
                decimalValueIn,
                min(valueInDecimals, valueOutDecimals),
                RoundingMode.HALF_EVEN
            ).abs()
            val formattedPrice = numberFormatter.formatCoinFull(price, valueOut.coinCode, 8)
            val formattedFiatPrice = rates[valueOut.coinUid]?.let { rate ->
                numberFormatter.formatFiatFull(price * rate.value, rate.currency.symbol).let {
                    " ($it)"
                }
            } ?: ""
            "${valueIn.coinCode} = $formattedPrice$formattedFiatPrice"
        }

        items.add(
            TransactionInfoViewItem.PriceWithToggle(
                getString(R.string.TransactionInfo_Price),
                priceValueOne,
                priceValueTwo,
            )
        )

        return items
    }

    private fun getContractCreationItems(transaction: ContractCreationTransactionRecord): List<TransactionInfoViewItem> =
        listOf(
            Transaction(
                getString(R.string.Transactions_ContractCreation),
                "",
                TransactionViewItem.Icon.Platform(transaction.blockchainType).iconRes
            )
        )

    private fun getApproveSectionItems(
        value: TransactionValue,
        coinPrice: CurrencyValue?,
        spenderAddress: String,
        hideAmount: Boolean,
    ): List<TransactionInfoViewItem> {
        val fiatAmountFormatted = coinPrice?.let {
            value.decimalValue?.let { decimalValue ->
                numberFormatter.formatFiatFull(
                    (it.value * decimalValue).abs(),
                    it.currency.symbol
                )
            }
        } ?: "---"

        val coinAmountFormatted = value.decimalValue?.let { decimalValue ->
            numberFormatter.formatCoinFull(
                decimalValue,
                value.coinCode,
                8
            )
        } ?: ""

        val coinAmountString = when {
            hideAmount -> "*****"
            value.isMaxValue -> "∞ ${value.coinCode}"

            else -> coinAmountFormatted
        }

        val coinAmountColoredValue = ColoredValue(coinAmountString, getAmountColor(null))

        val fiatAmountString = when {
            hideAmount -> "*****"
            value.isMaxValue -> translator.getString(R.string.Transaction_Unlimited)
            else -> fiatAmountFormatted
        }

        val fiatAmountColoredValue = ColoredValue(fiatAmountString, ColorName.Grey)

        val contact = getContact(spenderAddress)

        val items = mutableListOf(
            Amount(
                coinAmountColoredValue,
                fiatAmountColoredValue,
                value.coinIconUrl,
                value.alternativeCoinIconUrl,
                value.coinIconPlaceholder,
                value.coin?.uid,
                value.badge,
                AmountType.Approved
            ),
            Address(getString(R.string.TransactionInfo_Spender), spenderAddress, contact == null, blockchainType, StatSection.AddressSpender)
        )

        contact?.let {
            items.add(ContactItem(it))
        }

        return items
    }

    private fun getContractMethodSectionItems(
        method: String?,
        contractAddress: String,
        blockchainType: BlockchainType
    ) = listOf(
        Transaction(
            method ?: getString(R.string.Transactions_ContractCall),
            evmLabelManager.mapped(contractAddress),
            TransactionViewItem.Icon.Platform(blockchainType).iconRes
        )
    )

    private fun getBitcoinSectionItems(transaction: BitcoinTransactionRecord, lastBlockInfo: LastBlockInfo?): List<TransactionInfoViewItem> {
        val items: MutableList<TransactionInfoViewItem> = mutableListOf()

        transaction.conflictingHash?.let { conflictingHash ->
            items.add(
                getDoubleSpendViewItem(
                    transaction.transactionHash,
                    conflictingHash
                )
            )
        }

        if (transaction.showRawTransaction) {
            items.add(RawTransaction)
        }

        val lockState = transaction.lockState(lastBlockInfo?.timestamp)
        getLockStateItem(lockState)?.let {
            items.add(it)
        }

        return items
    }

    private fun getStatusSectionItems(
        transaction: TransactionRecord,
        status: TransactionStatus,
        rates: Map<String, CurrencyValue?>
    ): List<TransactionInfoViewItem> {
        val items: MutableList<TransactionInfoViewItem> = mutableListOf(
            Value(getString(R.string.TransactionInfo_Date), dateHelper.getFullDate(Date(transaction.timestamp * 1000))),
            Status(status)
        )

        when (transaction) {
            is EvmTransactionRecord -> {
                if (!transaction.foreignTransaction && transaction.fee != null) {
                    items.add(getFeeItem(transaction.fee, rates[transaction.fee.coinUid], status))
                }

                if (transaction is SwapTransactionRecord && transaction.valueOut != null) {
                    val recipientItems = mutableListOf<TransactionInfoViewItem>()

                    val recipient = transaction.recipient
                    if (recipient != null) {
                        val contact = getContact(recipient)

                        recipientItems.add(
                            Address(
                                getString(R.string.TransactionInfo_RecipientHash),
                                recipient,
                                contact == null,
                                blockchainType,
                                StatSection.AddressRecipient
                            )
                        )

                        contact?.let {
                            recipientItems.add(
                                ContactItem(it)
                            )
                        }
                    }

                    items.addAll(0, recipientItems)
                }
            }

            is TronTransactionRecord -> {
                if (!transaction.foreignTransaction && transaction.fee != null) {
                    items.add(getFeeItem(transaction.fee, rates[transaction.fee.coinUid], status))
                }
            }

            is TonTransactionRecord -> {
                if (transaction.fee != null) {
                    items.add(getFeeItem(transaction.fee, rates[transaction.fee.coinUid], status))
                }
            }

            is BitcoinOutgoingTransactionRecord ->
                transaction.fee?.let { items.add(getFee(it, rates[it.coinUid])) }

            is BinanceChainOutgoingTransactionRecord ->
                items.add(getFee(transaction.fee, rates[transaction.fee.coinUid]))

            is SolanaOutgoingTransactionRecord -> {
                if (transaction.fee != null) {
                    items.add(getFeeItem(transaction.fee, rates[transaction.fee.coinUid], status))
                }
            }
        }

        items.add(TransactionHash(transaction.transactionHash))

        return items
    }

    private fun getExplorerSectionItems(explorerData: TransactionInfoModule.ExplorerData): List<TransactionInfoViewItem> =
        listOf(
            Explorer(
                translator.getString(R.string.TransactionInfo_ButtonViewOnExplorerName, explorerData.title),
                explorerData.url
            )
        )

    private fun getDoubleSpendViewItem(transactionHash: String, conflictingHash: String) = DoubleSpend(transactionHash, conflictingHash)

    private fun getLockStateItem(lockState: TransactionLockState?): TransactionInfoViewItem? {
        return lockState?.let {
            val leftIcon = if (it.locked) com.icons.R.drawable.ic_lock_20 else com.icons.R.drawable.ic_unlock_20
            val date = DateHelper.getFullDate(it.date)
            val title = translator.getString(
                if (it.locked) R.string.TransactionInfo_LockedUntil else R.string.TransactionInfo_UnlockedAt,
                date
            )
            LockState(title, leftIcon, it.date, it.locked)
        }
    }

    private fun getAmountColor(incoming: Boolean?): ColorName {
        return when (incoming) {
            true -> ColorName.Remus
            false -> ColorName.Lucian
            else -> ColorName.Leah
        }
    }

    private fun getString(resId: Int): String {
        return translator.getString(resId)
    }

    private fun getAmount(
        rate: CurrencyValue?,
        value: TransactionValue,
        incoming: Boolean?,
        hideAmount: Boolean,
        amountType: AmountType,
        amount: SwapTransactionRecord.Amount? = null,
        hasRecipient: Boolean = false,
    ): TransactionInfoViewItem {
        val valueInFiat = if (hideAmount) "*****" else rate?.let {
            value.decimalValue?.let { decimalValue ->
                numberFormatter.formatFiatFull(
                    (it.value * decimalValue).abs(),
                    it.currency.symbol
                )
            }
        } ?: "---"
        val fiatValueColored = ColoredValue(valueInFiat, ColorName.Grey)
        val coinValueFormatted = if (hideAmount) "*****" else value.decimalValue?.let { decimalValue ->
            val sign = when (incoming) {
                true -> "+"
                false -> "-"
                else -> ""
            }
            val valueWithCoinCode = numberFormatter.formatCoinFull(decimalValue.abs(), value.coinCode, 8)
            if (amount is SwapTransactionRecord.Amount.Extremum && incoming != null) {
                val suffix = if (incoming) getString(R.string.Swap_AmountMin) else getString(R.string.Swap_AmountMax)
                "$sign$valueWithCoinCode $suffix"
            } else {
                "$sign$valueWithCoinCode"
            }
        } ?: "---"

        val color = if (hasRecipient && incoming == true) {
            ColorName.Lucian
        } else {
            getAmountColor(incoming)
        }

        val coinValueColored = ColoredValue(coinValueFormatted, color)
        val coinUid = if (value is TransactionValue.CoinValue && !value.token.isCustom) {
            value.token.coin.uid
        } else {
            null
        }
        return Amount(
            coinValueColored,
            fiatValueColored,
            value.coinIconUrl,
            value.alternativeCoinIconUrl,
            value.coinIconPlaceholder,
            coinUid,
            value.badge,
            amountType,
        )
    }

    private fun getNftAmount(
        title: String,
        value: TransactionValue.NftValue,
        incoming: Boolean?,
        hideAmount: Boolean,
        nftMetadata: NftAssetBriefMetadata?
    ): TransactionInfoViewItem {
        val valueFormatted = if (hideAmount) "*****" else value.decimalValue.let { decimalValue ->
            val sign = when {
                incoming == null -> ""
                decimalValue < BigDecimal.ZERO -> "-"
                decimalValue > BigDecimal.ZERO -> "+"
                else -> ""
            }
            val valueWithCoinCode = numberFormatter.formatCoinFull(decimalValue.abs(), value.coinCode, 8)
            "$sign$valueWithCoinCode"
        }

        val nftName = nftMetadata?.name ?: value.tokenName?.let {
            when (value.nftUid) {
                is NftUid.Evm -> "$it #${value.nftUid.tokenId}"
                is NftUid.Solana -> it
            }
        } ?: "#${value.nftUid.tokenId}"

        return NftAmount(
            title,
            ColoredValue(valueFormatted, getAmountColor(incoming)),
            nftName,
            nftMetadata?.previewImageUrl,
            R.drawable.icon_24_nft_placeholder,
            null,
        )
    }

    private fun getHistoricalRate(
        rate: CurrencyValue?,
        transactionValue: TransactionValue,
    ): TransactionInfoViewItem {
        val rateValue = if (rate == null) {
            "---"
        } else {
            val rateFormatted = numberFormatter.formatFiatFull(rate.value, rate.currency.symbol)
            translator.getString(
                R.string.Balance_RatePerCoin,
                rateFormatted,
                transactionValue.coinCode
            )
        }
        return Value(getString(R.string.TransactionInfo_HistoricalRate), rateValue)
    }

    private fun getFee(transactionValue: TransactionValue, rate: CurrencyValue?): TransactionInfoViewItem {
        val feeAmountString = getFeeAmountString(rate, transactionValue)

        return Value(getString(R.string.TransactionInfo_Fee), feeAmountString)
    }

    private fun getFeeItem(
        transactionValue: TransactionValue,
        rate: CurrencyValue?,
        status: TransactionStatus
    ): TransactionInfoViewItem {
        val feeAmountString = getFeeAmountString(rate, transactionValue)
        val feeTitle: String = when (status) {
            TransactionStatus.Pending -> getString(R.string.TransactionInfo_FeeEstimated)
            is TransactionStatus.Processing,
            TransactionStatus.Failed,
            TransactionStatus.Completed -> getString(R.string.TransactionInfo_Fee)
        }

        return Value(feeTitle, feeAmountString)
    }

    private fun getFeeAmountString(rate: CurrencyValue?, transactionValue: TransactionValue): String {
        val feeInFiat = rate?.let {
            transactionValue.decimalValue?.let { decimalValue ->
                numberFormatter.formatFiatFull(
                    it.value * decimalValue,
                    it.currency.symbol
                )
            }
        }
        val feeInCoin = transactionValue.decimalValue?.let { decimalValue ->
            numberFormatter.formatCoinFull(decimalValue, transactionValue.coinCode, 8)
        } ?: ""

        return feeInCoin + (if (feeInFiat != null) " | $feeInFiat" else "")
    }

}
