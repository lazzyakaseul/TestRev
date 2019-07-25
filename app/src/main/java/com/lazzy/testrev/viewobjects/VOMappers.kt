package com.lazzy.testrev.viewobjects

import com.lazzy.testrev.domain.entity.Currency
import java.math.RoundingMode
import java.text.DecimalFormat


fun Currency.convertToViewObject(factory: FlagBitmapFactory, isBase: Boolean = false) =
    CurrencyVO(
        code,
        DecimalFormat("#.##").run {
            roundingMode = RoundingMode.HALF_EVEN
            format(value)
        },
        java.util.Currency.getInstance(code).displayName,
        factory.getFlagBitmap(code),
        isBase
    )