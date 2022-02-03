package it.ipzs.cieidsdk.network.service

import io.reactivex.Single
import it.ipzs.cieidsdk.network.Endpoints
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface IdpService {

    @Headers("User-Agent: Mozilla/5.0")
    @FormUrlEncoded
    @POST(Endpoints.idp)
    fun callIdp(@FieldMap(encoded = true) values: Map<String, String>): Single<Response<ResponseBody>>


    @Headers("User-Agent: Mozilla/5.0")
    @FormUrlEncoded
    @POST(Endpoints.idpX509AuthIbrido)
    fun callIdpQr(@FieldMap(encoded = true) valuesQrCode: Map<String, String>): Single<Response<ResponseBody>>


    companion object {

        const val generaCodice: String = "generaCodice"
        const val authnRequest: String = "authnRequest"
    }
}
