package com.lazzy.testrev.util

import java.math.RoundingMode
import java.text.DecimalFormat

fun Double.convertToStringCurrency(): String =
    if (this > 0) {
        DecimalFormat("#.##").run {
            roundingMode = RoundingMode.HALF_EVEN
            format(this@convertToStringCurrency)
        }
    } else ""
