package com.lazzy.testrev.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.bumptech.glide.Glide
import com.lazzy.testrev.R
import com.lazzy.testrev.viewobjects.CurrencyVO
import kotlinx.android.synthetic.main.item_currency_layout.view.*


class CurrencyAdapter(
    private val selectListener: (CurrencyVO) -> Unit,
    private val updateValueListener: (Double) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyHolder>() {

    private var currencies: List<CurrencyVO> = emptyList()
    private val currencyDiffUtil = CurrencyDiffUtil()
    private var focusedView: EditText? = null
    private var updatesAllowed = true

    private val watcher = object : TextWatcher {
        override fun afterTextChanged(source: Editable?) {
            if (updatesAllowed) {
                updateValueListener(source.toString().toDoubleOrNull() ?: 0.0)
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onCreateViewHolder(container: ViewGroup, pos: Int): CurrencyHolder =
        CurrencyHolder(
            LayoutInflater.from(container.context).inflate(
                R.layout.item_currency_layout,
                container,
                false
            )
        )

    override fun getItemCount(): Int = currencies.size

    override fun onBindViewHolder(holder: CurrencyHolder, pos: Int) = holder.bind(currencies[pos])

    override fun onBindViewHolder(
        holder: CurrencyHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val old = (payloads.first() as CurrencyDiffUtil.Update).oldItem
            val new = (payloads.last() as CurrencyDiffUtil.Update).newItem
            if (old.isBase != new.isBase) {
                onBindViewHolder(holder, position)
            } else if (old.value != new.value) {
                holder.bindValue(new)
            }
        }
    }

    fun updateCurrencies(currencies: List<CurrencyVO>) {
        DiffUtil.calculateDiff(currencyDiffUtil.init(this.currencies, currencies))
            .dispatchUpdatesTo(this)
        this.currencies = currencies
    }

    fun setUpdatesAllowed(allowed: Boolean) {
        updatesAllowed = allowed
    }

    inner class CurrencyHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView = view.imageView
        private val codeView = view.codeView
        private val descriptionView = view.descriptionView
        private val valueView = view.valueView
        private var currency: CurrencyVO? = null

        init {
            itemView.setOnClickListener {
                currency?.apply(selectListener)
                focusedView?.clearFocus()
            }
        }

        fun bind(item: CurrencyVO) {
            currency = item
            if (!item.isBase) {
                valueView?.removeTextChangedListener(watcher)
            }
            codeView.text = item.code
            descriptionView.text = item.description
            valueView.filters = arrayOf(DecimalDigitsInputFilter(DIGITS_AFTER_ZERO))
            valueView.apply {
                setText(item.value)
                setSelection(item.value.length)
                isFocusable = item.isBase
                isFocusableInTouchMode = item.isBase
            }
            if (item.isBase) {
                focusedView = valueView
                valueView?.addTextChangedListener(watcher)
                focusedView?.requestFocus()
            }
            Glide.with(itemView.context)
                .load(item.image)
                .into(imageView)
        }

        fun bindValue(currency: CurrencyVO) {
            this@CurrencyHolder.currency = currency
            currency.value.apply {
                if (valueView != focusedView) {
                    valueView.setText(this)
                }
            }
        }

    }

    companion object {

        private const val DIGITS_AFTER_ZERO = 2

    }

}