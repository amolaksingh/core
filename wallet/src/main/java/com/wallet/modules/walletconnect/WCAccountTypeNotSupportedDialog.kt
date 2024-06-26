package com.wallet.modules.walletconnect

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wallet.R
import com.wallet.core.getInput
import com.wallet.core.slideFromRight
import com.wallet.modules.manageaccounts.ManageAccountsModule
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.ButtonPrimaryYellow
import com.wallet.ui.compose.components.TextImportantWarning
import com.wallet.ui.extensions.BaseComposableBottomSheetFragment
import com.wallet.ui.extensions.BottomSheetHeader
import com.core.findNavController
import kotlinx.parcelize.Parcelize

class WCAccountTypeNotSupportedDialog : BaseComposableBottomSheetFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                val navController = findNavController()

                ComposeAppTheme {
                    WCAccountTypeNotSupportedScreen(
                        accountTypeDescription = navController.getInput<Input>()?.accountTypeDescription ?: "",
                        onCloseClick = {
                            navController.popBackStack()
                        },
                        onSwitchClick = {
                            navController.popBackStack()
                            navController.slideFromRight(
                                R.id.manageAccountsFragment,
                                ManageAccountsModule.Mode.Manage
                            )
                        }
                    )
                }
            }
        }
    }

    @Parcelize
    data class Input(val accountTypeDescription: String) : Parcelable
}

@Composable
fun WCAccountTypeNotSupportedScreen(
    accountTypeDescription: String,
    onCloseClick: () -> Unit,
    onSwitchClick: () -> Unit
) {
    BottomSheetHeader(
        iconPainter = painterResource(com.icons.R.drawable.ic_wallet_connect_24),
        iconTint = ColorFilter.tint(ComposeAppTheme.colors.jacob),
        title = stringResource(R.string.WalletConnect_Title),
        onCloseClick = onCloseClick
    ) {
        TextImportantWarning(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            text = stringResource(id = R.string.WalletConnect_NotSupportedDescription, accountTypeDescription)
        )
        ButtonPrimaryYellow(
            modifier = Modifier
                .padding(vertical = 20.dp, horizontal = 24.dp)
                .fillMaxWidth(),
            title = stringResource(R.string.Button_Switch),
            onClick = onSwitchClick
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Preview
@Composable
private fun WalletConnectErrorWatchAccountPreview() {
    ComposeAppTheme {
        WCAccountTypeNotSupportedScreen("Account Type Desc", {}, {})
    }
}
