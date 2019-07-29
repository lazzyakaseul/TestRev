package com.lazzy.testrev.di

import com.lazzy.testrev.data.di.DataModule
import com.lazzy.testrev.presentation.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DataModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
}