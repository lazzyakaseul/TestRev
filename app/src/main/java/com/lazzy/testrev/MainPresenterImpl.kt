package com.lazzy.testrev

import android.util.Log
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCase
import com.lazzy.testrev.domain.entity.Course
import com.lazzy.testrev.domain.entity.Currency
import com.lazzy.testrev.domain.entity.convertToCurrenciesList
import com.lazzy.testrev.viewobjects.CurrencyVO
import com.lazzy.testrev.viewobjects.FlagBitmapFactory
import com.lazzy.testrev.viewobjects.convertToViewObject
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
class MainPresenterImpl @Inject constructor(
    private val receiveCurrenciesUseCase: ReceiveCurrenciesUseCase,
    private val flagFactory: FlagBitmapFactory
) : MainPresenter {

    private var view: MainView? = null
    private val currentBaseSubject = BehaviorSubject.create<String>()
    private val currentValueSubject = BehaviorSubject.create<Double>()
    private val currenciesSubject = BehaviorSubject.create<List<Currency>>()

    private val compositeDisposable = CompositeDisposable()

    init {
        receiveCurrenciesUseCase.receiveCurrencies()
            .doOnSuccess {
                currentBaseSubject.onNext(it.base.code)
                currentValueSubject.onNext(it.base.value)
                val result = mutableListOf(it.base).apply {
                    addAll(it.currencies.convertToCurrenciesList())
                }
                this.currenciesSubject.onNext(result)
            }
            .subscribeOn(Schedulers.computation())
            .subscribe()
    }


    override fun attachView(view: MainView) {
        this.view = view
        observeData()
    }

    override fun detachView() {
        view = null
        compositeDisposable.clear()
    }

    override fun onCurrencySelected(currency: CurrencyVO) =
        currentBaseSubject.onNext(currency.code)

    override fun updateSelectedCurrency(newValue: Double) =
        currentValueSubject.onNext(newValue)

    private fun observeData() {
        currentBaseSubject
            .flatMapSingle { base -> currenciesSubject.firstOrError().map { base to it } }
            .map { (base, currencies) ->
                base to currencies.toMutableList().apply {
                    find { it.code == base }?.apply {
                        remove(this)
                        add(0, this)
                    }
                }
            }
            .switchMap { (base, currencies) ->
                Observable.combineLatest(
                    currentValueSubject,
                    Observable.interval(1, TimeUnit.SECONDS)
                        .flatMapSingle { receiveCurrenciesUseCase.receiveCurrencies(base) },
                    BiFunction { currentValue: Double, course: Course -> currentValue to course }
                )
                    .map { (currentValue, course) ->
                        currencies.run {
                            map {
                                if (base != it.code) {
                                    Currency(
                                        it.code,
                                        course.currencies[it.code]?.times(currentValue) ?: 0.0
                                    )
                                } else it.copy(value = currentValue)
                            }
                        }
                    }
            }
            .subscribeOn(Schedulers.computation())
            .subscribe(
                { currenciesSubject.onNext(it) },
                {
                    Log.e("Exception", it.message ?: it.toString())
                    it.localizedMessage?.apply { view?.showError(this) }
                }
            )
            .composite(compositeDisposable)

        currenciesSubject
            .map { currencies ->
                mutableListOf(
                    currencies.first()
                        .convertToViewObject(flagFactory, true)
                ).apply {
                    addAll(currencies.drop(1)
                        .map { it.convertToViewObject(flagFactory) })
                }
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view?.showCurrencies(it) }
            .composite(compositeDisposable)
    }

    private fun Disposable.composite(compositeDisposable: CompositeDisposable) =
        compositeDisposable.add(this)

}