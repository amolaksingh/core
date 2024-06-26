package com.wallet.modules.rooteddevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wallet.R
import com.wallet.modules.evmfee.ButtonsGroupWithShade
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.ButtonPrimaryYellow
import com.wallet.ui.compose.components.subhead2_grey


@Composable
fun RootedDeviceScreen(
    onIgnoreWarningClicked: () -> Unit
) {
    Column(Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(com.icons.R.drawable.ic_attention_24),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(20.dp))
                subhead2_grey(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.Alert_DeviceIsRootedWarning)
                )
            }
        }

        ButtonsGroupWithShade {
            ButtonPrimaryYellow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                title = stringResource(R.string.RootedDevice_Button_Understand),
                onClick = onIgnoreWarningClicked,
            )
        }
    }
}

@Preview
@Composable
fun Preview_RootedDeviceScreen() {
    ComposeAppTheme {
        RootedDeviceScreen {}
    }
}