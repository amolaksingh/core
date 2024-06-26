package com.wallet.modules.coin.indicators

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.entities.DataState
import com.wallet.modules.chart.ChartIndicatorSetting
import com.wallet.modules.evmfee.ButtonsGroupWithShade
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.ButtonPrimaryYellow
import com.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.wallet.ui.compose.components.FormsInput
import com.wallet.ui.compose.components.HeaderText
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.InfoText
import com.wallet.ui.compose.components.MenuItem
import com.wallet.ui.compose.components.RowUniversal
import com.wallet.ui.compose.components.SelectorDialogCompose
import com.wallet.ui.compose.components.SelectorItem
import com.wallet.ui.compose.components.VSpacer
import com.wallet.ui.compose.components.body_leah
import com.wallet.ui.compose.components.subhead1_grey

@Composable
fun EmaSettingsScreen(navController: NavController, indicatorSetting: ChartIndicatorSetting) {
    val viewModel = viewModel<MovingAverageSettingViewModel>(
        factory = MovingAverageSettingViewModel.Factory(indicatorSetting)
    )
    val uiState = viewModel.uiState

    if (uiState.finish) {
        LaunchedEffect(uiState.finish) {
            navController.popBackStack()
        }
    }

    var showEmaSelectorDialog by remember { mutableStateOf(false) }

    val maType = uiState.maType ?: viewModel.defaultMaType

    if (showEmaSelectorDialog) {
        SelectorDialogCompose(
            title = stringResource(R.string.CoinPage_Type),
            items = viewModel.maTypes.map {
                SelectorItem(it, it == maType, it)
            },
            onDismissRequest = {
                showEmaSelectorDialog = false
            },
            onSelectItem = {
                viewModel.onSelectMaType(it)
            }
        )
    }

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = viewModel.name,
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Reset),
                        enabled = uiState.resetEnabled,
                        onClick = {
                            viewModel.reset()
                        }
                    )
                )
            )
        }
    ) {
        Column(Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                InfoText(
                    text = stringResource(R.string.CoinPage_EmaSettingsDescription)
                )
                VSpacer(12.dp)
                CellUniversalLawrenceSection(
                    listOf {
                        RowUniversal(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            onClick = {
                                showEmaSelectorDialog = true
                            }
                        ) {
                            body_leah(
                                text = stringResource(R.string.CoinPage_Type),
                                modifier = Modifier.weight(1f)
                            )
                            subhead1_grey(
                                text = maType,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Icon(
                                painter = painterResource(id = com.icons.R.drawable.ic_down_arrow_20),
                                contentDescription = null,
                                tint = ComposeAppTheme.colors.grey
                            )
                        }
                    }
                )
                VSpacer(24.dp)
                HeaderText(
                    text = stringResource(R.string.CoinPage_Period).uppercase()
                )

                FormsInput(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    hint = viewModel.defaultPeriod ?: "",
                    initial = uiState.period,
                    state = uiState.periodError?.let {
                        DataState.Error(it)
                    },
                    pasteEnabled = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    onValueChange = {
                        viewModel.onEnterPeriod(it)
                    }
                )
                VSpacer(32.dp)
            }
            ButtonsGroupWithShade {
                ButtonPrimaryYellow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    title = stringResource(R.string.SwapSettings_Apply),
                    onClick = {
                        viewModel.save()
                    },
                    enabled = uiState.applyEnabled
                )
            }
        }
    }
}