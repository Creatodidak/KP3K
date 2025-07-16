package id.creatodidak.kp3k.api

import id.creatodidak.kp3k.api.RequestClass.PanenByTanggalPanenRangeRequest
import id.creatodidak.kp3k.api.RequestClass.PanenByTanggalPanenRequest
import id.creatodidak.kp3k.api.RequestClass.PanenIdRequest
import id.creatodidak.kp3k.api.RequestClass.PanenIdsRequest
import id.creatodidak.kp3k.api.newModel.PanenResponseItem
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PanenEndpoint {
    @POST("/panen/bytanamanid")
    suspend fun getAllPanenOnLahan(
        @Body request: PanenIdRequest
    ): Response<List<PanenResponseItem>>

    @POST("/panen/bytanamanids")
    suspend fun getAllPanenOnLahans(
        @Body request: PanenIdsRequest
    ): Response<List<PanenResponseItem>>

    @POST("/panen/bytanamanid/bytanggalpanen")
    suspend fun getAllPanenOnLahanByTanggalPanen(
        @Body request: PanenByTanggalPanenRequest
    ): Response<List<PanenResponseItem>>

    @POST("/panen/bytanamanid/bytanggalpanenrange")
    suspend fun getAllPanenOnLahanByTanggalPanenRange(
        @Body request: PanenByTanggalPanenRangeRequest
    ): Response<List<PanenResponseItem>>

    @GET("/panen/detail/{id}")
    suspend fun getPanenDetail(
        @Path("id") id: Int
    ):Response<PanenResponseItem>

    @GET("/panen/status/{status}")
    suspend fun getPanenByStatus(
        @Path("status") status: String
    ):Response<List<PanenResponseItem>>

    @DELETE("/panen/delete/{id}")
    suspend fun deletePanenById(
        @Path("id") id: Int
    ): Response<ResponseBody>
}