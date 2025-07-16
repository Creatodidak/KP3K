package id.creatodidak.kp3k.api

import id.creatodidak.kp3k.api.RequestClass.InsertDataTanam
import id.creatodidak.kp3k.api.RequestClass.TanamanByMasaTanamRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanByTanggalTanamRangeRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanByTanggalTanamRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanIdRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanIdsRequest
import id.creatodidak.kp3k.api.RequestClass.UpdateDataTanam
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.api.newModel.DefaultResponse
import id.creatodidak.kp3k.api.newModel.LahanCreateUpdateResponse
import id.creatodidak.kp3k.api.newModel.TanamanCreateUpdateResponse
import id.creatodidak.kp3k.api.newModel.TanamanResponseItem
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TanamanEndpoint {
    @POST("/tanaman/bylahanid")
    suspend fun getAllTanamanOnLahan(
        @Body request: TanamanIdRequest
    ): Response<List<TanamanResponseItem>>

    @POST("/tanaman/bylahanids")
    suspend fun getAllTanamanOnLahans(
        @Body request: TanamanIdsRequest
    ): Response<List<TanamanResponseItem>>

    @POST("/tanaman/bylahanid/bymasatanam")
    suspend fun getAllTanamanOnLahanByMasaTanam(
        @Body request: TanamanByMasaTanamRequest
    ): Response<List<TanamanResponseItem>>

    @POST("/tanaman/bylahanid/bytanggaltanam")
    suspend fun getAllTanamanOnLahanByTanggalTanam(
        @Body request: TanamanByTanggalTanamRequest
    ): Response<List<TanamanResponseItem>>

    @POST("/tanaman/bylahanid/bytanggaltanamrange")
    suspend fun getAllTanamanOnLahanByTanggalTanamRange(
        @Body request: TanamanByTanggalTanamRangeRequest
    ): Response<List<TanamanResponseItem>>

    @GET("/tanaman/detail/{id}")
    suspend fun getTanamanDetail(
        @Path("id") id: Int
    ):Response<TanamanResponseItem>

    @GET("/tanaman/status/{status}")
    suspend fun getTanamanByStatus(
        @Path("status") status: String
    ):Response<List<TanamanResponseItem>>

    @DELETE("/tanaman/delete/{id}")
    suspend fun deleteTanamanById(
        @Path("id") id: Int
    ): Response<DefaultResponse>

    @POST("/tanaman/add")
    suspend fun addTanaman(
        @Body request: InsertDataTanam
    ): Response<TanamanCreateUpdateResponse>

    @PATCH("/tanaman/update/{id}")
    suspend fun updateTanaman(
        @Path("id") id: Int,
        @Body request: UpdateDataTanam
    ): Response<TanamanCreateUpdateResponse>

    @PATCH("/tanaman/verifikasi/{id}")
    suspend fun verifikasiTanaman(
        @Path("id") id: String,
        @Body VerifikasiRequest: VerifikasiRequest
    ): Response<TanamanCreateUpdateResponse>
}