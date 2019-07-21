package com.lazzy.testrev.di

import com.google.gson.GsonBuilder
import com.lazzy.testrev.BuildConfig
import com.lazzy.testrev.BuildConfig.BASE_URL
import com.lazzy.testrev.MainPresenter
import com.lazzy.testrev.MainPresenterImpl
import com.lazzy.testrev.data.CurrenciesRepository
import com.lazzy.testrev.data.CurrenciesRepositoryImpl
import com.lazzy.testrev.data.api.RevApi
import com.lazzy.testrev.data.dataobjects.CurrentCourse
import com.lazzy.testrev.data.parser.ResponseParser
import com.lazzy.testrev.domain.ReceiveCurrenciesProvider
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCase
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module(includes = [AppModule.Bindings::class])
class AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build().run {
                val gson = GsonBuilder()
                    .setDateFormat("MM/dd/yyyy")
                    .registerTypeAdapter(CurrentCourse::class.java, ResponseParser())
                    .create()
                Retrofit.Builder()
                    .client(this)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            }

    @Singleton
    @Provides
    fun provideServerApi(retrofit: Retrofit): RevApi = retrofit.create(RevApi::class.java)

    @Module
    interface Bindings {

        @Binds
        fun bindCurrenciesRepository(impl: CurrenciesRepositoryImpl): CurrenciesRepository

        @Binds
        fun bindReceiveCurrenciesProvider(impl: CurrenciesRepository): ReceiveCurrenciesProvider

        @Binds
        fun bindReceiveCurrenciesUseCase(impl: ReceiveCurrenciesUseCaseImpl): ReceiveCurrenciesUseCase

        @Binds
        fun bindMainPresenter(impl: MainPresenterImpl): MainPresenter

    }

}