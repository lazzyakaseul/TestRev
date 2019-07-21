package com.lazzy.testrev.domain

import com.lazzy.testrev.domain.entity.Course
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiveCurrenciesUseCaseImpl @Inject constructor(
    private val receiveCurrenciesProvider: ReceiveCurrenciesProvider
) : ReceiveCurrenciesUseCase {

    override fun receiveCurrencies(base: String?): Single<Course> =
        receiveCurrenciesProvider.receiveCurrencies(base)

}