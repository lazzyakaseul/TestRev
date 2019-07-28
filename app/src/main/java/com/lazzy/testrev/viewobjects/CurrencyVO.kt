package com.lazzy.testrev.viewobjects

import android.graphics.Bitmap

data class CurrencyVO(
    val code: String,
    val value: String,
    val description: String,
    val image: Bitmap?,
    val isBase: Boolean
) {

    override fun toString(): String {
        return "$code $value $isBase"
    }
}