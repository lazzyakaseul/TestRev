package com.lazzy.testrev.viewobjects

import com.lazzy.testrev.domain.entity.Currency


fun Currency.convertToViewObject() =
    CurrencyVO(code, value, java.util.Currency.getInstance(code).displayName, null)

fun Currency.convertToViewObjectConsiderCount(count: Double) =
    CurrencyVO(code , value * count, java.util.Currency.getInstance(code).displayName, null)

fun CurrencyVO.convertToEntity() = Currency(code, value)
