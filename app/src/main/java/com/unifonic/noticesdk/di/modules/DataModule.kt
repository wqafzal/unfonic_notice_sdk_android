package com.unifonic.noticesdk.di.modules


import octek.android.unifonicsdk.UnifonicNotificationService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
class DataModule {



    @Singleton
    @Provides
    fun getHomeService(@Named(RetrofitModule.PUBLIC_CLIENT) retrofit: Retrofit): octek.android.unifonicsdk.UnifonicNotificationService {
        return retrofit.create(octek.android.unifonicsdk.UnifonicNotificationService::class.java)
    }

}