package id.creatodidak.kp3k.api

import id.creatodidak.kp3k.api.model.LoginRequest
import id.creatodidak.kp3k.api.model.MBasicData
import id.creatodidak.kp3k.api.model.MLogin
import id.creatodidak.kp3k.api.model.MMyLahan
import id.creatodidak.kp3k.api.model.MUpdate
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Update {
    @GET("/android/update")
    suspend fun getLatestUpdate(): MUpdate
}