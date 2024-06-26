package com.wallet.modules.confirm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wallet.R
import com.wallet.modules.evmfee.ButtonsGroupWithShade
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.MenuItem
import com.wallet.ui.compose.components.VSpacer

@Composable
fun ConfirmTransactionScreen(
    title: String = stringResource(R.string.Swap_Confirm_Title),
    onClickBack: () -> Unit,
    onClickSettings: (() -> Unit)?,
    onClickClose: (() -> Unit)?,
    buttonsSlot: @Composable() (ColumnScope.() -> Unit),
    content: @Composable() (ColumnScope.() -> Unit)
) {
    Scaffold(
        topBar = {
            AppBar(
                title = title,
                navigationIcon = {
                    HsBackButton(onClick = onClickBack)
                },
                menuItems = buildList<MenuItem> {
                    onClickSettings?.let {
                        add(
                            MenuItem(
                                title = TranslatableString.ResString(R.string.Settings_Title),
                                icon = R.drawable.ic_manage_2_24,
                                onClick = onClickSettings
                            )
                        )
                    }
                    onClickClose?.let {
                        add(
                            MenuItem(
                                title = TranslatableString.ResString(R.string.Button_Close),
                                icon = R.drawable.ic_close,
                                onClick = onClickClose
                            )
                        )
                    }
                },
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = buttonsSlot
                )
            }
        },
        backgroundColor = ComposeAppTheme.colors.tyler,
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            VSpacer(height = 12.dp)

            content.invoke(this)

            VSpacer(height = 32.dp)
        }
    }
}