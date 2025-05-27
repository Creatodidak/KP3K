package id.creatodidak.kp3k.api.model.pimpinan

import com.google.gson.annotations.SerializedName

data class DataKontak(
    @field:SerializedName("nama")
    val nama: String? = null,

    @field:SerializedName("jabatan")
    val jabatan: String? = null,

    @field:SerializedName("target")
    val target: String? = null
)

data class ValDataKontak(
    @SerializedName("jenis") val jenis: String,
    @SerializedName("kab") val kab: String? = null,
    @SerializedName("kec") val kec: String? = null,
    @SerializedName("desa") val desa: String? = null
)

data class listWilayah(
    @field:SerializedName("nama")
    val nama: String? = null,
)

data class ValDataWilayah(
    @SerializedName("kab") val kab: String,
    @SerializedName("kec") val kec: String? = null,
)