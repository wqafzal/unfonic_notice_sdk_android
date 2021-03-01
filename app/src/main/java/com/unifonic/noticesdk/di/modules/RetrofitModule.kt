package com.unifonic.noticesdk.di.modules


import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
class RetrofitModule {

    companion object {
        const val PUBLIC_CLIENT = "publicClient"
        const val AUTH_CLIENT = "authClient"
        const val WEB_CLIENT = "webClient"
    }

    @Singleton
    @Provides
    @Named(PUBLIC_CLIENT)
    fun getPublicRetrofitClient(): Retrofit {

        val client = OkHttpClient.Builder()
//        if (BuildConfig.enabledLogging) {

            val logging = HttpLoggingInterceptor()

            logging.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logging)
//        }
        client.connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)

        return Retrofit.Builder()
            .baseUrl("http://dev.qamsoft.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()
    }
}