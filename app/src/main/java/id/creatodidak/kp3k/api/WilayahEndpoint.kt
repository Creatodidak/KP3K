package id.creatodidak.kp3k.api

import com.google.gson.annotations.SerializedName
import id.creatodidak.kp3k.api.RequestClass.TanamanByMasaTanamRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanByTanggalTanamRangeRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanByTanggalTanamRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanIdRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanIdsRequest
import id.creatodidak.kp3k.api.newModel.DesaResponseItem
import id.creatodidak.kp3k.api.newModel.KabupatenResponseItem
import id.creatodidak.kp3k.api.newModel.KecamatanResponseItem
import id.creatodidak.kp3k.api.newModel.ProvinsiResponseItem
import id.creatodidak.kp3k.api.newModel.TanamanResponseItem
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WilayahEndpoint {
    data class wilayahRequest(
        @SerializedName("ids") val ids: List<Int>
    )

    @POST("/polri/export/provinsi")
    suspend fun getProvinsi(
        @Body request: wilayahRequest
    ): Response<ResponseBody>

    @POST("/polri/export/kabupaten")
    suspend fun getKabupaten(
        @Body request: wilayahRequest
    ): Response<ResponseBody>

    @POST("/polri/export/kecamatan")
    suspend fun getKecamatan(
        @Body request: wilayahRequest
    ): Response<ResponseBody>

    @POST("/polri/export/desa")
    suspend fun getDesa(
        @Body request: wilayahRequest
    ): Response<ResponseBody>

    @GET("/polri/satkerdata/{type}/{nrp}")
    suspend fun getSatkerData(
        @Path("type") type: String,
        @Path("nrp") nrp: String
    ): Response<ResponseBody>

    @GET("/polri/polsekpivot/{type}/{nrp}")
    suspend fun getPolsekPivot(
        @Path("type") type: String,
        @Path("nrp") nrp: String
    ): Response<ResponseBody>
}
