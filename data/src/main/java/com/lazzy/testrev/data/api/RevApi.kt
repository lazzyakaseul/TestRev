package com.lazzy.testrev.data.api

import com.lazzy.testrev.data.dataobjects.CurrentCourse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query


interface RevApi {

    @GET("/latest")
    fun receiveCurrencies(@Query("base") base: String?): Single<CurrentCourse>

}