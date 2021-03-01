package com.unifonic.noticesdk

import android.app.Application
import android.content.Context
import com.unifonic.noticesdk.di.components.AppComponent
import com.unifonic.noticesdk.di.components.DaggerAppComponent
import com.unifonic.noticesdk.di.modules.AppModule


class AndroidApp : Application() {
    init {
        instance = this
    }

    val component: AppComponent by lazy {

        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }


    override fun onCreate() {
        super.onCreate()
        component.inject(this)

    }

    companion object {
        lateinit var instance: AndroidApp
        fun context(): Context {
            return instance.applicationContext
        }
    }

}