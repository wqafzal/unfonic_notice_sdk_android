package com.unifonic.noticesdk.di.modules


import com.unifonic.noticesdk.network.rest.SMSService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
class DataModule {



    @Singleton
    @Provides
    fun getHomeService(@Named(RetrofitModule.PUBLIC_CLIENT) retrofit: Retrofit): SMSService {
        return retrofit.create(SMSService::class.java)
    }

}