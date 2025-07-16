package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "desa")
data class DesaEntity(
    @PrimaryKey val id: Int,
    val nama: String,
    val kecamatanId: Int
){
    override fun toString(): String = nama
}