package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.creatodidak.kp3k.api.newModel.ByEntity.Roles
import id.creatodidak.kp3k.helper.SumberBibit
import java.util.Date

@Entity
data class PersonilEntity(
    @PrimaryKey val id: Int,
    val nrp: String,
    val nohp: String,
    val jabatan: String,
    val satkerId: Int,
    val foto: String = "/media/uploads/default.png",
    val password: String,
    val passwordiv: String,
    val role: Roles,
    val status: String,
    val nama: String,
    val pangkat: String,
    val desaBinaanId: Int? = null
)
