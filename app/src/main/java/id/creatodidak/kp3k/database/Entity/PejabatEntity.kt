package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.creatodidak.kp3k.api.newModel.ByEntity.Roles

@Entity
data class PejabatEntity(
    @PrimaryKey val id: Int,
    val username: String,
    val nrp: String,
    val jabatan: String,
    val satkerId: Int,
    val password: String,
    val passwordiv: String,
    val role: Roles,
    val status: String,
    val wilayah: Int? = null
)
