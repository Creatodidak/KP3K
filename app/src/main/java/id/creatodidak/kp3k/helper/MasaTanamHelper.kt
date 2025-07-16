package id.creatodidak.kp3k.helper

import id.creatodidak.kp3k.api.newModel.MasaTanam
import id.creatodidak.kp3k.database.Entity.TanamanEntity

fun generateMasaTanamList(listTanaman: List<TanamanEntity>): List<MasaTanam> {
    return listTanaman
        .map { it.masatanam }
        .distinct()
        .sorted() // opsional: sort agar urut
        .mapIndexed { index, mt ->
            MasaTanam(
                id = index + 1,
                masatanam = mt,
                name = "MASA TANAM - ${index + 1}"
            )
        }
}