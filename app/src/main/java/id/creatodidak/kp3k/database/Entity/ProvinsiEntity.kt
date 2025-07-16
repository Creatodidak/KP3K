package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provinsi")
data class ProvinsiEntity(
    @PrimaryKey val id: Int,
    val nama: String
){
    override fun toString(): String = nama
}