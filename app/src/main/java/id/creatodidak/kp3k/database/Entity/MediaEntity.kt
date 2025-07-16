package id.creatodidak.kp3k.database.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.creatodidak.kp3k.helper.MediaType
import java.util.Date

@Entity
data class MediaEntity(
    @PrimaryKey val id: Int,
    val nrp: String,
    val filename: String,
    val url: String,
    val type: MediaType,
    val createdAt: Date = Date()
)
