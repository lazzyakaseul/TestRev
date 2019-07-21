package com.lazzy.testrev

import com.lazzy.testrev.viewobjects.CurrencyVO

interface MainPresenter {

    fun attachView(view: MainView)

    fun detachView()

    fun onCurrencySelected(currency: CurrencyVO)

    fun updateSelectedCurrency(newValue: Double)

}