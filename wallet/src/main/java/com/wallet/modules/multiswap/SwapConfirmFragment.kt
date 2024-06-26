package com.wallet.modules.multiswap

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.core.badge
import com.wallet.core.setNavigationResultX
import com.wallet.core.slideFromRight
import com.wallet.core.stats.StatPage
import com.wallet.entities.CoinValue
import com.wallet.entities.Currency
import com.wallet.entities.CurrencyValue
import com.wallet.modules.confirm.ConfirmTransactionScreen
import com.wallet.modules.evmfee.Cautions
import com.wallet.modules.multiswap.ui.DataFieldFee
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.ButtonPrimaryDefault
import com.wallet.ui.compose.components.ButtonPrimaryYellow
import com.wallet.ui.compose.components.CoinImage
import com.wallet.ui.compose.components.HFillSpacer
import com.wallet.ui.compose.components.HSpacer
import com.wallet.ui.compose.components.VSpacer
import com.wallet.ui.compose.components.caption_grey
import com.wallet.ui.compose.components.cell.CellUniversal
import com.wallet.ui.compose.components.cell.SectionUniversalLawrence
import com.wallet.ui.compose.components.subhead1_leah
import com.wallet.ui.compose.components.subhead2_grey
import com.wallet.ui.compose.components.subhead2_leah
import com.core.SnackbarDuration
import com.core.helpers.HudHelper
import io.horizontalsystems.marketkit.models.Token
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

class SwapConfirmFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        SwapConfirmScreen(navController)
    }

    @Parcelize
    data class Result(val success: Boolean) : Parcelable
}

@Composable
fun SwapConfirmScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    val previousBackStackEntry = remember { navController.previousBackStackEntry }
    val swapViewModel = viewModel<SwapViewModel>(
        viewModelStoreOwner = previousBackStackEntry!!,
    )

    val currentQuote = remember { swapViewModel.getCurrentQuote() } ?: return
    val settings = remember { swapViewModel.getSettings() }

    val currentBackStackEntry = remember { navController.currentBackStackEntry }
    val viewModel = viewModel<SwapConfirmViewModel>(
        viewModelStoreOwner = currentBackStackEntry!!,
        initializer = SwapConfirmViewModel.init(currentQuote, settings)
    )

    val uiState = viewModel.uiState

    ConfirmTransactionScreen(
        onClickBack = navController::popBackStack,
        onClickSettings = {
            navController.slideFromRight(R.id.swapTransactionSettings)
        },
        onClickClose = null,
        buttonsSlot = {
            if (uiState.loading) {
                ButtonPrimaryYellow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.Alert_Loading),
                    enabled = false,
                    onClick = { },
                )
                VSpacer(height = 12.dp)
                subhead1_leah(text = stringResource(id = R.string.SwapConfirm_FetchingFinalQuote))
            } else if (!uiState.validQuote) {
                ButtonPrimaryDefault(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.Button_Refresh),
                    onClick = {
                        viewModel.refresh()
                    },
                )
                VSpacer(height = 12.dp)
                subhead1_leah(text = "Quote is invalid")
            } else if (uiState.expired) {
                ButtonPrimaryDefault(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.Button_Refresh),
                    onClick = {
                        viewModel.refresh()
                    },
                )
                VSpacer(height = 12.dp)
                subhead1_leah(text = stringResource(id = R.string.SwapConfirm_QuoteExpired))
            } else {
                var buttonEnabled by remember { mutableStateOf(true) }
                ButtonPrimaryYellow(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.Swap),
                    enabled = buttonEnabled,
                    onClick = {
                        coroutineScope.launch {
                            buttonEnabled = false
                            HudHelper.showInProcessMessage(view, R.string.Swap_Swapping, SnackbarDuration.INDEFINITE)

                            val result = try {
                                viewModel.swap()

                                HudHelper.showSuccessMessage(view, R.string.Hud_Text_Done)
                                delay(1200)
                                SwapConfirmFragment.Result(true)
                            } catch (t: Throwable) {
                                HudHelper.showErrorMessage(view, t.javaClass.simpleName)
                                SwapConfirmFragment.Result(false)
                            }

                            buttonEnabled = true
                            navController.setNavigationResultX(result)
                            navController.popBackStack()
                        }
                    },
                )
                VSpacer(height = 12.dp)
                subhead1_leah(text = "Quote expires in ${uiState.expiresIn}")
            }
        }
    ) {
        SectionUniversalLawrence {
            TokenRow(
                token = uiState.tokenIn,
                amount = uiState.amountIn,
                fiatAmount = uiState.fiatAmountIn,
                currency = uiState.currency,
                borderTop = false,
                title = stringResource(R.string.Send_Confirmation_YouSend),
                amountColor = ComposeAppTheme.colors.leah,
            )
            TokenRow(
                token = uiState.tokenOut,
                amount = uiState.amountOut,
                fiatAmount = uiState.fiatAmountOut,
                currency = uiState.currency,
                title = stringResource(R.string.Swap_ToAmountTitle),
                amountColor = ComposeAppTheme.colors.remus,
            )
        }
        uiState.amountOut?.let { amountOut ->
            VSpacer(height = 16.dp)
            SectionUniversalLawrence {
                PriceField(uiState.tokenIn, uiState.tokenOut, uiState.amountIn, amountOut, StatPage.SwapConfirmation)
                PriceImpactField(uiState.priceImpact, uiState.priceImpactLevel, navController)
                uiState.amountOutMin?.let { amountOutMin ->
                    val subvalue = uiState.fiatAmountOutMin?.let { fiatAmountOutMin ->
                        CurrencyValue(uiState.currency, fiatAmountOutMin).getFormattedFull()
                    } ?: "---"

                    SwapInfoRow(
                        borderTop = true,
                        title = stringResource(id = R.string.Swap_MinimumReceived),
                        value = CoinValue(uiState.tokenOut, amountOutMin).getFormattedFull(),
                        subvalue = subvalue
                    )
                }
                uiState.quoteFields.forEach {
                    it.GetContent(navController, true)
                }
            }
        }

        val transactionFields = uiState.transactionFields
        if (transactionFields.isNotEmpty()) {
            VSpacer(height = 16.dp)
            SectionUniversalLawrence {
                transactionFields.forEachIndexed { index, field ->
                    field.GetContent(navController, index != 0)
                }
            }
        }

        VSpacer(height = 16.dp)
        SectionUniversalLawrence {
            DataFieldFee(
                navController,
                uiState.networkFee?.primary?.getFormattedPlain() ?: "---",
                uiState.networkFee?.secondary?.getFormattedPlain() ?: "---"
            )
        }

        if (uiState.cautions.isNotEmpty()) {
            Cautions(cautions = uiState.cautions)
        }
    }
}

@Composable
private fun SwapInfoRow(borderTop: Boolean, title: String, value: String, subvalue: String? = null) {
    CellUniversal(borderTop = borderTop) {
        subhead2_grey(text = title)
        HFillSpacer(minWidth = 16.dp)
        Column(horizontalAlignment = Alignment.End) {
            subhead2_leah(text = value)
            subvalue?.let {
                VSpacer(height = 1.dp)
                caption_grey(text = it)
            }
        }
    }
}

@Composable
fun TokenRow(
    token: Token,
    amount: BigDecimal?,
    fiatAmount: BigDecimal?,
    currency: Currency,
    borderTop: Boolean = true,
    title: String,
    amountColor: Color,
) {
    CellUniversal(borderTop = borderTop) {
        CoinImage(
            token = token,
            modifier = Modifier.size(32.dp)
        )
        HSpacer(width = 16.dp)
        Column {
            subhead2_leah(text = title)
            VSpacer(height = 1.dp)
            caption_grey(text = token.badge ?: stringResource(id = R.string.CoinPlatforms_Native))
        }
        HFillSpacer(minWidth = 16.dp)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount?.let { CoinValue(token, it).getFormattedFull() } ?: "---",
                style = ComposeAppTheme.typography.subhead1,
                color = amountColor,
            )
            fiatAmount?.let {
                VSpacer(height = 1.dp)
                caption_grey(text = CurrencyValue(currency, fiatAmount).getFormattedFull())
            }
        }
    }
}

@Composable
fun TokenRowUnlimited(
    token: Token,
    borderTop: Boolean = true,
    title: String,
    amountColor: Color,
) {
    CellUniversal(borderTop = borderTop) {
        CoinImage(
            token = token,
            modifier = Modifier.size(32.dp)
        )
        HSpacer(width = 16.dp)
        Column {
            subhead2_leah(text = title)
            VSpacer(height = 1.dp)
            caption_grey(text = token.badge ?: stringResource(id = R.string.CoinPlatforms_Native))
        }
        HFillSpacer(minWidth = 16.dp)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "∞ ${token.coin.code}",
                style = ComposeAppTheme.typography.subhead1,
                color = amountColor,
            )
            VSpacer(height = 1.dp)
            caption_grey(text = stringResource(id = R.string.Transaction_Unlimited))
        }
    }
}
