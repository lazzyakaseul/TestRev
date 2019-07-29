package com.lazzy.testrev.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.util.*
import javax.inject.Inject

class FlagBitmapFactory @Inject constructor(private val context: Context) {

    private val flagBitmapsMap = WeakHashMap<String, Bitmap>()

    fun getFlagBitmap(code: String): Bitmap? =
        flagBitmapsMap[code] ?: getBitmapFromAssets(code)
            .apply { flagBitmapsMap[code] = this }

    private fun getBitmapFromAssets(code: String): Bitmap? =
        try {
            BitmapFactory.decodeStream(
                context.assets
                    .open("flags/${code.toLowerCase()}.png")
            )
        } catch (ex: IOException) {
            null
        }

}