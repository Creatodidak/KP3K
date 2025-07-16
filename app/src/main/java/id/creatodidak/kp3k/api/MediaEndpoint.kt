package id.creatodidak.kp3k.api

import id.creatodidak.kp3k.api.RequestClass.DeleteMediaRequest
import id.creatodidak.kp3k.api.newModel.DefaultResponse
import id.creatodidak.kp3k.api.newModel.MediaResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MediaEndpoint {
    @Multipart
    @POST("/media/upload")
    suspend fun uploadMedia(
        @Part files: List<MultipartBody.Part>,
        @Part("nrp") nrp: RequestBody
    ): Response<List<MediaResponse>>

    @POST("/media/delete-multiple")
    suspend fun deleteMultipleMedia(
        @Body request: DeleteMediaRequest
    ): Response<DefaultResponse>


}