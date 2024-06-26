package com.wallet.modules.walletconnect.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.wallet.R
import com.wallet.core.AppLogger
import com.wallet.core.BaseComposeFragment
import com.wallet.modules.evmfee.ButtonsGroupWithShade
import com.wallet.modules.sendevmtransaction.TitleValue
import com.wallet.modules.sendevmtransaction.ValueType
import com.wallet.modules.sendevmtransaction.ViewItem
import com.wallet.modules.walletconnect.request.sendtransaction.WCEthereumTransaction
import com.wallet.modules.walletconnect.request.sendtransaction.WCSendEthRequestScreen
import com.wallet.modules.walletconnect.request.signtransaction.WCSignEthereumTransactionRequestScreen
import com.wallet.modules.walletconnect.session.ui.BlockchainCell
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.ButtonPrimaryDefault
import com.wallet.ui.compose.components.ButtonPrimaryYellow
import com.wallet.ui.compose.components.MenuItem
import com.wallet.ui.compose.components.MessageToSign
import com.wallet.ui.compose.components.ScreenMessageWithAction
import com.wallet.ui.compose.components.VSpacer
import com.wallet.ui.compose.components.cell.SectionUniversalLawrence
import com.core.helpers.HudHelper
import kotlinx.coroutines.launch

class WCRequestFragment : BaseComposeFragment() {
    private val logger = AppLogger("wallet-connect request")

    @Composable
    override fun GetContent(navController: NavController) {
        val wcRequestViewModel = viewModel<WCNewRequestViewModel>(factory = WCNewRequestViewModel.Factory())
        val composableScope = rememberCoroutineScope()
        when (val sessionRequestUI = wcRequestViewModel.sessionRequest) {
            is SessionRequestUI.Content -> {
                if (sessionRequestUI.method == "eth_sendTransaction") {
                    val blockchainType = wcRequestViewModel.blockchain?.type ?: return
                    val transaction =
                        try {
                            val ethTransaction = Gson().fromJson(
                                sessionRequestUI.param,
                                WCEthereumTransaction::class.java
                            )
                            ethTransaction.getWCTransaction()
                        } catch (e: Throwable) {
                            return
                        }

                    WCSendEthRequestScreen(
                        navController,
                        logger,
                        blockchainType,
                        transaction,
                        sessionRequestUI.peerUI.peerName
                    )
                } else if (sessionRequestUI.method == "eth_signTransaction") {
                    val blockchainType = wcRequestViewModel.blockchain?.type ?: return

                    val transaction = try {
                        val ethTransaction = Gson().fromJson(
                            sessionRequestUI.param,
                            WCEthereumTransaction::class.java
                        )
                        ethTransaction.getWCTransaction()
                    } catch (e: Throwable) {
                        return
                    }

                    WCSignEthereumTransactionRequestScreen(
                        navController,
                        logger,
                        blockchainType,
                        transaction,
                        sessionRequestUI.peerUI.peerName
                    )
                } else {
                    WCNewSignRequestScreen(
                        sessionRequestUI,
                        navController,
                        onAllow = {
                            composableScope.launch {
                                try {
                                    wcRequestViewModel.allow()
                                    navController.popBackStack()
                                } catch (e: Throwable) {
                                    showError(e)
                                }
                            }
                            logger.info("allow request")
                        },
                        onDecline = {
                            composableScope.launch {
                                try {
                                    wcRequestViewModel.reject()
                                    navController.popBackStack()
                                } catch (e: Throwable) {
                                    showError(e)
                                }
                            }
                            logger.info("decline request")
                        }
                    )
                }
            }

            is SessionRequestUI.Initial -> {
                ScreenMessageWithAction(
                    text = stringResource(R.string.Error),
                    icon = com.icons.R.drawable.ic_error_48
                ) {
                    ButtonPrimaryYellow(
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .fillMaxWidth(),
                        title = stringResource(R.string.Button_Close),
                        onClick = { navController.popBackStack() }
                    )
                }
            }
        }

    }

    private fun showError(e: Throwable) {
        HudHelper.showErrorMessage(
            requireActivity().findViewById(android.R.id.content),
            e.message ?: e::class.java.simpleName
        )
    }

}

@Composable
fun WCNewSignRequestScreen(
    sessionRequestUI: SessionRequestUI.Content,
    navController: NavController,
    onAllow: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)
    ) {
        AppBar(
            stringResource(R.string.WalletConnect_SignMessageRequest_Title),
            menuItems = listOf(
                MenuItem(
                    title = TranslatableString.ResString(R.string.Button_Close),
                    icon = R.drawable.ic_close,
                    onClick = { navController.popBackStack() }
                )
            )
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .fillMaxWidth()
        ) {
            VSpacer(12.dp)

            MessageContent(
                sessionRequestUI.param,
                sessionRequestUI.peerUI.peerName,
                sessionRequestUI.chainData,
            )

            VSpacer(24.dp)
        }

        ActionButtons(
            onDecline = onDecline,
            onAllow = onAllow
        )

    }

}

@Composable
private fun ActionButtons(
    onDecline: () -> Unit = {},
    onAllow: () -> Unit = {}
) {
    ButtonsGroupWithShade {
        Column(Modifier.padding(horizontal = 24.dp)) {
            ButtonPrimaryYellow(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.WalletConnect_SignMessageRequest_ButtonSign),
                onClick = onAllow,
            )
            VSpacer(16.dp)
            ButtonPrimaryDefault(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.Button_Reject),
                onClick = onDecline
            )
        }
    }
}

@Composable
private fun MessageContent(
    message: String,
    dAppName: String?,
    wcChainData: WCChainData?,
) {
    SectionUniversalLawrence {
        dAppName?.let { dApp ->
            TitleValue(
                ViewItem.Value(
                    title = stringResource(R.string.WalletConnect_SignMessageRequest_dApp),
                    value = dApp,
                    type = ValueType.Regular
                )
            )
        }
        wcChainData?.let {
            BlockchainCell(wcChainData.chain.name, wcChainData.address)
        }
    }

    MessageToSign(message)

}

