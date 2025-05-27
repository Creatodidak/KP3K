package id.creatodidak.kp3k.api

import com.google.gson.annotations.SerializedName
import id.creatodidak.kp3k.api.model.AddOwner
import id.creatodidak.kp3k.api.model.BasicResponse
import id.creatodidak.kp3k.api.model.MDataPerkembanganItem
import id.creatodidak.kp3k.api.model.MLahanMitra
import id.creatodidak.kp3k.api.model.MMyLahan
import id.creatodidak.kp3k.api.model.MNewOwnerItem
import id.creatodidak.kp3k.api.model.MRealisasiItem
import id.creatodidak.kp3k.api.model.MRealisasiPanen
import id.creatodidak.kp3k.api.model.RAtensiItem
import id.creatodidak.kp3k.api.model.RMyLahanTugasItem
import id.creatodidak.kp3k.api.model.getAtensiVal
import id.creatodidak.kp3k.api.model.newLahan
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

interface Data {
    @GET("/android/lahan/mine/{kode}")
    suspend fun getMyLahanTUgas(@Path("kode") kode: String): List<RMyLahanTugasItem>

    @GET("/android/lahantugas/{nrp}")
    suspend fun getMyLahan(@Path("nrp") nrp: String): MMyLahan

    @GET("/android/mitra/desa/{desa_id}")
    suspend fun getOwner(
        @Path("desa_id") desa_id: String
    ): List<MNewOwnerItem>

    @POST("/android/mitra/add")
    suspend fun sendNewPemilikLahan(@Body request: AddOwner): BasicResponse

    @POST("/android/lahan/add")
    suspend fun sendNewLahan(@Body request: newLahan): BasicResponse

    @GET("/android/lahan/mitra/{owner_id}")
    suspend fun getOwnerLahan(@Path("owner_id") ownerid: String): MLahanMitra

    @GET("/android/realisasi/lahan/{id_lahan}")
    suspend fun getDataTanamOnLahan(@Path("id_lahan") id_lahan: String): List<MRealisasiItem>

    @GET("/android/realisasi/{id}")
    suspend fun getDataTanamById(@Path("id") id: String): MRealisasiItem

    @Multipart
    @POST("/android/realisasi/add")
    suspend fun uploadLaporanTanam(
        @Part("kodelahan") kodelahan: RequestBody,
        @Part("masatanam") masatanam: RequestBody,
        @Part("luastanam") luastanam: RequestBody,
        @Part("prediksipanen") prediksipanen: RequestBody,
        @Part("komoditas") komoditas: RequestBody,
        @Part("varietas") varietas: RequestBody,
        @Part foto1: MultipartBody.Part,
        @Part foto2: MultipartBody.Part,
        @Part foto3: MultipartBody.Part,
        @Part foto4: MultipartBody.Part,
    ): Response<ResponseBody>

    @Multipart
    @PUT("/android/realisasi/update")
    suspend fun updateLaporanTanam(
        @Part("id") id: RequestBody,
        @Part("masatanam") masatanam: RequestBody? = null,
        @Part("luastanam") luastanam: RequestBody? = null,
        @Part("prediksipanen") prediksipanen: RequestBody? = null,
        @Part("varietas") varietas: RequestBody? = null,
        @Part foto1: MultipartBody.Part? = null,
        @Part foto2: MultipartBody.Part? = null,
        @Part foto3: MultipartBody.Part? = null,
        @Part foto4: MultipartBody.Part? = null,
    ): Response<ResponseBody>

    @Multipart
    @POST("/android/perkembangan/add")
    suspend fun uploadLaporanPerkembanganTanam(
        @Part("kodelahan") kodelahan: RequestBody,
        @Part("tanaman_id") tanaman_id: RequestBody,
        @Part("tinggitanaman") tinggitanaman: RequestBody,
        @Part("kondisitanah") kondisitanah: RequestBody,
        @Part("warnadaun") warnadaun: RequestBody,
        @Part("curahhujan") curahhujan: RequestBody,
        @Part("hama") hama: RequestBody,
        @Part("keteranganhama") keteranganhama: RequestBody,
        @Part("keterangan") keterangan: RequestBody,
        @Part("ph") ph: RequestBody,
        @Part("kondisiair") kondisiAir: RequestBody,
        @Part("pupuk") pupuk: RequestBody,
        @Part("pestisida") pestisida: RequestBody,
        @Part("gangguanalam") gangguanAlam: RequestBody,
        @Part("gangguanlainnya") gangguanLainnya: RequestBody,
        @Part("keterangangangguanalam") keteranganGangguanAlam: RequestBody,
        @Part("keterangangangguanlainnya") keteranganGangguanLainnya: RequestBody,
        @Part foto1: MultipartBody.Part,
        @Part foto2: MultipartBody.Part,
        @Part foto3: MultipartBody.Part,
        @Part foto4: MultipartBody.Part,
        @Part video: MultipartBody.Part? = null
    ): Response<ResponseBody>

    @Multipart
    @PUT("/android/perkembangan/update")
    suspend fun uploadUpdateLaporanPerkembanganTanam(
        @Part("id") id: RequestBody,
        @Part("tinggitanaman") tinggitanaman: RequestBody? = null,
        @Part("kondisitanah") kondisitanah: RequestBody? = null,
        @Part("warnadaun") warnadaun: RequestBody? = null,
        @Part("curahhujan") curahhujan : RequestBody? = null,
        @Part("hama") hama : RequestBody? = null,
        @Part("keteranganhama") keteranganhama : RequestBody? = null,
        @Part("keterangan") keterangan : RequestBody? = null,
        @Part("ph") ph: RequestBody? = null,
        @Part("kondisiair") kondisiAir: RequestBody? = null,
        @Part("pupuk") pupuk: RequestBody? = null,
        @Part("pestisida") pestisida: RequestBody? = null,
        @Part("gangguanalam") gangguanAlam: RequestBody? = null,
        @Part("gangguanlainnya") gangguanLainnya: RequestBody? = null,
        @Part("keterangangangguanalam") keteranganGangguanAlam: RequestBody? = null,
        @Part("keterangangangguanlainnya") keteranganGangguanLainnya: RequestBody? = null,
        @Part foto1: MultipartBody.Part? = null,
        @Part foto2: MultipartBody.Part? = null,
        @Part foto3: MultipartBody.Part? = null,
        @Part foto4: MultipartBody.Part? = null,
    ): Response<ResponseBody>

    @GET("/android/perkembangan/tanaman/{tanaman_id}")
    suspend fun getDataPerkembangan(@Path("tanaman_id") tanaman_id : String): List<MDataPerkembanganItem>

    @GET("/android/perkembangan/{id}")
    suspend fun getDataPerkembaganById(@Path("id") id: String): MDataPerkembanganItem

    @GET("/android/panen/tanaman/{tanaman_id}")
    suspend fun getDataPanen(@Path("tanaman_id") tanaman_id : String): MRealisasiPanen

    @Multipart
    @POST("/android/panen/add")
    suspend fun uploadDataPanen(
        @Part("tanaman_id") tanaman_id: RequestBody,
        @Part("jumlahpanen") jumlahpanen: RequestBody,
        @Part("keterangan") keterangan: RequestBody,
        @Part foto1: MultipartBody.Part,
        @Part foto2: MultipartBody.Part,
        @Part foto3: MultipartBody.Part,
        @Part foto4: MultipartBody.Part,
    ): Response<ResponseBody>

    @Multipart
    @PUT("/android/panen/update")
    suspend fun uploadUpdateDataPanen(
        @Part("id") id: RequestBody,
        @Part("jumlahpanen") jumlahpanen: RequestBody? = null,
        @Part("keterangan") keterangan: RequestBody? = null,
        @Part foto1: MultipartBody.Part? = null,
        @Part foto2: MultipartBody.Part? = null,
        @Part foto3: MultipartBody.Part? = null,
        @Part foto4: MultipartBody.Part? = null,
    ): Response<ResponseBody>

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

    @DELETE("/android/mitra/delete/{kode}")
    suspend fun deleteOwner(@Path("kode") kode : String): Response<ResponseBody>

    @DELETE("/android/lahan/delete/{kode}")
    suspend fun deleteLahan(@Path("kode") kode : String): Response<ResponseBody>

    data class LocationRequest(
        @SerializedName("nrp") val nrp: String,
        @SerializedName("lat") val lat: String,
        @SerializedName("long") val long: String
    )

    @POST("/android/tracking")
    suspend fun sendLocation(
        @Body request: LocationRequest
    ): Response<ResponseBody>

    @POST("/android/atensi")
    suspend fun getAtensi(
        @Body request: getAtensiVal
    ): List<RAtensiItem>


}