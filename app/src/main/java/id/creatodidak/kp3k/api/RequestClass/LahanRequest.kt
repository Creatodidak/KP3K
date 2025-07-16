package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class LahanAddRequest(
    @SerializedName("type")
    val type: String,

    @SerializedName("komoditas")
    val komoditas: String,

    @SerializedName("owner_id")
    val ownerId: Int,

    @SerializedName("provinsi_id")
    val provinsiId: Int,

    @SerializedName("kabupaten_id")
    val kabupatenId: Int,

    @SerializedName("kecamatan_id")
    val kecamatanId: Int,

    @SerializedName("desa_id")
    val desaId: Int,

    @SerializedName("luas")
    val luas: String,

    @SerializedName("latitude")
    val latitude: String,

    @SerializedName("longitude")
    val longitude: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("alasan")
    val alasan: String? = null,

    @SerializedName("submitter")
    val submitter: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("lahanke")
    val lahanke: String
)

data class LahanPatchRequest(
    @SerializedName("type")
    val type: String?,

    @SerializedName("komoditas")
    val komoditas: String?,

    @SerializedName("owner_id")
    val ownerId: Int?,

    @SerializedName("provinsi_id")
    val provinsiId: Int?,

    @SerializedName("kabupaten_id")
    val kabupatenId: Int?,

    @SerializedName("kecamatan_id")
    val kecamatanId: Int?,

    @SerializedName("desa_id")
    val desaId: Int?,

    @SerializedName("luas")
    val luas: String?,

    @SerializedName("latitude")
    val latitude: String?,

    @SerializedName("longitude")
    val longitude: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("alasan")
    val alasan: String? = null,

    @SerializedName("submitter")
    val submitter: String?,

    @SerializedName("role")
    val role: String?,

    @SerializedName("lahanke")
    val lahanke: String?
)