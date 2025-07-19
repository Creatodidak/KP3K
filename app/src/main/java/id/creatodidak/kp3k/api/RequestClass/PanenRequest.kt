package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class PanenIdsRequest(
    @SerializedName("ids") val ids: List<Int>
)

data class PanenIdRequest(
    @SerializedName("id") val id: Int
)

data class PanenByTanggalPanenRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("tanggalpanen") val tanggalPanen: String
)

data class PanenByTanggalPanenRangeRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("tanggalpanenstart") val tanggalPanenStart: String,
    @SerializedName("tanggalpanenend") val tanggalPanenEnd: String
)

data class InsertDataPanen(
    @SerializedName("tanaman_id") val tanaman_id : String,
    @SerializedName("jumlahpanen") val jumlahpanen : String,
    @SerializedName("luaspanen") val luaspanen : String,
    @SerializedName("tanggalpanen") val tanggalpanen : String,
    @SerializedName("keterangan") val keterangan : String,
    @SerializedName("analisa") val analisa : String?,
    @SerializedName("foto1") val foto1 : String,
    @SerializedName("foto2") val foto2 : String,
    @SerializedName("foto3") val foto3 : String,
    @SerializedName("foto4") val foto4 : String,
    @SerializedName("status") val status : String,
    @SerializedName("alasan") val alasan : String?,
    @SerializedName("komoditas") val komoditas : String,
    @SerializedName("submitter") val submitter: String,
    @SerializedName("kabupatenId") val kabupatenId: String,
    @SerializedName("role") val role: String
,)

data class UpdateDataPanen(
    @SerializedName("tanaman_id") val tanaman_id : String?,
    @SerializedName("jumlahpanen") val jumlahpanen : String?,
    @SerializedName("luaspanen") val luaspanen : String?,
    @SerializedName("tanggalpanen") val tanggalpanen : String,
    @SerializedName("keterangan") val keterangan : String?,
    @SerializedName("analisa") val analisa : String?,
    @SerializedName("foto1") val foto1 : String?,
    @SerializedName("foto2") val foto2 : String?,
    @SerializedName("foto3") val foto3 : String?,
    @SerializedName("foto4") val foto4 : String?,
    @SerializedName("status") val status : String?,
    @SerializedName("alasan") val alasan : String?,
    @SerializedName("komoditas") val komoditas : String?,
    @SerializedName("createAt") val createAt : String?,
    @SerializedName("submitter") val submitter: String?,
    @SerializedName("kabupatenId") val kabupatenId: String,
    @SerializedName("role") val role: String
)