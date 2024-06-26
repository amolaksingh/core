package com.wallet.modules.markdown

import android.os.Parcelable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.core.getInput
import com.wallet.core.slideFromRight
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.MenuItem
import kotlinx.parcelize.Parcelize

class MarkdownFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()

        MarkdownScreen(
            handleRelativeUrl = input?.handleRelativeUrl ?: false,
            showAsPopup = input?.showAsPopup ?: false,
            markdownUrl = input?.markdownUrl ?: "",
            onCloseClick = { navController.popBackStack() },
            onUrlClick = { url ->
                navController.slideFromRight(
                    R.id.markdownFragment, MarkdownFragment.Input(url)
                )
            }
        )
    }

    @Parcelize
    data class Input(
        val markdownUrl: String,
        val handleRelativeUrl: Boolean = false,
        val showAsPopup: Boolean = false,
    ) : Parcelable
}

@Composable
private fun MarkdownScreen(
    handleRelativeUrl: Boolean,
    showAsPopup: Boolean,
    markdownUrl: String,
    onCloseClick: () -> Unit,
    onUrlClick: (String) -> Unit,
    viewModel: MarkdownViewModel = viewModel(factory = MarkdownModule.Factory(markdownUrl))
) {

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            if (showAsPopup) {
                AppBar(
                    menuItems = listOf(
                        MenuItem(
                            title = TranslatableString.ResString(R.string.Button_Close),
                            icon = R.drawable.ic_close,
                            onClick = onCloseClick
                        )
                    )
                )
            } else {
                AppBar(navigationIcon = { HsBackButton(onClick = onCloseClick) })
            }
        }
    ) {
        MarkdownContent(
            modifier = Modifier.padding(it),
            viewState = viewModel.viewState,
            markdownBlocks = viewModel.markdownBlocks,
            handleRelativeUrl = handleRelativeUrl,
            onRetryClick = { viewModel.retry() },
            onUrlClick = onUrlClick
        )
    }
}
