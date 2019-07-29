package com.lazzy.testrev.di

import android.content.Context
import com.lazzy.testrev.data.CurrenciesRepository
import com.lazzy.testrev.data.CurrenciesRepositoryImpl
import com.lazzy.testrev.domain.ReceiveCurrenciesProvider
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCase
import com.lazzy.testrev.domain.ReceiveCurrenciesUseCaseImpl
import com.lazzy.testrev.presentation.MainPresenter
import com.lazzy.testrev.presentation.MainPresenterImpl
import com.lazzy.testrev.util.FlagBitmapFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module(includes = [AppModule.Bindings::class])
class AppModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideAppContext(): Context = context

    @Singleton
    @Provides
    fun provideBitmapFactory(context: Context) = FlagBitmapFactory(context)

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