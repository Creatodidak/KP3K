package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.TypeOwner
import java.util.Date

@Entity
data class OwnerEntity(
    @PrimaryKey val id: Int,
    val type: TypeOwner,
    val gapki: IsGapki = IsGapki.TIDAK,
    val nama_pok: String,
    val nama: String,
    val nik: String,
    val alamat: String,
    val telepon: String,
    val provinsi_id: Int,
    val kabupaten_id: Int,
    val kecamatan_id: Int,
    val desa_id: Int,
    val status: String = "VERIFIED",
    val alasan: String?,
    val createAt: Date = Date(),
    val updatedAt: Date = Date(),
    val komoditas: String,
    val submitter: String
){
    override fun toString(): String = nama
}
