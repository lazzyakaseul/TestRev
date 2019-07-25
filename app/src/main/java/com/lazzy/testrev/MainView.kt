package com.lazzy.testrev

import com.lazzy.testrev.viewobjects.CurrencyVO

interface MainView {

    fun showCurrencies(currencies: List<CurrencyVO>)

    fun showError(error: String)

}