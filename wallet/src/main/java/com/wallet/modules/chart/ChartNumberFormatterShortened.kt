package io.horizontalsystems.bankwallet.modules.chart

import com.wallet.core.App
import io.horizontalsystems.bankwallet.entities.Currency
import java.math.BigDecimal

class ChartNumberFormatterShortened : ChartModule.ChartNumberFormatter {

    override fun formatValue(currency: Currency, value: BigDecimal): String {
        return App.numberFormatter.formatNumberShort(value, 2)
    }

}