package com.lazzy.testrev.domain

import com.lazzy.testrev.domain.entity.Course
import io.reactivex.Single

interface ReceiveCurrenciesProvider {

    fun receiveCurrencies(base: String?): Single<Course>

}