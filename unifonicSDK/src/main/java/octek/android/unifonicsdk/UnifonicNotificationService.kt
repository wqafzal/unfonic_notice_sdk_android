package octek.android.unifonicsdk


import com.google.gson.JsonObject
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UnifonicNotificationService {

    //Suppose to return device id
    // in 'identifier' key
    @Multipart
    @POST("apps/register")
    fun register(
        @Part("app_id") appId: RequestBody,
        @Part("identifier") identifier: RequestBody
    ): Call<JsonObject>

    @Multipart
    @POST("bindings/update_status")
    fun disableNotification(
        @Part("address") address: RequestBody,
        @Part("status") status: RequestBody,
    ): Call<JsonObject>

    @Multipart
    @POST("bindings")
    fun saveToken(
        @Part("address") address: RequestBody?,
        @Part("identifier") identifier: RequestBody?,
        @Part("type") type: RequestBody?,
    ): Call<JsonObject>

    @Multipart
    @POST("bindings/refresh")
    fun updateToken(
        @Part("old_address") oldAddress: RequestBody?,
        @Part("address") newAddress: RequestBody?
    ): Call<JsonObject>

    @Multipart
    @POST("notifications/read")
    fun markNotificationRead(@Part("message_id") messageId: RequestBody): Call<JsonObject>

    @Multipart
    @POST("notifications/received")
    fun markNotificationReceived(@Part("message_id") messageId: RequestBody): Call<JsonObject>
}