package com.unifonic.noticesdk.network.rest


import com.unifonic.noticesdk.models.requestmodels.Channel
import com.unifonic.noticesdk.models.requestmodels.RegisterDevice
import retrofit2.http.Body
import retrofit2.http.POST

interface UnifonicNotificationService {

    //Suppose to return device id
    // in 'identifier' key
    @POST("apps/register")
    suspend fun register(@Body device: RegisterDevice): HashMap<String, String>

    @POST("bindings/update_status")
    suspend fun disableNotification(@Body forChannel: Channel): Any

    @POST("bindings")
    suspend fun saveToken(@Body body: HashMap<String, String>): Any

    @POST("bindings/refresh")
    suspend fun updateToken(@Body forChannel: HashMap<String, Any>): Any

    @POST("bindings/refresh")
    suspend fun markNotification(@Body forChannel: HashMap<String, Any>): Any

    @POST("notifications/read")
    suspend fun markNotificationRead(@Body map: HashMap<String, Any>): Any

    @POST("notifications/received")
    suspend fun markNotificationReceived(@Body map: HashMap<String, Any>): Any
}