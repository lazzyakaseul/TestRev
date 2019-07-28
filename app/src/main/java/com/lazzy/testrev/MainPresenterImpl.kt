package com.lazzy.testrev

import android.util.Log
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCase
import com.lazzy.testrev.domain.entity.Course
import com.lazzy.testrev.domain.entity.Currency
import com.lazzy.testrev.domain.entity.convertToCurrenciesList
import com.lazzy.testrev.viewobjects.CurrencyVO
import com.lazzy.testrev.viewobjects.FlagBitmapFactory
import com.lazzy.testrev.viewobjects.convertToViewObject
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
class MainPresenterImpl @Inject constructor(
    private val receiveCurrenciesUseCase: ReceiveCurrenciesUseCase,
    private val flagFactory: FlagBitmapFactory
) : MainPresenter {

    private var view: MainView? = null
    private val currentBaseSubject = BehaviorSubject.create<String>()
    private val currentValueSubject = BehaviorSubject.create<Double>()
    private val currenciesSubject = BehaviorSubject.create<List<Currency>>()
    private val stateSubject =
        BehaviorSubject.createDefault<MainPresenter.ScreenState>(MainPresenter.ScreenState.Loading)

    private val compositeDisposable = CompositeDisposable()

    init {
        receiveData()
    }

    override fun attachView(view: MainView) {
        this.view = view
        observeData()
    }

    override fun detachView() {
        view = null
        compositeDisposable.clear()
    }

    override fun receiveData() {
        stateSubject.onNext(MainPresenter.ScreenState.Loading)
        @Suppress
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
    }

    override fun onCurrencySelected(currency: CurrencyVO) =
        currentBaseSubject.onNext(currency.code)

    override fun updateSelectedCurrency(newValue: Double) {
        currentValueSubject.onNext(newValue)
    }

    private fun observeData() {
        currentBaseSubject
            .doOnNext { stateSubject.onNext(MainPresenter.ScreenState.UpdatesBlocked) }
            .flatMapSingle { base ->
                currenciesSubject.firstOrError()
                    .map { base to it }
            }
            .map { (base, currencies) ->
                base to currencies.toMutableList().apply {
                    find { it.code == base }?.apply {
                        remove(this)
                        add(0, this)
                    }
                }
            }
            .switchMapCompletable { (base, currencies) ->
                Observable.combineLatest(
                    currentValueSubject,
                    Observable.interval(1, TimeUnit.SECONDS)
                        .concatMapSingle { receiveCurrenciesUseCase.receiveCurrencies(base) }
                        .doOnError {
                            stateSubject.onNext(MainPresenter.ScreenState.ErrorUpdating(it))
                        }
                        .retry(),
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
                    .doOnNext { currenciesSubject.onNext(it) }
                    .map {
                        mutableListOf(
                            it.first()
                                .convertToViewObject(flagFactory, true)
                        ).apply {
                            addAll(it.drop(1)
                                .map { it.convertToViewObject(flagFactory) })
                        }
                    }
                    .flatMapCompletable {
                        Completable.fromAction {
                            stateSubject.onNext(MainPresenter.ScreenState.UpdateCurrencies(it))
                        }
                            .andThen { stateSubject.onNext(MainPresenter.ScreenState.UpdatesAllowed) }
                    }
                    .subscribeOn(Schedulers.computation())
            }
            .subscribeOn(Schedulers.computation())
            .subscribe({}, { Log.e("Exception", it.message ?: it.toString()) })
            .composite(compositeDisposable)

        stateSubject
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                when (it) {
                    is MainPresenter.ScreenState.ErrorUpdating ->
                        view?.showErrorUpdating()
                    is MainPresenter.ScreenState.UpdateCurrencies ->
                        view?.showCurrencies(it.currencies)
                    is MainPresenter.ScreenState.UpdatesBlocked ->
                        view?.blockCurrencyUpdates()
                    is MainPresenter.ScreenState.UpdatesAllowed ->
                        view?.allowCurrencyUpdates()
                    is MainPresenter.ScreenState.ErrorLoading ->
                        view?.showErrorScreen()
                    is MainPresenter.ScreenState.Success ->
                        view?.showSuccessScreen()
                    is MainPresenter.ScreenState.Loading ->
                        view?.showProgressState()
                }
            }
            .subscribe()
            .composite(compositeDisposable)
    }

    private fun Disposable.composite(compositeDisposable: CompositeDisposable) =
        compositeDisposable.add(this)

}