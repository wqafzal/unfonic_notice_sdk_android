package com.unifonic.noticesdk.ui

import androidx.lifecycle.MutableLiveData
import com.unifonic.noticesdk.AndroidApp
import com.unifonic.noticesdk.base.BaseViewModel
import com.unifonic.noticesdk.extensions.*
import com.unifonic.noticesdk.models.NotificationReadType
import com.unifonic.noticesdk.models.requestmodels.Channel
import com.unifonic.noticesdk.models.requestmodels.RegisterDevice
import com.unifonic.noticesdk.network.ApiResponseResource
import com.unifonic.noticesdk.network.rest.UnifonicNotificationService
import javax.inject.Inject

class DashBoardViewModel @Inject constructor(
    app: AndroidApp,
    val service: UnifonicNotificationService
) : BaseViewModel(app) {

    //@param app_id entered by user
    //@param identifier entered by user
    fun register(appId: String, identifier: String) =
        MutableLiveData<ApiResponseResource<Any>>(ApiResponseResource.loading()).apply {
            launchApi {
                kotlin.runCatching { service.register(RegisterDevice(appId, identifier)) }
                    .onSuccess {
                        it["sdk_token"]?.let { it1 ->
                            app.saveSdkToken(it1)
                            app.saveIdentifier(identifier)
                            app.saveAppId(appId)
                            it1
                        }
                            ?: return@launchApi postValue(
                                ApiResponseResource.error("Unable to register.")
                            )
                        //save all three -> sdk token, identifier, app_id
                        postValue(ApiResponseResource.success(it))
                    }
                    .onFailure { postValue(ApiResponseResource.error(onHandleError(it))) }
            }
        }

    fun disableNotification(disabled:Boolean) =
        MutableLiveData<ApiResponseResource<Any>>(ApiResponseResource.loading()).apply {
            launchApi {
                kotlin.runCatching {
                    service.disableNotification(app.getFirebaseToken()?.let {
                        Channel(
                            it, if (disabled) "disabled" else "enabled"
                        )
                    }
                        ?: return@launchApi postValue(ApiResponseResource.error("Device not registered for notifications.")))
                }
                    .onSuccess { postValue(ApiResponseResource.success(it)) }
                    .onFailure { postValue(ApiResponseResource.error(onHandleError(it))) }
            }
        }


    fun saveToken(update: Boolean = false) =
        MutableLiveData<ApiResponseResource<Any>>(ApiResponseResource.loading()).apply {
            launchApi {
                kotlin.runCatching {
                    if (update) service.updateToken(
                        hashMapOf(
                            Pair(
                                "old_address",
                                app.getOldFirebaseToken() ?: return@launchApi postValue(
                                    ApiResponseResource.error("old token not found"))
                            ),
                            Pair(
                                "address",
                                app.getFirebaseToken() ?: return@launchApi postValue(
                                    ApiResponseResource.error("new token not found"))

                            ),
                        )
                    ) else service.saveToken(
                        hashMapOf(
                            Pair(
                                "address",
                                app.getFirebaseToken() ?: return@launchApi postValue(
                                    ApiResponseResource.error("token not found"))

                            ),
                            Pair(
                                "identifier",
                                app.getIdentifier() ?: return@launchApi postValue(
                                    ApiResponseResource.error("identifier not found"))

                            ),
                            Pair(
                                "type",
                                "apn"
                            ),
                        )
                    )
                }
                    .onSuccess { postValue(ApiResponseResource.success(it)) }
                    .onFailure { postValue(ApiResponseResource.error(onHandleError(it))) }
            }
        }

    fun markNotification(messageId: String, status: NotificationReadType) =
        MutableLiveData<ApiResponseResource<Any>>(ApiResponseResource.loading()).apply {
            launchApi {
                kotlin.runCatching {
                    if (status == NotificationReadType.READ)
                        service.markNotificationRead(hashMapOf(Pair("message_id", messageId)))
                    else service.markNotificationReceived(hashMapOf(Pair("message_id", messageId)))
                }
                    .onSuccess { postValue(ApiResponseResource.success(it)) }
                    .onFailure { postValue(ApiResponseResource.error(onHandleError(it))) }
            }
        }

}