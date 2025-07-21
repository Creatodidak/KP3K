package id.creatodidak.kp3k.api

import com.google.gson.annotations.SerializedName
import id.creatodidak.kp3k.api.model.LoginKapolres
import id.creatodidak.kp3k.api.model.LoginPamatwil
import id.creatodidak.kp3k.api.model.LoginPimpinan
import id.creatodidak.kp3k.api.model.LoginRequest
import id.creatodidak.kp3k.api.model.MLogin
import id.creatodidak.kp3k.api.model.PINRegister
import id.creatodidak.kp3k.api.model.TokenRegister
import id.creatodidak.kp3k.api.newModel.LoginResponse
import id.creatodidak.kp3k.api.newModel.NewLoginRequest
import id.creatodidak.kp3k.api.newModel.OTPRequest
import id.creatodidak.kp3k.api.newModel.OTPResponse
import id.creatodidak.kp3k.api.newModel.PejabatLoginResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NewAuth {
    @POST("/auth/pers-login")
    suspend fun login(@Body request: NewLoginRequest): Response<LoginResponse>

    data class PejabatLoginRequest(
        @SerializedName("username") val username: String,
        @SerializedName("password") val password: String,
    )

    @POST("/auth/pejabat-login")
    suspend fun loginPejabat(@Body request: PejabatLoginRequest): Response<PejabatLoginResponse>

    @POST("/auth/cek-otp")
    suspend fun cekOTP(@Body request: OTPRequest): Response<OTPResponse>
}