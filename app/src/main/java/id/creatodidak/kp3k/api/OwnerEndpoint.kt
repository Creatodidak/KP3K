package id.creatodidak.kp3k.api

import com.google.gson.annotations.SerializedName
import id.creatodidak.kp3k.api.RequestClass.OwnerAddRequest
import id.creatodidak.kp3k.api.RequestClass.OwnerPatchRequest
import id.creatodidak.kp3k.api.RequestClass.VerifikasiRequest
import id.creatodidak.kp3k.api.newModel.DefaultResponse
import id.creatodidak.kp3k.api.newModel.OwnerResponseItem
import id.creatodidak.kp3k.api.newModel.OwnerCreateUpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OwnerEndpoint {
    data class RequestIds(
        @SerializedName("id")
        val id : List<Int>
    )

    @POST("/owner/wilayah/{type}")
    suspend fun getAllOwner(
        @Path("type") type: String,
        @Body RequestIds: RequestIds
    ): Response<List<OwnerResponseItem>>

    @GET("/owner/detail/{id}")
    suspend fun getOwnerDetail(
        @Path("id") id: String
    ): Response<OwnerResponseItem>

    @POST("/owner/add")
    suspend fun addOwner(
        @Body OwnerAddRequest: OwnerAddRequest
    ): Response<OwnerCreateUpdateResponse>

    @PATCH("/owner/update/{id}")
    suspend fun updateOwner(
        @Path("id") id: String,
        @Body OwnerPatchRequest: OwnerPatchRequest
    ): Response<OwnerCreateUpdateResponse>

    @PATCH("/owner/verifikasi/{id}")
    suspend fun verifikasiOwner(
        @Path("id") id: String,
        @Body VerifikasiRequest: VerifikasiRequest
    ): Response<OwnerCreateUpdateResponse>

    @DELETE("/owner/delete/{id}")
    suspend fun deleteOwner(
        @Path("id") id: String
    ): Response<DefaultResponse>
}