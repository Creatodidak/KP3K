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
import id.creatodidak.kp3k.api.model.SocketDataPeta
import id.creatodidak.kp3k.api.model.newLahan
import id.creatodidak.kp3k.api.model.pimpinan.RBasicPimpinan
import id.creatodidak.kp3k.api.model.pimpinan.RDetailLahan
import id.creatodidak.kp3k.api.model.pimpinan.RDetailPersonil
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

    @GET("/p/basic")
    suspend fun getBasic(): RBasicPimpinan

    @GET("/p/lahandetails/{kode}")
    suspend fun getDetailLahan(@Path ("kode") kode: String): RDetailLahan

    @GET("/p/persdetails/{nrp}")
    suspend fun getDetailPers(@Path ("nrp") nrp: String): RDetailPersonil

}