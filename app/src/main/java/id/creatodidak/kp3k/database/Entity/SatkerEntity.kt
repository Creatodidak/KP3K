package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "satker")
data class SatkerEntity(
    @PrimaryKey val id: Int,
    val kode: String,
    val nama: String,
    val level: String,
    val parentId: Int?,
    val provinsiId: Int?,
    val kabupatenId: Int?
){
    override fun toString(): String = nama
}