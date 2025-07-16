package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kabupaten")
data class KabupatenEntity(
    @PrimaryKey val id: Int,
    val nama: String,
    val provinsiId: Int
){
    override fun toString(): String = nama
}