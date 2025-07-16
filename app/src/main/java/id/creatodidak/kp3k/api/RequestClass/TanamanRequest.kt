package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class TanamanIdsRequest(
    @SerializedName("ids") val ids: List<Int>
)

data class TanamanIdRequest(
    @SerializedName("id") val id: Int
)

data class TanamanByMasaTanamRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("masatanam") val masaTanam: String
)

data class TanamanByTanggalTanamRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("tanggaltanam") val tanggalTanam: String
)

data class TanamanByTanggalTanamRangeRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("tanggaltanamstart") val tanggalTanamStart: String,
    @SerializedName("tanggaltanamend") val tanggalTanamEnd: String
)

data class InsertDataTanam(
    @SerializedName("lahan_id") val lahan_id : String,
    @SerializedName("masatanam") val masatanam : String,
    @SerializedName("luastanam") val luastanam : String,
    @SerializedName("tanggaltanam") val tanggaltanam : String,
    @SerializedName("prediksipanen") val prediksipanen : String,
    @SerializedName("rencanatanggalpanen") val rencanatanggalpanen : String,
    @SerializedName("komoditas") val komoditas : String,
    @SerializedName("varietas") val varietas : String,
    @SerializedName("sumber") val sumber : String,
    @SerializedName("keteranganSumber") val keteranganSumber : String,
    @SerializedName("foto1") val foto1 : String,
    @SerializedName("foto2") val foto2 : String,
    @SerializedName("foto3") val foto3 : String,
    @SerializedName("foto4") val foto4 : String,
    @SerializedName("status") val status : String,
    @SerializedName("alasan") val alasan : String?,
    @SerializedName("submitter") val submitter: String,
    @SerializedName("role") val role: String,
    @SerializedName("kabupatenId") val kabupatenId: String,
    @SerializedName("tanamanke") val tanamanke: String

)

data class UpdateDataTanam(
    @SerializedName("lahan_id") val lahan_id : String?,
    @SerializedName("masatanam") val masatanam : String?,
    @SerializedName("luastanam") val luastanam : String?,
    @SerializedName("tanggaltanam") val tanggaltanam : String?,
    @SerializedName("prediksipanen") val prediksipanen : String?,
    @SerializedName("rencanatanggalpanen") val rencanatanggalpanen : String?,
    @SerializedName("komoditas") val komoditas : String?,
    @SerializedName("varietas") val varietas : String?,
    @SerializedName("sumber") val sumber : String?,
    @SerializedName("keteranganSumber") val keteranganSumber : String?,
    @SerializedName("foto1") val foto1 : String?,
    @SerializedName("foto2") val foto2 : String?,
    @SerializedName("foto3") val foto3 : String?,
    @SerializedName("foto4") val foto4 : String?,
    @SerializedName("status") val status : String?,
    @SerializedName("alasan") val alasan : String?,
    @SerializedName("submitter") val submitter: String?,
    @SerializedName("role") val role: String,
    @SerializedName("kabupatenId") val kabupatenId: String,
    @SerializedName("tanamanke") val tanamanke: String
)