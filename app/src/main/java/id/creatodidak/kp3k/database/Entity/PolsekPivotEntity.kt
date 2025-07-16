package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "polsekpivot")
data class PolsekPivotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val satkerId: Int,
    val kecamatanId: Int
)