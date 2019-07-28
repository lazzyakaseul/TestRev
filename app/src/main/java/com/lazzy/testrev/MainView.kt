package com.lazzy.testrev

import com.lazzy.testrev.viewobjects.CurrencyVO

interface MainView {

    fun showCurrencies(currencies: List<CurrencyVO>)

    fun blockCurrencyUpdates()

    fun allowCurrencyUpdates()

    fun showErrorUpdating()

    fun showErrorScreen()

    fun showSuccessScreen()

    fun showProgressState()

}