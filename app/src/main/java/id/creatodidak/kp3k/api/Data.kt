package id.creatodidak.kp3k.api

import id.creatodidak.kp3k.api.model.AddOwner
import id.creatodidak.kp3k.api.model.BasicResponse
import id.creatodidak.kp3k.api.model.LoginRequest
import id.creatodidak.kp3k.api.model.MBasicData
import id.creatodidak.kp3k.api.model.MDataPerkembangan
import id.creatodidak.kp3k.api.model.MDataPerkembanganItem
import id.creatodidak.kp3k.api.model.MListLahanOwner
import id.creatodidak.kp3k.api.model.MLogin
import id.creatodidak.kp3k.api.model.MMyLahan
import id.creatodidak.kp3k.api.model.MOwner
import id.creatodidak.kp3k.api.model.MOwnerAddLahan
import id.creatodidak.kp3k.api.model.MOwnerItem
import id.creatodidak.kp3k.api.model.MRealisasiTanam
import id.creatodidak.kp3k.api.model.OwnerItem
import id.creatodidak.kp3k.api.model.newLahan
import id.creatodidak.kp3k.dashboard.ui.lahantugas.AddLahan
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface Data {
    @GET("/android/basicdata")
    suspend fun getBasicData(): MBasicData

    @GET("/android/lahantugas/{nrp}")
    suspend fun getMyLahan(@Path("nrp") nrp: String): MMyLahan

    @GET("/android/getowner/{nrp}")
    suspend fun getOwner(
        @Path("nrp") nrp: String
    ): List<MOwnerItem>

    @GET("/android/getowner/{type}/{nrp}")
    suspend fun getOwnerByType(
        @Path("type") type: String,
        @Path("nrp") nrp: String
    ): MOwnerAddLahan

    @POST("/android/add/pemiliklahan")
    suspend fun sendNewPemilikLahan(@Body request: AddOwner): BasicResponse

    @POST("/android/add/lahan")
    suspend fun sendNewLahan(@Body request: newLahan): BasicResponse

    @GET("/android/lahan/{id_lahan}")
    suspend fun getOwnerLahan(@Path("id_lahan") id_lahan: String): MListLahanOwner

    @GET("/android/datatanam/bylahan/{id_lahan}")
    suspend fun getDataTanamOnLahan(@Path("id_lahan") id_lahan: String): MRealisasiTanam

    @Multipart
    @POST("/android/datatanam/add")
    suspend fun uploadLaporanTanam(
        @Part("kodelahan") kodelahan: RequestBody,
        @Part("luastanam") luastanam: RequestBody,
        @Part("prediksipanen") prediksipanen: RequestBody,
        @Part("komoditas") komoditas: RequestBody,
        @Part("varietas") varietas: RequestBody,
        @Part foto1: MultipartBody.Part,
        @Part foto2: MultipartBody.Part,
        @Part foto3: MultipartBody.Part,
        @Part foto4: MultipartBody.Part,
        @Part video: MultipartBody.Part? = null // opsional
    ): Response<ResponseBody>

    @Multipart
    @POST("/android/dataperkembangan/add")
    suspend fun uploadLaporanPerkembanganTanam(
        @Part("kodelahan") kodelahan: RequestBody,
        @Part("tanaman_id") tanaman_id: RequestBody,
        @Part("tinggitanaman") tinggitanaman: RequestBody,
        @Part("kondisitanah") kondisitanah: RequestBody,
        @Part("warnadaun") warnadaun: RequestBody,
        @Part("curahhujan") curahhujan : RequestBody,
        @Part("hama") hama : RequestBody,
        @Part("keteranganhama") keteranganhama : RequestBody,
        @Part("keterangan") keterangan : RequestBody,
        @Part foto1: MultipartBody.Part,
        @Part foto2: MultipartBody.Part,
        @Part foto3: MultipartBody.Part,
        @Part foto4: MultipartBody.Part,
        @Part video: MultipartBody.Part? = null
    ): Response<ResponseBody>

    @GET("/android/perkembangan/{kodelahan}}/{tanaman_id}")
    suspend fun getDataPerkembangan(@Path("kodelahan") kodelahan: String, @Path("tanaman_id") tanaman_id : String): List<MDataPerkembanganItem>

    @Multipart
    @POST("/android/fotoprofile")
    suspend fun uploadFotoProfile(
        @Part("nrp") nrp: RequestBody,
        @Part foto: MultipartBody.Part,
    ): BasicResponse

    @GET("/android/verifikasi/{nrp}/{nohp}")
    suspend fun getKodeVerifikasi(@Path("nrp") nrp: String, @Path("nohp") nohp : String): BasicResponse

    @GET("/android/verifikasi/auth/{nrp}/{kode}")
    suspend fun sendKodeVerifikasi(@Path("nrp") nrp: String, @Path("kode") kode : String): BasicResponse
}