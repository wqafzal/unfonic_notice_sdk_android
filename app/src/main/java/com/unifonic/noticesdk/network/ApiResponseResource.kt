package com.unifonic.noticesdk.network


import androidx.annotation.Nullable
import com.unifonic.noticesdk.network.Status.ERROR
import com.unifonic.noticesdk.network.Status.LOADING
import com.unifonic.noticesdk.network.Status.NO_MORE_ITEM
import com.unifonic.noticesdk.network.Status.SUCCESS


class ApiResponseResource<T> private constructor(val status: Int, @param:Nullable @field:Nullable val message: String?) {

    var data: T? = null


    constructor(status: Int, response: T, message: String?) : this(status, message) {
        this.data = response
    }


    companion object {

        fun <T> success(data: T): ApiResponseResource<T> {
            return ApiResponseResource(SUCCESS, data, "success")
        }

        fun <T> error(msg: String): ApiResponseResource<T> {
            return ApiResponseResource(ERROR, msg)
        }

        fun <T> loading(@Nullable msg: String): ApiResponseResource<T> {
            return ApiResponseResource(LOADING, msg)
        }

        fun <T> loading(): ApiResponseResource<T> {
            return ApiResponseResource(LOADING, "loading")
        }

        fun <T> noMoreItem(): ApiResponseResource<T> {
            return ApiResponseResource(NO_MORE_ITEM, "no more items exist")
        }
    }

}