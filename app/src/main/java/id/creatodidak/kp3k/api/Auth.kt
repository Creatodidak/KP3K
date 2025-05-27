package id.creatodidak.kp3k.api

import com.google.gson.annotations.SerializedName
import id.creatodidak.kp3k.api.model.LoginKapolres
import id.creatodidak.kp3k.api.model.LoginPamatwil
import id.creatodidak.kp3k.api.model.LoginPimpinan
import id.creatodidak.kp3k.api.model.LoginRequest
import id.creatodidak.kp3k.api.model.MLogin
import id.creatodidak.kp3k.api.model.PINRegister
import id.creatodidak.kp3k.api.model.TokenRegister
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Auth {
    @POST("/auth/loginandroid")
    suspend fun login(@Body request: LoginRequest): MLogin

    @POST("/auth/loginpimpinan")
    suspend fun loginPimpinan(@Body request: LoginRequest): LoginPimpinan

    @POST("/auth/loginpamatwil")
    suspend fun loginPamatwil(@Body request: LoginRequest): LoginPamatwil

    @POST("/auth/loginkapolres")
    suspend fun loginKapolres(@Body request: LoginRequest): LoginKapolres

    @POST("/auth/registerfcm")
    suspend fun registerFcm(@Body request: TokenRegister): MLogin

    data class TokenRegisterPimpinan(
        @SerializedName("username") val username: String,
        @SerializedName("token") val token: String,
    )
    @POST("/auth/registerfcmpimpinan")
    suspend fun registerFcmPimpinan(@Body request: TokenRegisterPimpinan): Response<ResponseBody>
    @POST("/auth/registerfcmpamatwil")
    suspend fun registerFcmPamatwil(@Body request: TokenRegisterPimpinan): Response<ResponseBody>
    @POST("/auth/registerfcmkapolres")
    suspend fun registerFcmKapolres(@Body request: TokenRegisterPimpinan): Response<ResponseBody>
    @POST("/auth/registerpin")
    suspend fun registerPin(@Body request: PINRegister): Response<ResponseBody>

    data class PINRegisterPimpinan(
        @SerializedName("username") val username: String,
        @SerializedName("pin") val pin: String,
    )

    @POST("/auth/registerpinpimpinan")
    suspend fun registerPinPimpinan(@Body request: PINRegisterPimpinan): Response<ResponseBody>
    @POST("/auth/registerpinpamatwil")
    suspend fun registerPinPamatwil(@Body request: PINRegisterPimpinan): Response<ResponseBody>
    @POST("/auth/registerpinkapolres")
    suspend fun registerPinKapolres(@Body request: PINRegisterPimpinan): Response<ResponseBody>
}