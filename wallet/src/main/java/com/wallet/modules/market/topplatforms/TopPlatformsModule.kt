package com.wallet.modules.market.topplatforms

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.wallet.R
import com.wallet.core.App
import com.wallet.core.iconUrl
import com.wallet.entities.ViewState
import com.wallet.modules.market.MarketField
import com.wallet.modules.market.SortingField
import com.wallet.modules.market.TimeDuration
import com.wallet.ui.compose.Select
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

object TopPlatformsModule {

    class Factory(private val timeDuration: TimeDuration?) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = TopPlatformsRepository(App.marketKit)
            return TopPlatformsViewModel(repository, App.currencyManager, timeDuration) as T
        }
    }

    data class Menu(
        val sortingFieldSelect: Select<SortingField>,
        val marketFieldSelect: Select<MarketField>
    )

    data class UiState(
        val sortingField: SortingField,
        val timePeriod: TimeDuration,
        val viewItems: List<TopPlatformViewItem>,
        val viewState: ViewState,
        val isRefreshing: Boolean
    )

}

@Parcelize
data class Platform(
    val uid: String,
    val name: String,
) : Parcelable

data class TopPlatformItem(
    val platform: Platform,
    val rank: Int,
    val protocols: Int,
    val marketCap: BigDecimal,
    val rankDiff: Int?,
    val changeDiff: BigDecimal?
)

@Immutable
data class TopPlatformViewItem(
    val platform: Platform,
    val subtitle: String,
    val marketCap: String,
    val marketCapDiff: BigDecimal?,
    val rank: String?,
    val rankDiff: Int?,
) {


    val iconUrl: String
        get() = platform.iconUrl

    val iconPlaceHolder: Int
        get() = R.drawable.ic_platform_placeholder_24

}
