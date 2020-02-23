package com.lazzy.testrev.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

fun Double.convertToStringCurrency(): String =
    if (this > 0) {
        NumberFormat.getInstance(Locale.UK).run {
            this as DecimalFormat
            applyPattern("#.##")
            roundingMode = RoundingMode.HALF_EVEN
            format(this@convertToStringCurrency)
        }
    } else ""
