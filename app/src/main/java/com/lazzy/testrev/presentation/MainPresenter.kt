package com.lazzy.testrev.presentation

import com.lazzy.testrev.presentation.viewobjects.CurrencyVO


interface MainPresenter {

    fun attachView(view: MainView)

    fun detachView(view: MainView)

    fun receiveData()

    fun onCurrencySelected(currency: CurrencyVO)

    fun updateSelectedCurrency(newValue: Double)

    sealed class ScreenState {

        object Success : ScreenState()

        data class UpdateCurrencies(val currencies: List<CurrencyVO>) : ScreenState()

        object UpdatesBlocked : ScreenState()

        object UpdatesAllowed : ScreenState()

        data class ErrorUpdating(val exception: Throwable) : ScreenState()

        object Loading : ScreenState()

        object ErrorLoading : ScreenState()

    }

}