package com.lazzy.testrev.domain

import com.lazzy.testrev.domain.entity.Course
import com.lazzy.testrev.domain.entity.Currency
import io.reactivex.Single

interface ReceiveCurrenciesUseCase {

    fun receiveCurrencies(base: String? = null): Single<Course>

}