package com.wallet.modules.settings.faq

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wallet.R
import com.wallet.core.BaseComposeFragment
import com.wallet.core.LocalizedException
import com.wallet.core.slideFromRight
import com.wallet.core.stats.StatEvent
import com.wallet.core.stats.StatPage
import com.wallet.core.stats.stat
import com.wallet.entities.Faq
import com.wallet.entities.ViewState
import com.wallet.modules.coin.overview.ui.Loading
import com.wallet.modules.markdown.MarkdownFragment
import com.wallet.ui.compose.ComposeAppTheme
import com.wallet.ui.compose.components.AppBar
import com.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.wallet.ui.compose.components.HsBackButton
import com.wallet.ui.compose.components.RowUniversal
import com.wallet.ui.compose.components.ScreenMessageWithAction
import com.wallet.ui.compose.components.ScrollableTabs
import com.wallet.ui.compose.components.TabItem
import com.wallet.ui.compose.components.subhead1_leah
import java.net.UnknownHostException

class FaqListFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        FaqScreen(
            onCloseClick = { navController.popBackStack() },
            onItemClick = { faqItem ->
                navController.slideFromRight(
                    R.id.markdownFragment,
                    MarkdownFragment.Input(faqItem.markdown)
                )

                stat(page = StatPage.Faq, event = StatEvent.OpenArticle(faqItem.markdown))
            }
        )
    }

}

@Composable
private fun FaqScreen(
    onCloseClick: () -> Unit,
    onItemClick: (Faq) -> Unit,
    viewModel: FaqViewModel = viewModel(factory = FaqModule.Factory())
) {
    val viewState = viewModel.viewState
    Column(modifier = Modifier.background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = stringResource(R.string.Settings_Faq),
            navigationIcon = {
                HsBackButton(onClick = onCloseClick)
            }
        )
        Crossfade(viewState) { viewState ->
            when (viewState) {
                ViewState.Loading -> {
                    Loading()
                }

                is ViewState.Error -> {
                    val s = when (val error = viewState.t) {
                        is UnknownHostException -> stringResource(R.string.Hud_Text_NoInternet)
                        is LocalizedException -> stringResource(error.errorTextRes)
                        else -> stringResource(R.string.Hud_UnknownError, error)
                    }

                    ScreenMessageWithAction(s, com.icons.R.drawable.ic_error_48)
                }

                ViewState.Success -> {
                    Column {
                        val tabItems =
                            viewModel.sections.map {
                                TabItem(
                                    it.section,
                                    it == viewModel.selectedSection,
                                    it
                                )
                            }
                        ScrollableTabs(tabItems) { tab ->
                            viewModel.onSelectSection(tab)
                        }

                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Spacer(Modifier.height(12.dp))
                            CellUniversalLawrenceSection(viewModel.faqItems) { faq ->
                                RowUniversal(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { onItemClick(faq) }
                                ) {
                                    subhead1_leah(text = faq.title)
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
