package com.wallet.modules.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.core.getInput
import com.wallet.modules.coin.analytics.CoinAnalyticsModule.AnalyticInfo
import com.wallet.modules.info.ui.BulletedText
import com.wallet.modules.info.ui.InfoBody
import com.wallet.modules.info.ui.InfoHeader
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.ButtonPrimaryYellow
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.ScreenMessageWithAction

class CoinAnalyticsInfoFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        CoinAnalyticsInfoScreen(
            navController.getInput()
        ) { navController.popBackStack() }
    }

}

@Composable
private fun CoinAnalyticsInfoScreen(
    analyticsInfo: AnalyticInfo?,
    onBackPress: () -> Unit
) {

    Surface(color = ComposeAppTheme.colors.tyler) {
        Column {
            AppBar(
                navigationIcon = {
                    HsBackButton(onClick = onBackPress)
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                analyticsInfo?.let { info ->
                    InfoHeader(info.title)
                    AnalyticsInfoBody(info)
                    Spacer(Modifier.height(20.dp))
                } ?: run {
                    ScreenMessageWithAction(
                        text = stringResource(R.string.Error),
                        icon = com.icons.R.drawable.ic_error_48
                    ) {
                        ButtonPrimaryYellow(
                            modifier = Modifier
                                .padding(horizontal = 48.dp)
                                .fillMaxWidth(),
                            title = stringResource(R.string.Button_Close),
                            onClick = onBackPress
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsInfoBody(info: AnalyticInfo) {
    when (info) {
        AnalyticInfo.CexVolumeInfo -> {
            BulletedText(R.string.CoinAnalytics_CexVolume_Info1)
            BulletedText(R.string.CoinAnalytics_CexVolume_Info2)
            BulletedText(R.string.CoinAnalytics_CexVolume_Info3)
            BulletedText(R.string.CoinAnalytics_CexVolume_Info4)
        }

        AnalyticInfo.DexVolumeInfo -> {
            BulletedText(R.string.CoinAnalytics_DexVolume_Info1)
            BulletedText(R.string.CoinAnalytics_DexVolume_Info2)
            BulletedText(R.string.CoinAnalytics_DexVolume_Info3)
            BulletedText(R.string.CoinAnalytics_DexVolume_Info4)
            InfoBody(R.string.CoinAnalytics_DexVolume_TrackedDexes)
            BulletedText(R.string.CoinAnalytics_DexVolume_TrackedDexes_Info1)
            BulletedText(R.string.CoinAnalytics_DexVolume_TrackedDexes_Info2)
        }

        AnalyticInfo.DexLiquidityInfo -> {
            BulletedText(R.string.CoinAnalytics_DexLiquidity_Info1)
            BulletedText(R.string.CoinAnalytics_DexLiquidity_Info2)
            BulletedText(R.string.CoinAnalytics_DexLiquidity_Info3)
            InfoBody(R.string.CoinAnalytics_DexLiquidity_TrackedDexes)
            BulletedText(R.string.CoinAnalytics_DexLiquidity_TrackedDexes_Info1)
            BulletedText(R.string.CoinAnalytics_DexLiquidity_TrackedDexes_Info2)
        }

        AnalyticInfo.AddressesInfo -> {
            BulletedText(R.string.CoinAnalytics_ActiveAddresses_Info1)
            BulletedText(R.string.CoinAnalytics_ActiveAddresses_Info2)
            BulletedText(R.string.CoinAnalytics_ActiveAddresses_Info3)
            BulletedText(R.string.CoinAnalytics_ActiveAddresses_Info4)
            BulletedText(R.string.CoinAnalytics_ActiveAddresses_Info5)
        }

        AnalyticInfo.TransactionCountInfo -> {
            BulletedText(R.string.CoinAnalytics_TransactionCount_Info1)
            BulletedText(R.string.CoinAnalytics_TransactionCount_Info2)
            BulletedText(R.string.CoinAnalytics_TransactionCount_Info3)
            BulletedText(R.string.CoinAnalytics_TransactionCount_Info4)
            BulletedText(R.string.CoinAnalytics_TransactionCount_Info5)
        }

        AnalyticInfo.HoldersInfo -> {
            BulletedText(R.string.CoinAnalytics_Holders_Info1)
            BulletedText(R.string.CoinAnalytics_Holders_Info2)
            InfoBody(R.string.CoinAnalytics_Holders_TrackedBlockchains)
        }

        AnalyticInfo.TvlInfo -> {
            BulletedText(R.string.CoinAnalytics_ProjectTVL_Info1)
            BulletedText(R.string.CoinAnalytics_ProjectTVL_Info2)
            BulletedText(R.string.CoinAnalytics_ProjectTVL_Info3)
            BulletedText(R.string.CoinAnalytics_ProjectTVL_Info4)
            BulletedText(R.string.CoinAnalytics_ProjectTVL_Info5)
        }

        AnalyticInfo.TechnicalIndicatorsInfo -> {
            InfoBody(R.string.TechnicalAdvice_InfoDescription)
        }
    }
}

@Preview
@Composable
private fun Preview_CoinAnalyticsInfoScreen() {
    ComposeAppTheme {
        CoinAnalyticsInfoScreen(
            AnalyticInfo.CexVolumeInfo,
        ) {}
    }
}
