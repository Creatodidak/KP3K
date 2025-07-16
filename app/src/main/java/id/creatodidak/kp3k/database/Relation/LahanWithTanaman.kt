package id.creatodidak.kp3k.database.Relation

import androidx.room.Embedded
import androidx.room.Relation
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity

data class LahanWithTanaman(
    @Embedded val lahan: LahanEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "lahan_id"
    )
    val realisasitanam: List<TanamanEntity>
)
