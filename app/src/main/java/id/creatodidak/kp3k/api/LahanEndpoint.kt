package id.creatodidak.kp3k.api

import com.google.gson.annotations.SerializedName
import id.creatodidak.kp3k.api.RequestClass.LahanAddRequest
import id.creatodidak.kp3k.api.RequestClass.LahanPatchRequest
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.api.model.LoginKapolres
import id.creatodidak.kp3k.api.model.LoginPamatwil
import id.creatodidak.kp3k.api.model.LoginPimpinan
import id.creatodidak.kp3k.api.model.LoginRequest
import id.creatodidak.kp3k.api.model.MLogin
import id.creatodidak.kp3k.api.model.PINRegister
import id.creatodidak.kp3k.api.model.TokenRegister
import id.creatodidak.kp3k.api.newModel.DefaultResponse
import id.creatodidak.kp3k.api.newModel.LahanCreateUpdateResponse
import id.creatodidak.kp3k.api.newModel.LahanResponse
import id.creatodidak.kp3k.api.newModel.LahanResponseItem
import id.creatodidak.kp3k.api.newModel.LoginResponse
import id.creatodidak.kp3k.api.newModel.NewLoginRequest
import id.creatodidak.kp3k.api.newModel.OTPRequest
import id.creatodidak.kp3k.api.newModel.OTPResponse
import id.creatodidak.kp3k.api.newModel.OwnerCreateUpdateResponse
import id.creatodidak.kp3k.api.newModel.OwnerResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LahanEndpoint {
    @GET("/lahan/detail/{id}")
    suspend fun getDetailLahan(
        @Path("id") id: String
    ): Response<LahanResponseItem>

    @POST("/lahan/wilayah/{type}")
    suspend fun getAllLahan(
        @Path("type") type: String,
        @Body RequestIds: OwnerEndpoint.RequestIds
    ): Response<List<LahanResponseItem>>

    @POST("/lahan/add")
    suspend fun addLahan(
        @Body LahanAddRequest: LahanAddRequest
    ): Response<LahanCreateUpdateResponse>

    @PATCH("/lahan/update/{id}")
    suspend fun updateLahan(
        @Path("id") id: String,
        @Body LahanAddRequest: LahanPatchRequest
    ): Response<LahanCreateUpdateResponse>

    @PATCH("/lahan/verifikasi/{id}")
    suspend fun verifikasiLahan(
        @Path("id") id: String,
        @Body VerifikasiRequest: VerifikasiRequest
    ): Response<LahanCreateUpdateResponse>

    @DELETE("/lahan/{id}")
    suspend fun deleteLahan(
        @Path("id") id: String
    ): Response<DefaultResponse>
}