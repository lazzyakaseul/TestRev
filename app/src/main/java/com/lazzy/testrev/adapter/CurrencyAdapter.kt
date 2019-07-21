package com.lazzy.testrev.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.lazzy.testrev.R
import com.lazzy.testrev.viewobjects.CurrencyVO
import kotlinx.android.synthetic.main.item_currency_layout.view.*

class CurrencyAdapter(private val listener: (CurrencyVO) -> Unit)
    : RecyclerView.Adapter<CurrencyAdapter.CurrencyHolder>() {

    private var currencies: List<CurrencyVO> = emptyList()
    private val currencyDiffUtil = CurrencyDiffUtil()

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
            if (old.value != new.value) {
                holder.bind(new.value)
            }
        }
    }

    fun updateCurrencies(currencies: List<CurrencyVO>) {
        DiffUtil.calculateDiff(currencyDiffUtil.init(this.currencies, currencies))
            .dispatchUpdatesTo(this)
        this.currencies = currencies
    }

    inner class CurrencyHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView = view.imageView
        private val codeView = view.codeView
        private val descriptionView = view.descriptionView
        private val valueView = view.valueView
        private var currency: CurrencyVO? = null

        init {
            itemView.setOnClickListener {
                currency?.apply(listener)
//                valueView.addTextChangedListener(object : TextWatcher {
//                    override fun afterTextChanged(p0: Editable?) {}
//
//                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//                })
//                valueView.removeTextChangedListener()
            }
        }

        fun bind(item: CurrencyVO) {
            currency = item
            codeView.text = item.code
            descriptionView.text = item.description
            valueView.setText(item.value.toString())
            Glide.with(itemView.context)
                .load(item.image)
                .into(imageView)
        }

        fun bind(value: Double) {
            valueView.setText(value.toString())
        }

    }

}