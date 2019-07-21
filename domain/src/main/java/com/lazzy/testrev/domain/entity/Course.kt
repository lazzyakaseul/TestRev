package com.lazzy.testrev.domain.entity

typealias Currencies = Map<String, Double>

data class Course(val base: Currency, val currencies: Currencies)

