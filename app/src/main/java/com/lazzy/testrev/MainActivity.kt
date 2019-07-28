package com.lazzy.testrev

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.lazzy.testrev.adapter.CurrencyAdapter
import com.lazzy.testrev.viewobjects.CurrencyVO
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView {

    @Inject
    lateinit var presenter: MainPresenter
    private val adapter = CurrencyAdapter({
        presenter.onCurrencySelected(it)
        Log.d("ASD", it.value.toDoubleOrNull()?.toString() ?: 0.0.toString())
        presenter.updateSelectedCurrency(it.value.toDoubleOrNull() ?: 0.0)
        currenciesView.smoothScrollToPosition(0)
    }, {
        Log.d("ASDk", it.toString())
        presenter.updateSelectedCurrency(it)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        App.appComponent.inject(this)
        currenciesView.layoutManager = LinearLayoutManager(this)
        currenciesView.adapter = adapter

        retry.setOnClickListener { presenter.receiveData() }
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
        Log.d("ASD", currencies.toString())
        val recyclerViewState = currenciesView.layoutManager?.onSaveInstanceState()
        adapter.updateCurrencies(currencies)
        currenciesView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun blockCurrencyUpdates() {
        Log.d("ASDState", "BLOCKED")
        adapter.setUpdatesAllowed(false)
    }

    override fun allowCurrencyUpdates() {
        Log.d("ASDState", "ALLOWED")
        adapter.setUpdatesAllowed(true)
    }

    override fun showErrorUpdating() {
        Toast.makeText(this, R.string.error_updating, Toast.LENGTH_LONG)
            .show()
    }

    override fun showErrorScreen() {
        progressView.visibility = View.GONE
        currenciesView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }

    override fun showSuccessScreen() {
        progressView.visibility = View.GONE
        errorView.visibility = View.GONE
        currenciesView.visibility = View.VISIBLE
    }

    override fun showProgressState() {
        progressView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        currenciesView.visibility = View.GONE
    }

}
