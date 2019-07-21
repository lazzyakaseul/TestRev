package com.lazzy.testrev

import android.util.Log
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCase
import com.lazzy.testrev.domain.entity.Course
import com.lazzy.testrev.domain.entity.Currency
import com.lazzy.testrev.domain.entity.convertToCurrenciesList
import com.lazzy.testrev.viewobjects.CurrencyVO
import com.lazzy.testrev.viewobjects.convertToEntity
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
    private val receiveCurrenciesUseCase: ReceiveCurrenciesUseCase
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
                                } else it
                            }
                        }
                    }
            }
            .subscribeOn(Schedulers.computation())
            .subscribe(
                { currenciesSubject.onNext(it) },
                { Log.e("ERROR", it.message ?: it.toString()) }
            )
            .composite(compositeDisposable)

        /*currentBaseSubject
            .distinctUntilChanged { current: Currency -> current.code }
            .flatMapSingle { current -> currenciesSubject.firstOrError().map { current to it } }
            .map { (current, currencies) ->
                currencies.toMutableList().apply {
                    find { it.code == current.code }?.apply { remove(this)}
                    add(0, current)
                }
            }
            .subscribeOn(Schedulers.computation())
            .subscribe {
                currenciesSubject.onNext(it)
            }
            .composite(compositeDisposable)*/

        currenciesSubject
            .map { currencies -> currencies.map { it.convertToViewObject() } }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view?.showCurrencies(it) }
            .composite(compositeDisposable)
    }


    override fun attachView(view: MainView) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun onCurrencySelected(currency: CurrencyVO) =
        currentBaseSubject.onNext(currency.code)

    override fun updateSelectedCurrency(newValue: Double) =
        currentValueSubject.onNext(newValue)

    private fun Disposable.composite(compositeDisposable: CompositeDisposable) =
        compositeDisposable.add(this)

}


//private val currentBaseSubject = BehaviorSubject.create<Currency>()
//private val currenciesSubject = BehaviorSubject.create<List<Currency>>().toSerialized()
//private val compositeDisposable = CompositeDisposable()

//init {
//    receiveCurrenciesUseCase.receiveCurrencies()
//        .doOnSuccess {
//            //                currentBaseSubject.onNext(MainPresenter.CurrencyCommand.Swap(it.base.convertToViewObject()))
//            currentBaseSubject.onNext(it.base)
//            val result = mutableListOf(it.base).apply {
//                addAll(it.currencies.convertToCurrenciesList())
//            }
//            this.currenciesSubject.onNext(result)
//        }
//        .subscribeOn(Schedulers.computation())
//        .subscribe()
//
//    Observable.combineLatest(
//        currentBaseSubject.distinctUntilChanged { old, new -> old.code == new.code },
//        Observable.interval(1, TimeUnit.SECONDS)
//            .flatMapSingle { currentBaseSubject.firstOrError() }
//            .flatMapSingle { receiveCurrenciesUseCase.receiveCurrencies(it) },
//        BiFunction { current: Currency, course: Course -> current to course }
//    )
//        .flatMapSingle { (current, course) ->
//            currenciesSubject.firstOrError().map { Triple(current, course, it) }
//        }
//        .map { (current, course, currencies) ->
//            currencies.toMutableList()
//                .run {
//                    map {
//                        if (current.code != it.code) {
//                            Currency(
//                                it.code,
//                                course.currencies[it.code]?.times(current.value) ?: 0.0
//                            )
//                        } else it
//                    }
//                }
//        }
//        .subscribeOn(Schedulers.computation())
//        .subscribe(
//            { currenciesSubject.onNext(it) },
//            { Log.e("ERROR", it.message ?: it.toString()) }
//        )
//        .composite(compositeDisposable)
//
//    currentBaseSubject
//        .distinctUntilChanged { current: Currency -> current.code }
//        .flatMapSingle { current -> currenciesSubject.firstOrError().map { current to it } }
//        .map { (current, currencies) ->
//            currencies.toMutableList().apply {
//                find { it.code == current.code }?.apply { remove(this)}
//                add(0, current)
//            }
//        }
//        .subscribeOn(Schedulers.computation())
//        .subscribe {
//            currenciesSubject.onNext(it)
//        }
//        .composite(compositeDisposable)
//
//    currenciesSubject
//        .map { currencies -> currencies.map { it.convertToViewObject() } }
//        .subscribeOn(Schedulers.computation())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe { view?.showCurrencies(it) }
//        .composite(compositeDisposable)
//}

//override fun onCurrencySelected(currency: CurrencyVO) =
//    currentBaseSubject.onNext(currency.convertToEntity())
//
//override fun updateSelectedCurrency(newValue: Double) {
//    currentBaseSubject
//        .firstOrError()
//        .doOnSuccess { currentBaseSubject.onNext(Currency(it.code, newValue)) }
//        .subscribe()
//}