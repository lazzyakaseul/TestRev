package com.lazzy.testrev.presentation

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCase
import com.lazzy.testrev.domain.entity.Course
import com.lazzy.testrev.domain.entity.Currency
import com.lazzy.testrev.domain.entity.convertToCurrenciesList
import com.lazzy.testrev.presentation.viewobjects.CurrencyVO
import com.lazzy.testrev.presentation.viewobjects.convertToViewObject
import com.lazzy.testrev.util.FlagBitmapFactory
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@InjectViewState
class MainPresenterImpl @Inject constructor(
    private val receiveCurrenciesUseCase: ReceiveCurrenciesUseCase,
    private val flagFactory: FlagBitmapFactory
) : MvpPresenter<MainView>(), MainPresenter {

    private val currentBaseSubject = BehaviorSubject.create<String>()
    private val currentValueSubject = BehaviorSubject.create<Double>()
    private val currenciesSubject = BehaviorSubject.create<List<Currency>>()
    private val stateSubject =
        BehaviorSubject.createDefault<MainPresenter.ScreenState>(MainPresenter.ScreenState.Loading)

    private val compositeDisposable = CompositeDisposable()

    init {
        receiveData()
        observeData()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    override fun receiveData() {
        stateSubject.onNext(MainPresenter.ScreenState.Loading)
        receiveCurrenciesUseCase.receiveCurrencies()
            .doOnSuccess {
                currentValueSubject.onNext(it.base.value)
                currentBaseSubject.onNext(it.base.code)
                val result = mutableListOf(it.base).apply {
                    addAll(it.currencies.convertToCurrenciesList())
                }
                this.currenciesSubject.onNext(result)
                stateSubject.onNext(MainPresenter.ScreenState.Success)
            }
            .doOnError { stateSubject.onNext(MainPresenter.ScreenState.ErrorLoading) }
            .subscribeOn(Schedulers.computation())
            .subscribe({}, {})
            .let(compositeDisposable::add)
    }

    override fun onCurrencySelected(currency: CurrencyVO) =
        currentBaseSubject.onNext(currency.code)

    override fun updateSelectedCurrency(newValue: Double) {
        currentValueSubject.onNext(newValue)
    }

    private fun observeData() {
        currentBaseSubject
            .observeOn(Schedulers.computation())
            .doOnNext { stateSubject.onNext(MainPresenter.ScreenState.UpdatesBlocked) }
            .switchMapCompletable { base ->
                Observable.combineLatest(
                    currentValueSubject,
                    Observable.interval(UPDATE_PERIOD, TimeUnit.SECONDS)
                        .concatMapSingle { receiveCurrenciesUseCase.receiveCurrencies(base) }
                        .doOnError {
                            stateSubject.onNext(
                                MainPresenter.ScreenState.ErrorUpdating(
                                    it
                                )
                            )
                        }
                        .retry(),
                    BiFunction { currentValue: Double, course: Course -> currentValue to course }
                )
                    .flatMapSingle { (currentValue, course) ->
                        currenciesSubject.firstOrError()
                            .map { Triple(currentValue, course, it) }
                    }
                    .map { (currentValue, course, currencies) ->
                        val jumbledCurrenciesList = moveNewBaseCurrencyToTop(base, currencies)
                        recalculateAllCurrencies(base, currentValue, course, jumbledCurrenciesList)
                    }
                    .doOnNext { currenciesSubject.onNext(it) }
                    .map { currencies ->
                        currencies.map {
                            it.convertToViewObject(flagFactory, currencies.first() == it)
                        }
                    }
                    .flatMapCompletable {
                        Completable.fromAction {
                            stateSubject.onNext(
                                MainPresenter.ScreenState.UpdateCurrencies(
                                    it
                                )
                            )
                        }
                            .andThen {
                                stateSubject.onNext(MainPresenter.ScreenState.UpdatesAllowed)
                            }
                    }
            }
            .subscribe({ }, { Log.e("Exception", it.message ?: it.toString()) })
            .composite(compositeDisposable)

        stateSubject
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                when (it) {
                    is MainPresenter.ScreenState.ErrorUpdating ->
                        viewState.showErrorUpdating()
                    is MainPresenter.ScreenState.UpdateCurrencies ->
                        viewState.showCurrencies(it.currencies)
                    is MainPresenter.ScreenState.UpdatesBlocked ->
                        viewState.blockCurrencyUpdates()
                    is MainPresenter.ScreenState.UpdatesAllowed ->
                        viewState.allowCurrencyUpdates()
                    is MainPresenter.ScreenState.ErrorLoading ->
                        viewState.showErrorScreen()
                    is MainPresenter.ScreenState.Success ->
                        viewState.showSuccessScreen()
                    is MainPresenter.ScreenState.Loading ->
                        viewState.showProgressState()
                }
            }
            .subscribe()
            .composite(compositeDisposable)
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
        currentBase: String,
        currentValue: Double,
        currentCourse: Course,
        currencies: List<Currency>
    ): List<Currency> {
        return currencies.map {
            if (currentBase != it.code) {
                Currency(
                    code = it.code,
                    value = currentCourse.currencies[it.code]?.times(currentValue) ?: 0.0
                )
            } else {
                it.copy(value = currentValue)
            }
        }
    }

    private fun Disposable.composite(compositeDisposable: CompositeDisposable) =
        compositeDisposable.add(this)

    companion object {

        private const val UPDATE_PERIOD = 1L

    }

}