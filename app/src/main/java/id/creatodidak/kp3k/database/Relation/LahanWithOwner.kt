package id.creatodidak.kp3k.database.Relation

import androidx.room.Embedded
import androidx.room.Relation
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity

data class LahanWithOwner(
    @Embedded val lahan: LahanEntity,

    @Relation(
        parentColumn = "owner_id",
        entityColumn = "id"
    )
    val owner: OwnerEntity
)
