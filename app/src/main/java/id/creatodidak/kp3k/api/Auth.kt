package id.creatodidak.kp3k.api

import id.creatodidak.kp3k.api.model.LoginRequest
import id.creatodidak.kp3k.api.model.MLogin
import id.creatodidak.kp3k.api.model.PINRegister
import id.creatodidak.kp3k.api.model.TokenRegister
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Auth {
    @POST("/auth/loginandroid")
    suspend fun login(@Body request: LoginRequest): MLogin

    @POST("/auth/registerfcm")
    suspend fun registerFcm(@Body request: TokenRegister): MLogin

    @POST("/auth/registerpin")
    suspend fun registerPin(@Body request: PINRegister): MLogin
}