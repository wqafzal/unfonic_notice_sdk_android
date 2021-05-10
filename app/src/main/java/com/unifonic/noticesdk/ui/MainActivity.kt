package com.unifonic.noticesdk.ui

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.unifonic.noticesdk.BuildConfig
import com.unifonic.noticesdk.R
import com.unifonic.noticesdk.base.BaseActivity
import com.unifonic.noticesdk.base.BaseViewModel
import com.unifonic.noticesdk.di.ViewModelFactory
import com.unifonic.noticesdk.extensions.*
import kotlinx.android.synthetic.main.activity_main.*
import octek.android.unifonicsdk.CallResultHandler
import octek.android.unifonicsdk.UnifonicSDK
import octek.android.unifonicsdk.extensions.reset
import octek.android.unifonicsdk.models.NotificationReadType
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Response
import javax.inject.Inject


class MainActivity : BaseActivity() {

    companion object {
        const val NOTIFICATION_RECEIVED = "NOTIFICATION_RECEIVED"
        val NOTIFICATION_RECEIVED_INTENT_FILTER = IntentFilter(NOTIFICATION_RECEIVED)
        const val PAYLOAD = "payload"
    }

    override fun inject() {
        component.inject(this)
    }

    lateinit var sdk: UnifonicSDK

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: DashBoardViewModel

    override fun getViewModel(): BaseViewModel? = viewModel

    override fun layoutId(): Int? = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[DashBoardViewModel::class.java]
        etUrl.setText(getBaseUrl() ?: BuildConfig.BASE_URL)
        if (getBaseUrl() == null)
            btnInit.setOnClickListener {
                initSDK()
            }
        else {

            initSDK()
        }
        btnReset.setOnClickListener {
            FirebaseMessaging.getInstance().deleteToken()
            reset()
            recreate()
        }
    }

    private fun initSDK() {
        if (etUrl.text?.isEmpty() == true || kotlin.runCatching {
                etUrl.text.toString().toHttpUrl()
                    .let { if (it.host.split(".").size < 2) throw Exception("invalid Url") }
            }.isFailure) {
            Toast.makeText(this, "Invalid Url!", Toast.LENGTH_SHORT).show()
            return
        }

        btnInit.isEnabled = false
        btnInit.visibility = View.INVISIBLE
        btnReset.isVisible = true
        ilServerUrl.isEnabled = false
        hider.isVisible = false
        btnRegister.isVisible = true
        sdk = UnifonicSDK(
            etUrl.text.toString().let {
                saveBaseUrl(it)
                it
            }, this, true
        )


        btnRegister.setOnClickListener {
            ilAppId.error = ""
            ilIdentifier.error = ""

            if (etAppId.text.toString().isBlank()) {
                ilAppId.error = "App id is mandatory"
            } else if (etIdentifier.text.toString().isBlank()) {
                ilIdentifier.error = "Identifier is required"
            } else {
                showLoading()
                //Register Device
                sdk.register(etAppId.text.toString(), etIdentifier.text.toString(), object :
                    CallResultHandler {
                    override fun onResult(response: Response<*>) {
                        hideLoading()
                        textView.append((response.body() as JsonObject).toString().plus("\n"))
                        init()

                    }

                    override fun onError(errorMsg: String, responseCode: Int) {
                        hideLoading()
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(notificationReceivedBroadCast, NOTIFICATION_RECEIVED_INTENT_FILTER)
        if (getBaseUrl() != null)
            init()
    }

    private fun init() {
        if (getSdkToken() != null) {
            btnRegister.isEnabled = false
            etAppId.setText(getAppId())
            etIdentifier.setText(getIdentifier())
            etFirebaseToken.setText(getFirebaseToken())
            etSdkKey.setText(getSdkToken())
            if (getFirebaseToken() == null) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener {
                    saveFirebaseToken(it.result)
                    //update Firebase token on server side
                    createUpdateToken(true)
                }
            } else if (getOldFirebaseToken() != null && getOldFirebaseToken() != getFirebaseToken()) {
                //update new Firebase token on server side
                createUpdateToken(false)
            } else if (isUserRegisteredWithFirebaseToken().not()) {
                //update Firebase token on server side if not registered
                createUpdateToken(true)
            } else if (intent.getStringExtra(PAYLOAD) != null) {
                //update notification status
                savePushNotificationStatus(intent?.extras?.getString(PAYLOAD), false)
                intent.removeExtra(PAYLOAD)
            } else if (areNotificationsEnabled(NotificationManagerCompat.from(this)).not()) {
                if (isNotificationsDisableStatusNotified().not())
                    notifyNotificationDisabled(true)
            } else {
                if (isNotificationsDisableStatusNotified())
                    notifyNotificationDisabled(false)
            }
        }
    }

    private fun notifyNotificationDisabled(enabled: Boolean) {
        sdk.disableNotification(getFirebaseToken()!!, enabled, object :
            CallResultHandler {
            override fun onResult(response: Response<*>) {
                hideLoading()
                textView.append((response.body() as JsonObject).toString().plus("\n"))
            }

            override fun onError(errorMsg: String, responseCode: Int) {
                hideLoading()
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.extras?.getString(PAYLOAD) != null) {
            this.intent.extras?.putString(PAYLOAD, intent.extras?.getString(PAYLOAD))
            intent.extras?.remove(PAYLOAD)
            init()
        }
    }

    private fun createUpdateToken(create: Boolean = false) {
        sdk.saveToken(getFirebaseToken()!!, create, getOldFirebaseToken(), object :
            CallResultHandler {
            override fun onResult(response: Response<*>) {
                hideLoading()
                textView.append((response.body() as JsonObject).toString().plus("\n"))
                init()
            }

            override fun onError(errorMsg: String, responseCode: Int) {
                hideLoading()
                textView.append(errorMsg.plus("\n"))


            }
        })
    }

    private fun savePushNotificationStatus(messageId: String?, fromBroadCast: Boolean) {
        sdk.markNotification(
            messageId ?: return Toast.makeText(
                this,
                "Invalid Message Id",
                Toast.LENGTH_LONG
            ).show(),
            if (fromBroadCast) NotificationReadType.RECEIVED else NotificationReadType.READ,
            object :
                CallResultHandler {
                override fun onResult(response: Response<*>) {
                    textView.append((response.body() as JsonObject).toString().plus("\n"))

                }

                override fun onError(errorMsg: String, responseCode: Int) {
                    textView.append(errorMsg.plus("\n"))
                }
            })
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(notificationReceivedBroadCast)
    }

    private val notificationReceivedBroadCast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            savePushNotificationStatus(intent?.getStringExtra(PAYLOAD), true)
        }
    }

    private fun areNotificationsEnabled(notificationManager: NotificationManagerCompat) = when {
        notificationManager.areNotificationsEnabled().not() -> false
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            notificationManager.notificationChannels.firstOrNull { channel ->
                channel.importance == NotificationManager.IMPORTANCE_NONE
            } == null
        }
        else -> true
    }
}