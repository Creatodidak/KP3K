package id.creatodidak.kp3k.api

import id.creatodidak.kp3k.api.model.LoginKapolres
import id.creatodidak.kp3k.api.model.LoginPamatwil
import id.creatodidak.kp3k.api.model.LoginPimpinan
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

    @POST("/auth/loginpimpinan")
    suspend fun loginPimpinan(@Body request: LoginRequest): LoginPimpinan

    @POST("/auth/loginpamatwil")
    suspend fun loginPamatwil(@Body request: LoginRequest): LoginPamatwil

    @POST("/auth/loginkapolres")
    suspend fun loginKapolres(@Body request: LoginRequest): LoginKapolres

    @POST("/auth/registerfcm")
    suspend fun registerFcm(@Body request: TokenRegister): MLogin

    @POST("/auth/registerpin")
    suspend fun registerPin(@Body request: PINRegister): MLogin
}