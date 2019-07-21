package com.lazzy.testrev.adapter

import android.support.v7.util.DiffUtil
import com.lazzy.testrev.viewobjects.CurrencyVO

class CurrencyDiffUtil : DiffUtil.Callback() {

    private lateinit var oldList: List<CurrencyVO>
    private lateinit var newList: List<CurrencyVO>

    override fun areItemsTheSame(pos0: Int, pos1: Int): Boolean =
        oldList[pos0].code == newList[pos1].code

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(pos0: Int, pos1: Int): Boolean =
        oldList[pos0] == newList[pos1]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
        Update(oldList[oldItemPosition], newList[newItemPosition])

    fun init(oldList: List<CurrencyVO>, newList: List<CurrencyVO>): CurrencyDiffUtil {
        this.oldList = oldList
        this.newList = newList
        return this
    }

    data class Update(val oldItem: CurrencyVO, val newItem: CurrencyVO)

}