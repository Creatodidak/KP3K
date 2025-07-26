package id.creatodidak.kp3k.api

import id.creatodidak.kp3k.api.RequestClass.PerkembanganAddRequest
import id.creatodidak.kp3k.api.RequestClass.PerkembanganIdRequest
import id.creatodidak.kp3k.api.RequestClass.PerkembanganIdsRequest
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.api.newModel.PerkembanganCreateUpdateResponse
import id.creatodidak.kp3k.api.newModel.PerkembanganResponseItem
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PerkembanganEndpoint {
    @POST("/perkembangan/bytanamanid")
    suspend fun getAllPerkembanganOnLahan(
        @Body request: PerkembanganIdRequest
    ): Response<List<PerkembanganResponseItem>>

    @POST("/perkembangan/bytanamanids")
    suspend fun getAllPerkembanganOnLahans(
        @Body request: PerkembanganIdsRequest
    ): Response<List<PerkembanganResponseItem>>

    @GET("/perkembangan/detail/{id}")
    suspend fun getPerkembanganDetail(
        @Path("id") id: Int
    ):Response<PerkembanganResponseItem>

    @GET("/perkembangan/status/{status}")
    suspend fun getPerkembanganByStatus(
        @Path("status") status: String
    ):Response<List<PerkembanganResponseItem>>

    @DELETE("/perkembangan/delete/{id}")
    suspend fun deletePerkembanganById(
        @Path("id") id: Int
    ): Response<ResponseBody>

    @POST("/perkembangan/add")
    suspend fun addPerkembangan(
        @Body request: PerkembanganAddRequest
    ): Response<PerkembanganCreateUpdateResponse>

    @PATCH("/perkembangan/verifikasi/{id}")
    suspend fun verifikasiPerkembangan(
        @Path("id") id: String,
        @Body VerifikasiRequest: VerifikasiRequest
    ): Response<PerkembanganCreateUpdateResponse>
}