package com.lazzy.testrev.domain

import com.lazzy.testrev.domain.entity.Currencies
import com.lazzy.testrev.domain.entity.Currency
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round

@Singleton
class CurrenciesInteractorImpl @Inject constructor(
    private val receiveCurrenciesUseCase: ReceiveCurrenciesUseCase
) :
    CurrenciesInteractor {

    private val baseSubject = BehaviorSubject.create<String>()
    private val currentValueSubject = BehaviorSubject.create<Double>()
    private val currenciesSubject = BehaviorSubject.create<List<Currency>>()
    private val currentCourseSubject = BehaviorSubject.create<Currencies>()

    @Volatile
    private var oldBase: String? = null

    override fun changeBaseCurrency(newBase: String): Completable =
        if (oldBase != null) {
            baseSubject.firstOrError()
                .flatMapCompletable {
                    Completable.fromAction {
                        oldBase = it
                        baseSubject.onNext(newBase)
                    }
                }
        } else {
            Completable.create {
                baseSubject.onNext(newBase)
            }
        }

    override fun updateSelectedCurrency(newValue: Double) {
        currentValueSubject.onNext(newValue)
    }

    override fun setCurrencies(currencies: List<Currency>) {
        currenciesSubject.onNext(currencies)
    }

    override fun startOnlineCurrencyUpdates(): Completable =
        Observable.interval(UPDATE_PERIOD, TimeUnit.SECONDS)
            .flatMapSingle { baseSubject.firstOrError() }
            .concatMapSingle { receiveCurrenciesUseCase.receiveCurrencies(it) }
            .map { it.currencies }
            .doOnNext { currentCourseSubject.onNext(it) }
            .retryWhen { it.delay(REPEAT_REQUEST_DELAY, TimeUnit.SECONDS) }
            .ignoreElements()

    override fun observeCurrencyUpdates(): Observable<List<Currency>> =
        baseSubject
            .observeOn(Schedulers.computation())
            .switchMap { base ->
                Observable.combineLatest(
                    currentValueSubject,
                    currentCourseSubject,
                    BiFunction { currentValue: Double, courseCurrencies: Currencies ->
                        currentValue to courseCurrencies
                    }
                )
                    .flatMapSingle { (currentValue, courseCurrencies) ->
                        currenciesSubject.firstOrError()
                            .map { Triple(currentValue, courseCurrencies, it) }
                    }
                    .map { (currentValue, courseCurrencies, currencies) ->
                        val jumbledCurrenciesList = moveNewBaseCurrencyToTop(base, currencies)
                        recalculateAllCurrencies(
                            oldBase,
                            currentValue,
                            courseCurrencies,
                            jumbledCurrenciesList
                        )
                    }
                    .doOnNext { currenciesSubject.onNext(it) }
            }

    private fun moveNewBaseCurrencyToTop(
        baseCurrency: String,
        currencies: List<Currency>
    ): List<Currency> {
        return currencies.toMutableList().also { mutableCurrencies ->
            val base = mutableCurrencies.find { it.code == baseCurrency }
            if (base != null) {
                mutableCurrencies.remove(base)
                mutableCurrencies.add(0, base)
            }
        }
    }

    private fun recalculateAllCurrencies(
        oldBase: String?,
        currentValue: Double,
        courseCurrencies: Currencies,
        currencies: List<Currency>
    ): List<Currency> {
        return currencies.map {
            if (oldBase != it.code) {
                val roundedCourseValue = courseCurrencies[it.code]?.apply {
                    roundDoubleValue(this, TWO_DIGITS_AFTER_ZERO)
                }
                val roundedCurrentValue = roundDoubleValue(currentValue, TWO_DIGITS_AFTER_ZERO)
                Currency(
                    code = it.code,
                    value = roundedCourseValue?.times(roundedCurrentValue) ?: 0.0
                )
            } else {
                it.copy(value = currentValue)
            }
        }
    }

    private fun roundDoubleValue(value: Double, digitsAfterZero: Int): Double {
        return round(value * digitsAfterZero * 10.0) / (digitsAfterZero * 10.0)
    }

    companion object {
        private const val TWO_DIGITS_AFTER_ZERO = 2
        private const val UPDATE_PERIOD = 1L
        private const val REPEAT_REQUEST_DELAY = 5L
    }
}