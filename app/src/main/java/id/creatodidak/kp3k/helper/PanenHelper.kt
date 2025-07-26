package id.creatodidak.kp3k.helper

import id.creatodidak.kp3k.database.AppDatabase
import id.creatodidak.kp3k.database.Entity.PanenEntity
import id.creatodidak.kp3k.newversion.DataPanen.ShowDataPanenByCategory

data class PanenStat(
    val jumlah: String,
    val luas: String,
    val target: String,
    val capaian: String
)

suspend fun mapToNewPanenList(list: List<PanenEntity>?, db: AppDatabase): List<ShowDataPanenByCategory.NewPanenEntity> {
    return list?.map {
        val tanaman = db.tanamanDao().getTanamanById(it.tanaman_id)
        val lahan = db.lahanDao().getLahanById(tanaman.lahan_id)
        ShowDataPanenByCategory.NewPanenEntity(
            it.id, "", tanaman, lahan, null, it.tanaman_id, it.jumlahpanen, it.luaspanen,
            it.tanggalpanen, it.keterangan, it.analisa, it.foto1, it.foto2, it.foto3, it.foto4,
            it.status, it.alasan, it.createAt, it.updateAt, it.komoditas, it.submitter
        )
    } ?: emptyList()
}

fun calculateStat(list: List<ShowDataPanenByCategory.NewPanenEntity>): PanenStat {
    val jumlah = list.sumOf { it.jumlahpanen.toDoubleOrNull() ?: 0.0 }
    val luas = list.sumOf { it.luaspanen.toDoubleOrNull() ?: 0.0 }
    val target = list.sumOf { it.tanaman?.prediksipanen?.toDoubleOrNull() ?: 0.0 }
    val capaian = if (target != 0.0) (jumlah / target) * 100 else 0.0

    return PanenStat(
        angkaIndonesia(convertToTon(jumlah)),
        angkaIndonesia(convertToHektar(luas)),
        angkaIndonesia(convertToTon(target)),
        angkaIndonesia(capaian)
    )
}
