package com.lazzy.testrev

import android.app.Application
import com.lazzy.testrev.di.AppComponent
import com.lazzy.testrev.di.DaggerAppComponent

class App : Application() {

    companion object {

        lateinit var appComponent: AppComponent
            private set

    }

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .build()
    }
}