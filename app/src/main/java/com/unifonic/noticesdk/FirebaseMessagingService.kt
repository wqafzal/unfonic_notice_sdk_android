package com.unifonic.noticesdk

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.AsyncTask
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.unifonic.noticesdk.di.modules.ServiceModule
import com.unifonic.noticesdk.extensions.getBaseUrl
import octek.android.unifonicsdk.extensions.saveFirebaseToken
import com.unifonic.noticesdk.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import octek.android.unifonicsdk.UnifonicSDK
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var service: octek.android.unifonicsdk.UnifonicNotificationService


    val app: AndroidApp
        get() = application as AndroidApp

    private val component by lazy {
        app.component.plus(ServiceModule(this))
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (ProcessLifecycleOwner.get().lifecycle.currentState != Lifecycle.State.RESUMED) {
            updateNotificationStatus(message.data["uni_message_id"])

            NotificationManagerCompat.from(this).notify(
                System.currentTimeMillis().div(1000.times(1000)).toInt(),
                (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) Notification.Builder(
                    this,
                    AndroidApp.instance.createNotificationChannel().id
                ).setSmallIcon(
                    Icon.createWithResource(
                        this,
                        R.drawable.ic_launcher_foreground
                    )
                ) else Notification.Builder(this))
                    .setContentTitle(message.data["uni_title"])
                    .setContentText(message.data["uni_body"])
                    .setContentIntent(
                        PendingIntent.getActivity(
                            this,
                            0,
                            Intent(this, MainActivity::class.java).putExtra(
                                MainActivity.PAYLOAD,
                                message.data["uni_message_id"]
                            ),
                            0
                        )
                    )
                    .setAutoCancel(true)
                    .build()
            )
        } else {
            sendBroadcast(
                Intent(MainActivity.NOTIFICATION_RECEIVED).putExtra(
                    MainActivity.PAYLOAD,
                    message.data["uni_message_id"]
                )
            )
        }
    }

    private fun updateNotificationStatus(messageId: String?) {

        UnifonicSDK(getBaseUrl()?:BuildConfig.BASE_URL,this, true)
        ProcessLifecycleOwner.get().lifecycle.coroutineScope.launch(Dispatchers.IO) {
            messageId?.let {
                kotlin.runCatching {
                    service.markNotificationReceived(messageId.toRequestBody()).execute()
                }.onSuccess {
                    it.body().toString()
                }.onFailure {
                    it
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        saveFirebaseToken(token)
    }
}