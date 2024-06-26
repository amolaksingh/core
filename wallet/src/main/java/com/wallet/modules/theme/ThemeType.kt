package com.wallet.modules.theme

import com.google.gson.annotations.SerializedName
import com.wallet.R
import com.wallet.ui.compose.TranslatableString
import com.wallet.ui.compose.WithTranslatableTitle

enum class ThemeType(
    val value: String,
    override val title: TranslatableString,
    val iconRes: Int
) : WithTranslatableTitle {
    @SerializedName("dark")
    Dark(
        "Dark",
        TranslatableString.ResString(R.string.SettingsTheme_Dark),
        com.icons.R.drawable.ic_theme_dark
    ),
    @SerializedName("light")
    Light(
        "Light",
        TranslatableString.ResString(R.string.SettingsTheme_Light),
        com.icons.R.drawable.ic_theme_light
    ),
    @SerializedName("system")
    System(
        "System",
        TranslatableString.ResString(R.string.SettingsTheme_System),
        com.icons.R.drawable.ic_theme_system
    );
}
