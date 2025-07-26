package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class PerkembanganEntity(
    @PrimaryKey val id: Int,
    val tanaman_id: Int,
    val tinggitanaman: String?,
    val kondisitanah: String?,
    val warnadaun: String?,
    val curahhujan: String?,
    val hama: String?,
    val keteranganhama: String?,
    val ph: String?,
    val kondisiair: String?,
    val pupuk: String?,
    val pestisida: String?,
    val gangguanalam: String?,
    val keterangangangguanalam: String?,
    val gangguanlainnya: String?,
    val keterangangangguanlainnya: String?,
    val keterangan: String?,
    val rekomendasi: String?,
    val submitter: String?,
    val foto1: String = "/media/default.jpg",
    val foto2: String = "/media/default.jpg",
    val foto3: String = "/media/default.jpg",
    val foto4: String = "/media/default.jpg",
    val status: String = "VERIFIED",
    val alasan: String?,
    val createAt: Date = Date(),
    val updateAt: Date = Date()
)
