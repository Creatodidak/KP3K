package id.creatodidak.kp3k.api

import com.google.gson.annotations.SerializedName
import id.creatodidak.kp3k.api.model.AddOwner
import id.creatodidak.kp3k.api.model.BasicResponse
import id.creatodidak.kp3k.api.model.MBasicData
import id.creatodidak.kp3k.api.model.MDataPerkembanganItem
import id.creatodidak.kp3k.api.model.MLahanMitra
import id.creatodidak.kp3k.api.model.MMyLahan
import id.creatodidak.kp3k.api.model.MNewOwnerItem
import id.creatodidak.kp3k.api.model.MRealisasiItem
import id.creatodidak.kp3k.api.model.MRealisasiPanen
import id.creatodidak.kp3k.api.model.MRealisasiTanam
import id.creatodidak.kp3k.api.model.RAtensiItem
import id.creatodidak.kp3k.api.model.SocketDataPeta
import id.creatodidak.kp3k.api.model.addAtensiItem
import id.creatodidak.kp3k.api.model.newLahan
import id.creatodidak.kp3k.api.model.pimpinan.DataKontak
import id.creatodidak.kp3k.api.model.pimpinan.KabupatenSummaryByMasaTanam
import id.creatodidak.kp3k.api.model.pimpinan.KabupatenSummaryMonthly
import id.creatodidak.kp3k.api.model.pimpinan.RBasicPimpinan
import id.creatodidak.kp3k.api.model.pimpinan.RDetailLahan
import id.creatodidak.kp3k.api.model.pimpinan.RDetailPersonil
import id.creatodidak.kp3k.api.model.pimpinan.ResCall
import id.creatodidak.kp3k.api.model.pimpinan.ValDataKontak
import id.creatodidak.kp3k.api.model.pimpinan.ValDataWilayah
import id.creatodidak.kp3k.api.model.pimpinan.listWilayah
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface DataPimpinan {
    @GET("/p/datapeta/all")
    suspend fun getPetaData(): SocketDataPeta

    @GET("/p/datapeta/wilayah/{kode}")
    suspend fun getPetaDataWilayah(@Path("kode") kode: String): SocketDataPeta

    @GET("/p/basic")
    suspend fun getBasic(): RBasicPimpinan

    @GET("/p/basic/{kode}")
    suspend fun getBasicWilayah(@Path("kode") kode: String): RBasicPimpinan

    @GET("/p/lahandetails/{kode}")
    suspend fun getDetailLahan(@Path ("kode") kode: String): RDetailLahan

    @GET("/p/persdetails/{nrp}")
    suspend fun getDetailPers(@Path ("nrp") nrp: String): RDetailPersonil

    data class CallPimpinan(
        @SerializedName("target") val target: String,
        @SerializedName("caller") val caller: String,
        @SerializedName("role") val role: String,
    )

    @POST("/p/call")
    suspend fun callPersonil(@Body request: CallPimpinan): ResCall

    @POST("/p/loadkontak")
    suspend fun loadKontakApi(@Body request: ValDataKontak): List<DataKontak>

    @POST("/p/wilayah")
    suspend fun loadWilayah(@Body request: ValDataWilayah): List<listWilayah>

    @POST("/p/atensi")
    suspend fun addAtensi(
        @Body request: addAtensiItem
    ): Response<ResponseBody>

    data class Kabs(
        @SerializedName("kab") val kab: String? = null,
    )

    @POST("/p/sumdatalahan")
    suspend fun getDataLahanByMasaTanam(
        @Body request: Kabs
    ): List<KabupatenSummaryByMasaTanam>

    @POST("/p/sumdatalahanmonthly")
    suspend fun getDataLahanByBulan(
        @Body request: Kabs
    ): List<KabupatenSummaryMonthly>
}