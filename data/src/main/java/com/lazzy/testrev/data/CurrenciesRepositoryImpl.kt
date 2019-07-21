package com.lazzy.testrev.data

import com.lazzy.testrev.data.api.RevApi
import com.lazzy.testrev.data.dataobjects.convertToEntity
import com.lazzy.testrev.domain.entity.Course
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrenciesRepositoryImpl @Inject constructor(private val api: RevApi) : CurrenciesRepository {

    override fun receiveCurrencies(base: String?): Single<Course> =
        api.receiveCurrencies(base)
            .map { it.convertToEntity() }
            .subscribeOn(Schedulers.io())
}