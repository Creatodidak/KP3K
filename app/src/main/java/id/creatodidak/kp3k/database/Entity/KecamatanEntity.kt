package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "kecamatan")
data class KecamatanEntity(
    @PrimaryKey val id: Int,
    val nama: String,
    val kabupatenId: Int
){
    override fun toString(): String = nama
}