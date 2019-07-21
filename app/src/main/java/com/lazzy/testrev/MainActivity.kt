package com.lazzy.testrev

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.lazzy.testrev.adapter.CurrencyAdapter
import com.lazzy.testrev.viewobjects.CurrencyVO
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView {

    @Inject
    lateinit var presenter: MainPresenter
    private val adapter = CurrencyAdapter {
        presenter.onCurrencySelected(it)
        presenter.updateSelectedCurrency(it.value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        App.appComponent.inject(this)
        currenciesView.layoutManager = LinearLayoutManager(this)
        currenciesView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.detachView()
    }

    override fun showCurrencies(currencies: List<CurrencyVO>) {
        adapter.updateCurrencies(currencies)
    }

}
