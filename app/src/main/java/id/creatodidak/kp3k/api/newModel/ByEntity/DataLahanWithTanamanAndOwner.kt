package id.creatodidak.kp3k.api.newModel.ByEntity

import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.TypeLahan
import java.util.Date


data class DataLahanWithTanamanAndOwner (
    val id: Int,
    val type: TypeLahan,
    val komoditas: String,
    val owner_id: Int,
    val provinsi_id: Int,
    val kabupaten_id: Int,
    val kecamatan_id: Int,
    val desa_id: Int,
    val luas: String,
    val latitude: String,
    val longitude: String,
    val status: String = "VERIFIED",
    val alasan: String?,
    val createAt: Date = Date(),
    val updateAt: Date = Date(),
    val submitter: String,
    val owner: OwnerEntity?,
    val realisasitanam: List<TanamanEntity>?
)