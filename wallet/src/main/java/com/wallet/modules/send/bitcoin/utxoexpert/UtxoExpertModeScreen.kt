package com.wallet.modules.send.bitcoin.utxoexpert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallet.R
import com.wallet.core.ISendBitcoinAdapter
import com.wallet.core.shorten
import com.wallet.entities.Address
import com.wallet.modules.evmfee.ButtonsGroupWithShade
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.ButtonPrimaryYellow
import com.wallet.ui.compose.components.HSpacer
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.HsCheckbox
import com.wallet.ui.compose.components.RowUniversal
import com.wallet.ui.compose.components.SectionItemBorderedRowUniversalClear
import com.wallet.ui.compose.components.SectionUniversalItem
import com.wallet.ui.compose.components.VSpacer
import com.wallet.ui.compose.components.subhead2_grey
import com.wallet.ui.compose.components.subhead2_leah
import com.wallet.ui.compose.components.subhead2_lucian
import io.horizontalsystems.bitcoincore.storage.UnspentOutputInfo
import io.horizontalsystems.marketkit.models.Token
import java.math.BigDecimal

@Composable
fun UtxoExpertModeScreen(
    adapter: ISendBitcoinAdapter,
    token: Token,
    address: Address?,
    memo: String?,
    value: BigDecimal?,
    feeRate: Int?,
    customUnspentOutputs: List<UnspentOutputInfo>?,
    updateUnspentOutputs: (List<UnspentOutputInfo>) -> Unit,
    onBackClick: () -> Unit
) {

    val viewModel: UtxoExpertModeViewModel = viewModel(
        factory = UtxoExpertModeModule.Factory(
            adapter,
            token,
            address,
            memo,
            value,
            feeRate,
            customUnspentOutputs
        )
    )
    val uiState = viewModel.uiState

    ComposeAppTheme {
        Scaffold(
            backgroundColor = ComposeAppTheme.colors.tyler,
            topBar = {
                AppBar(
                    title = stringResource(R.string.Send_Utxos),
                    navigationIcon = {
                        HsBackButton(onClick = onBackClick)
                    },
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.background(ComposeAppTheme.colors.lawrence)
                ) {
                    UtxoInfoSection(
                        uiState.sendToInfo,
                        uiState.changeInfo,
                        uiState.feeInfo,
                    )
                }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    UtxoList(
                        utxos = uiState.utxoItems,
                        onItemClicked = {
                            viewModel.onUnspentOutputClicked(it)
                            updateUnspentOutputs(viewModel.customOutputs)
                        }
                    )
                }
                ButtonsGroupWithShade {
                    ButtonPrimaryYellow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp),
                        title = stringResource(R.string.Button_Done),
                        onClick = onBackClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun UtxoList(
    utxos: List<UtxoExpertModeModule.UnspentOutputViewItem>,
    onItemClicked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        item {
            VSpacer(16.dp)
        }
        itemsIndexed(utxos) { index, item ->
            UtxoCell(
                id = item.id,
                selected = item.selected,
                title = item.date,
                subtitle = item.address.shorten(),
                value = item.amountToken,
                subValue = item.amountFiat,
                onItemClicked = onItemClicked,
                showTopBorder = index != 0,
                topRoundedCorners = index == 0,
                bottomRoundedCorners = index == utxos.size - 1
            )
        }
        item {
            VSpacer(16.dp)
        }
    }
}

@Composable
private fun UtxoInfoSection(
    sendToInfo: UtxoExpertModeModule.InfoItem,
    changeInfo: UtxoExpertModeModule.InfoItem?,
    feeInfo: UtxoExpertModeModule.InfoItem
) {
    val infoItems = buildList<@Composable () -> Unit> {
        add {
            UtxoInfoCell(
                title = stringResource(R.string.Send_Utxo_SendTo),
                subtitle = sendToInfo.subTitle,
                value = sendToInfo.value,
                subValue = sendToInfo.subValue
            )
        }
        changeInfo?.let {
            add {
                UtxoInfoCell(
                    title = stringResource(R.string.Send_Utxo_Change),
                    subtitle = changeInfo.subTitle,
                    value = changeInfo.value,
                    subValue = changeInfo.subValue
                )
            }
        }
        add {
            UtxoInfoCell(
                title = stringResource(R.string.Send_Fee),
                subtitle = feeInfo.subTitle,
                value = feeInfo.value,
                subValue = feeInfo.subValue
            )
        }
    }
    infoItems.forEachIndexed { index, composable ->
        SectionUniversalItem(
            borderTop = index != 0,
        ) {
            composable()
        }
    }
}

@Composable
private fun UtxoInfoCell(
    title: String,
    subtitle: String?,
    value: String?,
    subValue: String?
) {
    RowUniversal(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            subhead2_leah(text = title)
            subtitle?.let {
                subhead2_grey(text = it)
            }
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (value == null) {
                subhead2_lucian(text = "N/A")
            } else {
                subhead2_leah(text = value)
                subhead2_grey(text = subValue ?: "---")
            }
        }
    }
}

@Composable
private fun UtxoCell(
    id: String,
    selected: Boolean,
    showTopBorder: Boolean,
    title: String,
    subtitle: String,
    value: String,
    subValue: String?,
    onItemClicked: (String) -> Unit,
    topRoundedCorners: Boolean,
    bottomRoundedCorners: Boolean
) {
    val shape = when {
        topRoundedCorners && bottomRoundedCorners -> RoundedCornerShape(12.dp)
        topRoundedCorners -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        bottomRoundedCorners -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
        else -> RoundedCornerShape(0.dp)
    }
    Box(
        modifier = Modifier
            .clip(shape)
            .background(ComposeAppTheme.colors.lawrence)
    ) {
        SectionItemBorderedRowUniversalClear(
            onClick = {
                onItemClicked.invoke(id)
            },
            borderTop = showTopBorder
        ) {
            HsCheckbox(
                checked = selected,
                onCheckedChange = { onItemClicked.invoke(id) }
            )
            HSpacer(16.dp)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                subhead2_leah(text = title)
                subhead2_grey(text = subtitle)
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                subhead2_leah(text = value)
                subValue?.let {
                    subhead2_grey(text = it)
                }
            }
        }
    }
}