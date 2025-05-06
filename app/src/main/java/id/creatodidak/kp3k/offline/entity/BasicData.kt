package id.creatodidak.kp3k.offline.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "basic_data")
data class BasicDataEntity(
    @PrimaryKey val id: Int = 1, // karena hanya 1 data, id tetap
    val json: String
)
