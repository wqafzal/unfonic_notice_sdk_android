package octek.android.unifonicsdk.models

import com.google.gson.annotations.SerializedName

enum class NotificationReadType {
    @SerializedName("read")
    READ,
    @SerializedName("received")
    RECEIVED
}