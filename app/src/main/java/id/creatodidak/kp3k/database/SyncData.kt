package id.creatodidak.kp3k.database

import android.content.Context
import android.util.Log
import androidx.room.TypeConverter
import id.creatodidak.kp3k.api.Client
import id.creatodidak.kp3k.api.LahanEndpoint
import id.creatodidak.kp3k.api.OwnerEndpoint
import id.creatodidak.kp3k.api.PanenEndpoint
import id.creatodidak.kp3k.api.RequestClass.PanenIdsRequest
import id.creatodidak.kp3k.api.RequestClass.TanamanIdsRequest
import id.creatodidak.kp3k.api.TanamanEndpoint
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.PanenEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.helper.IsGapki
import id.creatodidak.kp3k.helper.Loading
import id.creatodidak.kp3k.helper.RoleHelper
import id.creatodidak.kp3k.helper.SumberBibit
import id.creatodidak.kp3k.helper.TypeLahan
import id.creatodidak.kp3k.helper.TypeOwner
import id.creatodidak.kp3k.helper.parseIsoDate
import java.util.Date

suspend fun syncDataFromServer(ctx: Context){
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")
    var type = ""
    var id = listOf<Int>()
    try {
        val roles = RoleHelper(ctx)
        when (roles.role) {
            in PROV_TYPE -> {
                type = "provinsi"
                id = listOf(roles.id)
            }
            in KAB_TYPE -> {
                type = "kabupaten"
                id = listOf(roles.id)
            }
            in KEC_TYPE -> {
                type = "kecamatan"
                id = roles.ids
                Log.i("DATA KECAMATAN REQUEST", roles.ids.toString())
            }
            in DESA_TYPE -> {
                type = "desa"
                id = listOf(roles.id)
            }
        }

        val db = DatabaseInstance.getDatabase(ctx)
        val ownerDao = db.ownerDao()
        val lahanDao = db.lahanDao()
        val tanamanDao = db.tanamanDao()
        val panenDao = db.panenDao()


        val owner = Client.retrofit.create(OwnerEndpoint::class.java).getAllOwner(type,
            OwnerEndpoint.RequestIds(id)
        )
        if(owner.isSuccessful && owner.body() != null && owner.body()?.isNotEmpty() == true){
            ownerDao.deleteOnlineData()
            val data = owner.body()?.map { item ->
                item.let {
                    OwnerEntity(
                        id = it.id!!,
                        komoditas = it.komoditas!!,
                        type = TypeOwner.valueOf(it.type!!),
                        gapki = IsGapki.valueOf(it.gapki!!),
                        nama_pok = it.namaPok!!,
                        nama = it.nama!!,
                        nik = it.nik!!,
                        alamat = it.alamat!!,
                        telepon = it.telepon!!,
                        provinsi_id = it.provinsiId!!,
                        kabupaten_id = it.kabupatenId!!,
                        kecamatan_id = it.kecamatanId!!,
                        desa_id = it.desaId!!,
                        status = it.status!!,
                        alasan = it.alasan,
                        createAt = parseIsoDate(it.createAt!!) ?: Date(),
                        updatedAt = parseIsoDate(it.updatedAt!!) ?: Date(),
                        submitter = it.submitter!!,
                    )
                }
            }

            data?.let { ownerDao.insertAll(it) }
        }

        if(ownerDao.getAll().isNotEmpty()){
            val lahan = Client.retrofit.create(LahanEndpoint::class.java).getAllLahan(type, OwnerEndpoint.RequestIds(id))
            if(lahan.isSuccessful && lahan.body() != null && lahan.body()?.isNotEmpty() == true){
                db.lahanDao().deleteOnlineData()
                val data = lahan.body()?.map { item ->
                    item.let {
                        LahanEntity(
                            id = it.id!!,
                            komoditas = it.komoditas!!,
                            type = TypeLahan.valueOf(it.type!!),
                            owner_id = it.ownerId!!,
                            provinsi_id = it.provinsiId!!,
                            kabupaten_id = it.kabupatenId!!,
                            kecamatan_id = it.kecamatanId!!,
                            desa_id = it.desaId!!,
                            luas = it.luas!!,
                            latitude = it.latitude!!,
                            longitude = it.longitude!!,
                            status = it.status!!,
                            alasan = it.alasan,
                            createAt = parseIsoDate(it.createAt!!) ?: Date(),
                            updateAt = parseIsoDate(it.updateAt!!) ?: Date(),
                            submitter = it.submitter!!,
                            lahanke = it.lahanke!!
                        )
                    }
                }

                data?.let{lahanDao.insertAll(it)}
            }
        }

        val lahanIdList = lahanDao.getAllId()
        if(lahanIdList.isNotEmpty()){
            val tanaman = Client.retrofit.create(TanamanEndpoint::class.java).getAllTanamanOnLahans(TanamanIdsRequest(lahanIdList))
            if(tanaman.isSuccessful && tanaman.body() != null && tanaman.body()?.isNotEmpty() == true){
                db.tanamanDao().deleteOnlineData()
                val data = tanaman.body()?.map { item ->
                    item.let {
                        TanamanEntity(
                            id = it.id!!,
                            lahan_id = it.lahanId!!,
                            masatanam = it.masatanam!!,
                            luastanam = it.luastanam!!,
                            tanggaltanam = parseIsoDate(it.tanggaltanam!!)!!,
                            prediksipanen = it.prediksipanen!!,
                            rencanatanggalpanen = parseIsoDate(it.rencanatanggalpanen!!)!!,
                            komoditas = it.komoditas!!,
                            varietas = it.varietas!!,
                            sumber = SumberBibit.valueOf(it.sumber!!),
                            keteranganSumber = it.keteranganSumber!!,
                            foto1 = it.foto1!!,
                            foto2 = it.foto2!!,
                            foto3 = it.foto3!!,
                            foto4 = it.foto4!!,
                            status = it.status!!,
                            alasan = it.alasan,
                            createAt = parseIsoDate(it.createAt!!)!!,
                            updateAt = parseIsoDate(it.updateAt!!)!!,
                            submitter = it.submitter!!,
                            tanamanke = it.tanamanke!!
                        )
                    }
                }

                data?.let{tanamanDao.insertAll(it)}
            }
        }

        val tanamanIdList = tanamanDao.getAllId()
        if(tanamanIdList.isNotEmpty()){
            db.panenDao().deleteOnlineData()
            val panen = Client.retrofit.create(PanenEndpoint::class.java).getAllPanenOnLahans(PanenIdsRequest(tanamanIdList))
            if(panen.isSuccessful && panen.body() != null && panen.body()?.isNotEmpty() == true){
                val data = panen.body()?.map { item ->
                    item.let {
                        PanenEntity(
                            id = it.id!!,
                            tanaman_id = it.tanamanId!!,
                            jumlahpanen = it.jumlahpanen!!,
                            luaspanen = it.luaspanen!!,
                            tanggalpanen = parseIsoDate(it.tanggalpanen!!) ?: Date(),
                            keterangan = it.keterangan!!,
                            komoditas = it.komoditas!!,
                            analisa = it.analisa,
                            foto1 = it.foto1!!,
                            foto2 = it.foto2!!,
                            foto3 = it.foto3!!,
                            foto4 = it.foto4!!,
                            status = it.status!!,
                            alasan = it.alasan,
                            createAt = parseIsoDate(it.createAt!!) ?: Date(),
                            updateAt = parseIsoDate(it.updateAt!!) ?: Date(),
                            submitter = it.submitter!!
                        )
                    }
                }

                data?.let{panenDao.insertAll(it)}
            }
        }

    }catch(e: Exception){
        e.printStackTrace()
    }
}
