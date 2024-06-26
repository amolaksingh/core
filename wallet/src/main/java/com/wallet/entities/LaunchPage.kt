package com.wallet.entities

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import com.wallet.R
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.WithTranslatableTitle

enum class LaunchPage(@StringRes val titleRes: Int, @DrawableRes val iconRes: Int):
    WithTranslatableTitle {
    @SerializedName("auto")
    Auto(R.string.SettingsLaunchScreen_Auto, com.icons.R.drawable.ic_settings_20),
    @SerializedName("balance")
    Balance(R.string.SettingsLaunchScreen_Balance, com.icons.R.drawable.ic_wallet_20),
    @SerializedName("market")
    Market(R.string.SettingsLaunchScreen_MarketOverview, com.icons.R.drawable.ic_market_20),
    @SerializedName("watchlist")
    Watchlist(R.string.SettingsLaunchScreen_Watchlist, com.icons.R.drawable.ic_star_20);

    override val title: TranslatableString
        get() = TranslatableString.ResString(titleRes)

    companion object {
        private val map = entries.associateBy(LaunchPage::name)

        fun fromString(type: String?): LaunchPage? = map[type]
    }
}
