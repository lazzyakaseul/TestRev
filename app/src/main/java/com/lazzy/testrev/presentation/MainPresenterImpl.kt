package com.lazzy.testrev.presentation

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.lazzy.testrev.domain.CurrenciesInteractor
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCase
import com.lazzy.testrev.domain.entity.convertToCurrenciesList
import com.lazzy.testrev.presentation.viewobjects.CurrencyVO
import com.lazzy.testrev.presentation.viewobjects.convertToViewObject
import com.lazzy.testrev.util.FlagBitmapFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@InjectViewState
class MainPresenterImpl @Inject constructor(
    private val receiveCurrenciesUseCase: ReceiveCurrenciesUseCase,
    private val currenciesInteractor: CurrenciesInteractor,
    private val flagFactory: FlagBitmapFactory
) : MvpPresenter<MainView>(), MainPresenter {

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
                currenciesInteractor.updateSelectedCurrency(it.base.value)
                currenciesInteractor.changeBaseCurrency(it.base.code)
                val result = mutableListOf(it.base).apply {
                    addAll(it.currencies.convertToCurrenciesList())
                }
                currenciesInteractor.setCurrencies(result)
                stateSubject.onNext(MainPresenter.ScreenState.Success)
            }
            .doOnError { stateSubject.onNext(MainPresenter.ScreenState.ErrorLoading) }
            .subscribeOn(Schedulers.computation())
            .subscribe({}, {})
            .let(compositeDisposable::add)
    }

    override fun onCurrencySelected(currency: CurrencyVO) =
        currenciesInteractor.changeBaseCurrency(currency.code)

    override fun updateSelectedCurrency(newValue: Double) {
        currenciesInteractor.updateSelectedCurrency(newValue)
    }

    private fun observeData() {
        currenciesInteractor.startOnlineCurrencyUpdates()
            .subscribe({}, { Log.e("Some error", it.message ?: "") })
            .let(compositeDisposable::add)

        currenciesInteractor.observeCurrencyUpdates()
            .map { currencies ->
                currencies.map {
                    it.convertToViewObject(flagFactory, it == currencies.first())
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { stateSubject.onNext(MainPresenter.ScreenState.UpdateCurrencies(it)) }
            .subscribe({ }, { Log.e("Some error", it.message ?: "") })
            .let(compositeDisposable::add)


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

    private fun Disposable.composite(compositeDisposable: CompositeDisposable) =
        compositeDisposable.add(this)
}