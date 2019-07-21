package com.lazzy.testrev.viewobjects

import android.graphics.Bitmap

data class CurrencyVO(
    val code: String,
    val value: Double,
    val description: String,
    val image: Bitmap?
)