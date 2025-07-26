package id.creatodidak.kp3k.api.RequestClass

import com.google.gson.annotations.SerializedName

data class PerkembanganIdsRequest(
    @SerializedName("ids") val ids: List<Int>
)

data class PerkembanganIdRequest(
    @SerializedName("id") val id: Int
)

data class PerkembanganAddRequest(
    // --- Identitas ---
    @SerializedName("tanaman_id")
    val tanamanId: Int,

    @SerializedName("submitter")
    val submitter: String,

    @SerializedName("status")
    val status: String,

    // --- Waktu ---
    @SerializedName("createAt")
    val createAt: String,

    @SerializedName("updateAt")
    val updateAt: String,

    // --- Data Tanaman & Lingkungan ---
    @SerializedName("tinggitanaman")
    val tinggitanaman: String,

    @SerializedName("kondisitanah")
    val kondisitanah: String,

    @SerializedName("warnadaun")
    val warnadaun: String,

    @SerializedName("curahhujan")
    val curahhujan: String,

    @SerializedName("ph")
    val ph: String,

    @SerializedName("kondisiair")
    val kondisiair: String,

    @SerializedName("pupuk")
    val pupuk: String,

    @SerializedName("pestisida")
    val pestisida: String,

    // --- Gangguan ---
    @SerializedName("hama")
    val hama: String,

    @SerializedName("keteranganhama")
    val keteranganhama: String,

    @SerializedName("gangguanalam")
    val gangguanalam: String,

    @SerializedName("keterangangangguanalam")
    val keterangangangguanalam: String,

    @SerializedName("gangguanlainnya")
    val gangguanlainnya: String,

    @SerializedName("keterangangangguanlainnya")
    val keterangangangguanlainnya: String,

    // --- Lain-lain / Keterangan ---
    @SerializedName("keterangan")
    val keterangan: String,

    @SerializedName("rekomendasi")
    val rekomendasi: String,

    @SerializedName("alasan")
    val alasan: String,

    // --- Foto Dokumentasi ---
    @SerializedName("foto1")
    val foto1: String,

    @SerializedName("foto2")
    val foto2: String,

    @SerializedName("foto3")
    val foto3: String,

    @SerializedName("foto4")
    val foto4: String
)
