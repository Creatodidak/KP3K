package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.creatodidak.kp3k.helper.SumberBibit
import java.util.Date

@Entity
data class TanamanDraftEntity(
    @PrimaryKey val id: Int,
    val currentId: Int?,
    val lahan_id: Int,
    val masatanam: String,
    val luastanam: String,
    val tanggaltanam: Date = Date(),
    val prediksipanen: String,
    val rencanatanggalpanen: Date = Date(),
    val komoditas: String,
    val varietas: String,
    val sumber: SumberBibit,
    val keteranganSumber: String,
    val foto1: String = "/media/default.jpg",
    val foto2: String = "/media/default.jpg",
    val foto3: String = "/media/default.jpg",
    val foto4: String = "/media/default.jpg",
    val status: String = "VERIFIED",
    val alasan: String?,
    val createAt: Date = Date(),
    val updateAt: Date = Date(),
    val submitter: String,
    val tanamanke: String
)
