package com.wallet.modules.send.evm

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.slideFromRightForResult
import com.wallet.entities.Address
import com.wallet.entities.Wallet
import com.wallet.modules.address.AddressParserModule
import com.wallet.modules.address.AddressParserViewModel
import com.wallet.modules.address.HSAddressInput
import com.wallet.modules.amount.AmountInputModeViewModel
import com.wallet.modules.amount.HSAmountInput
import com.wallet.modules.availablebalance.AvailableBalance
import com.wallet.modules.send.SendScreen
import com.wallet.modules.send.evm.confirmation.SendEvmConfirmationFragment
import com.wallet.modules.sendtokenselect.PrefilledData
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.ButtonPrimaryYellow
import com.core.helpers.HudHelper

@Composable
fun SendEvmScreen(
    title: String,
    navController: NavController,
    amountInputModeViewModel: AmountInputModeViewModel,
    prefilledData: PrefilledData?,
    wallet: Wallet,
    predefinedAddress: String?,
) {
    val viewModel = viewModel<SendEvmViewModel>(factory = SendEvmModule.Factory(wallet, predefinedAddress))
    val uiState = viewModel.uiState

    val availableBalance = uiState.availableBalance
    val addressError = uiState.addressError
    val amountCaution = uiState.amountCaution
    val proceedEnabled = uiState.canBeSend
    val amountInputType = amountInputModeViewModel.inputType

    val paymentAddressViewModel = viewModel<AddressParserViewModel>(
        factory = AddressParserModule.Factory(wallet.token, prefilledData?.amount)
    )
    val amountUnique = paymentAddressViewModel.amountUnique
    val view = LocalView.current

    ComposeAppTheme {
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        SendScreen(
            title = title,
            onCloseClick = { navController.popBackStack() }
        ) {
            AvailableBalance(
                coinCode = wallet.coin.code,
                coinDecimal = viewModel.coinMaxAllowedDecimals,
                fiatDecimal = viewModel.fiatMaxAllowedDecimals,
                availableBalance = availableBalance,
                amountInputType = amountInputType,
                rate = viewModel.coinRate
            )

            Spacer(modifier = Modifier.height(12.dp))
            HSAmountInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                focusRequester = focusRequester,
                availableBalance = availableBalance,
                caution = amountCaution,
                coinCode = wallet.coin.code,
                coinDecimal = viewModel.coinMaxAllowedDecimals,
                fiatDecimal = viewModel.fiatMaxAllowedDecimals,
                onClickHint = {
                    amountInputModeViewModel.onToggleInputType()
                },
                onValueChange = {
                    viewModel.onEnterAmount(it)
                },
                inputType = amountInputType,
                rate = viewModel.coinRate,
                amountUnique = amountUnique
            )

            if (uiState.showAddressInput) {
                Spacer(modifier = Modifier.height(12.dp))
                HSAddressInput(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    initial = prefilledData?.address?.let { Address(it) },
                    tokenQuery = wallet.token.tokenQuery,
                    coinCode = wallet.coin.code,
                    error = addressError,
                    textPreprocessor = paymentAddressViewModel,
                    navController = navController
                ) {
                    viewModel.onEnterAddress(it)
                }
            }
            ButtonPrimaryYellow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                title = stringResource(R.string.Send_DialogProceed),
                onClick = {
                    if (viewModel.hasConnection()) {
                        viewModel.getSendData()?.let {
                            navController.slideFromRightForResult<SendEvmConfirmationFragment.Result>(
                                R.id.sendEvmConfirmationFragment,
                                SendEvmConfirmationFragment.Input(
                                    sendData = it,
                                    blockchainType = viewModel.wallet.token.blockchainType
                                )
                            ) {
                                if (it.success) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    } else {
                        HudHelper.showErrorMessage(view, R.string.Hud_Text_NoInternet)
                    }
                },
                enabled = proceedEnabled
            )
        }
    }
}
