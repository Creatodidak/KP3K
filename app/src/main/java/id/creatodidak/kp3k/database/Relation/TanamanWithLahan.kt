package id.creatodidak.kp3k.database.Relation

import androidx.room.Embedded
import androidx.room.Relation
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity

data class TanamanWithLahan(
    @Embedded val tanaman: TanamanEntity,

    @Relation(
        parentColumn = "lahan_id",
        entityColumn = "id"
    )
    val lahan: LahanEntity
)
