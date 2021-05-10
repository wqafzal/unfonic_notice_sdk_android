package octek.android.unifonicsdk

import retrofit2.Response

interface CallResultHandler {
    fun onResult(response: Response<*>)
    fun onError( errorMsg: String, responseCode: Int)
}