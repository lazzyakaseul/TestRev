package com.lazzy.testrev.presentation

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.lazzy.testrev.presentation.viewobjects.CurrencyVO

@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {

    fun showCurrencies(currencies: List<CurrencyVO>)

    fun blockCurrencyUpdates()

    fun allowCurrencyUpdates()

    fun showErrorUpdating()

    fun showErrorScreen()

    fun showSuccessScreen()

    fun showProgressState()

}