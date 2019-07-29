package com.lazzy.testrev.presentation.adapter

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import java.util.regex.Pattern

class DecimalDigitsInputFilter(digitsAfterZero: Int) :
    InputFilter {

    private val mPattern =
        Pattern.compile("^$|(^(?:0|[1-9][0-9]*)(?:\\.[0-9]{1,$digitsAfterZero}|\\.)?\$)")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val replacement = source.subSequence(start, end)
        val newVal =
            "${dest.subSequence(0, dstart)}$replacement${dest.subSequence(dend, dest.length)}"
        if (mPattern.matcher(newVal).matches())
            return null

        return if (TextUtils.isEmpty(source)) dest.subSequence(dstart, dend) else ""
    }

}