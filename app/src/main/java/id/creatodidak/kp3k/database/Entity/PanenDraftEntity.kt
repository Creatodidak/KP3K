package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class PanenDraftEntity(
    @PrimaryKey val id: Int,
    val currentId: Int?,
    val tanaman_id: Int,
    val jumlahpanen: String,
    val luaspanen: String,
    val tanggalpanen: Date = Date(),
    val keterangan: String,
    val analisa: String?,
    val foto1: String = "/media/default.jpg",
    val foto2: String = "/media/default.jpg",
    val foto3: String = "/media/default.jpg",
    val foto4: String = "/media/default.jpg",
    val status: String = "VERIFIED",
    val alasan: String?,
    val createAt: Date = Date(),
    val updateAt: Date = Date(),
    val komoditas: String,
    val submitter: String
)
