package octek.android.unifonicsdk

import android.content.Context
import octek.android.unifonicsdk.extensions.*
import octek.android.unifonicsdk.models.NotificationReadType
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class UnifonicSDK constructor(
    val baseUrl: String,
    val context: Context,
    val enableLoggingInterceptor: Boolean = false
) {

    var retrofit: Retrofit
    var service: UnifonicNotificationService

    init {
        retrofit = getPublicRetrofitClient()
        service = retrofit.create(UnifonicNotificationService::class.java)
    }

    fun reset() {
        context.reset()
    }

    fun register(appId: String, identifier: String, callResultHandler: CallResultHandler?) {

        enqueueCallback(
            service.register(appId.toRequestBody(), identifier.toRequestBody()),
            getResponseHandler(callResultHandler)
        ) {
            it?.let {
                if (it.get("sdk_token").toString() != null.toString())
                    it.get("sdk_token")?.asString?.let {
                        context.saveSdkToken(it)
                        context.saveIdentifier(identifier)
                        context.saveAppId(appId)
                    }

            }
        }
    }

    fun disableNotification(
        firebaseToken: String,
        disabled: Boolean,
        callResultHandler: CallResultHandler?
    ) {
        enqueueCallback(
            service.disableNotification(
                firebaseToken.toRequestBody(),
                (if (disabled) "disabled" else "enabled").toRequestBody()
            ), getResponseHandler(callResultHandler)
        ) {
            it?.let {
                context.apply {
                    if (disabled) {
                        notificationsDisableStatusNotified(true)
                    } else {
                        notificationsDisableStatusNotified(false)
                    }
                }
            }
        }
    }

    fun saveToken(
        firebaseToken: String,
        create: Boolean = false, oldFirebaseToken: String? = null,
        callResultHandler: CallResultHandler?
    ) {

        enqueueCallback(
            if (create.not()) service.updateToken(
                oldFirebaseToken?.toRequestBody(),
                firebaseToken.toRequestBody()
            )
            else service.saveToken(
                context.getFirebaseToken()?.toRequestBody(),
                context.getIdentifier()?.toRequestBody(),
                "fcm".toRequestBody()
            ),
            getResponseHandler(callResultHandler)
        ) {
            if (create)
                context.saveFirebaseToken(firebaseToken)
            context.userRegisteredWithFirebaseToken()
        }
    }

    fun markNotification(
        messageId: String,
        status: NotificationReadType,
        callResultHandler: CallResultHandler?
    ) {

        enqueueCallback(
            if (status == NotificationReadType.READ)
                service.markNotificationRead(messageId.toRequestBody())
            else service.markNotificationReceived(messageId.toRequestBody()),
            getResponseHandler(callResultHandler)
        ) {
            it?.let {
                context.userRegisteredWithFirebaseToken()

            }
        }
    }

    private var responseHandler: ResponseHandler? = null
    private fun getResponseHandler(callResultHandler: CallResultHandler?): ResponseHandler? {
        try {
            responseHandler = object : ResponseHandler {
                override fun onSuccess(response: Response<*>) {
                    callResultHandler!!.onResult(response)
                }

                override fun onError(errorResponse: Response<*>) {
                    if (errorResponse.errorBody() != null)
                        callResultHandler!!.onError(
                            errorResponse.errorBody()?.string().toString(),
                            0
                        )
                    else callResultHandler!!.onError(errorResponse.message(), 0)
                }

                override fun onError(errorMsg: String) {
                    try {
                        callResultHandler!!.onError(errorMsg, 0)
                    } catch (ex: Exception) {

                    }
                }
            }
        } catch (ex: Exception) {

        }
        return responseHandler
    }
    ;
    private fun <T> enqueueCallback(
        call: Call<T>,
        responseHandler: ResponseHandler?,
        onSuccess: (T?) -> Unit
    ) {
        try {
            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    responseHandler!!.onError(t.message!!)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful && response.code() <= 204)
                        onSuccess.invoke(response.body())
                    processResponse(response, responseHandler)
                }
            })
        } catch (ex: Exception) {
            responseHandler!!.onError(ex.message!!)
        }
    }

    private fun processResponse(response: Response<*>, responseHandler: ResponseHandler?) {
        try {
            if (response.isSuccessful) {
                responseHandler!!.onSuccess(response)
            } else {
                responseHandler!!.onError(response)
            }
        } catch (ex: Exception) {
            responseHandler!!.onError(ex.message!!)
        }
    }


    interface ResponseHandler {
        fun onSuccess(response: Response<*>)
        fun onError(errorResponse: Response<*>)
        fun onError(errorMsg: String)
    }


    private fun getPublicRetrofitClient(): Retrofit {

        val client = OkHttpClient.Builder()

        client.addInterceptor { chain: Interceptor.Chain? ->

            val request: Request? = chain?.request()
                ?.newBuilder().apply {
                    this?.addHeader("Accept-Language", Locale.getDefault().language)
                    context.getSdkToken()?.let {
                        this?.addHeader("Authorization", "Bearer ${it}")
                    }
                    context.getAppId()?.let {
                        this?.addHeader("x-notice-app-id", it)
                    }
                }
                ?.build()

            chain?.proceed(request!!)!!
        }
        if (enableLoggingInterceptor) {

            val logging = HttpLoggingInterceptor()

            logging.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(logging)
        }

        client.connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()
    }

}