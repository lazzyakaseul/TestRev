package com.lazzy.testrev.domain.entity

fun Currencies.convertToCurrenciesList() =
    this.map { Currency(it.key, it.value) }