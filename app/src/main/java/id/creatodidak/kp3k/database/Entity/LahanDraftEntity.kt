package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.creatodidak.kp3k.helper.TypeLahan
import java.util.Date

@Entity
data class LahanDraftEntity(
    @PrimaryKey val id: Int,
    val currentId: Int?,
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
    val status: String = "OFFLINECREATE",
    val alasan: String?,
    val createAt: Date = Date(),
    val updateAt: Date = Date(),
    val submitter: String,
    val lahanke: String
)
