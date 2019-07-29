package com.lazzy.testrev.presentation.viewobjects

import com.lazzy.testrev.domain.entity.Currency
import com.lazzy.testrev.util.FlagBitmapFactory
import com.lazzy.testrev.util.convertToStringCurrency


fun Currency.convertToViewObject(factory: FlagBitmapFactory, isBase: Boolean = false) =
    CurrencyVO(
        code,
        value.convertToStringCurrency(),
        java.util.Currency.getInstance(code).displayName,
        factory.getFlagBitmap(code),
        isBase
    )