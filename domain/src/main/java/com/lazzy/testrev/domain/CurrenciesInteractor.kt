package com.lazzy.testrev.domain

import com.lazzy.testrev.domain.entity.Currency
import io.reactivex.Completable
import io.reactivex.Observable

interface CurrenciesInteractor {

    fun startOnlineCurrencyUpdates(): Completable

    fun observeCurrencyUpdates(): Observable<List<Currency>>

    fun changeBaseCurrency(newBase: String): Completable

    fun updateSelectedCurrency(newValue: Double)

    fun setCurrencies(currencies: List<Currency>)
}