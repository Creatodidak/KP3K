package id.creatodidak.kp3k.helper

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.creatodidak.kp3k.database.AppDatabase
import kotlinx.coroutines.launch

fun AppCompatActivity.getAlamatBerdasarkanRole(
    db: AppDatabase,
    sh: SharedPreferences,
    onResult: (String) -> Unit
) {
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")
    lifecycleScope.launch {
        val wilayahDao = db.wilayahDao()
        val roleHelper = RoleHelper(this@getAlamatBerdasarkanRole)
        val role = getMyRole(this@getAlamatBerdasarkanRole)

        val alamat = when (role) {
            in PROV_TYPE -> {
                val provinsi = wilayahDao.getProvinsiById(roleHelper.id).nama
                "PROVINSI $provinsi"
            }

            in KAB_TYPE -> {
                val kabupaten = wilayahDao.getKabupatenById(roleHelper.id).nama
                "KABUPATEN $kabupaten"
            }

            in KEC_TYPE -> {
                val kecamatanList = wilayahDao.getDataKecamatanByPolsekId(sh.getInt("satker_id", 0))
                kecamatanList.joinToString(", ") { "KEC. ${it.nama}" }
            }

            in DESA_TYPE -> {
                val provinsi = "PROV. " + (sh.getString("provinsi_nama", "") ?: "")
                val kabupaten = "KAB. " + (sh.getString("kabupaten_nama", "") ?: "")
                val kecamatan = "KEC. " + (sh.getString("kecamatan_nama", "") ?: "")
                val desa = "DESA " + (sh.getString("desa_nama", "") ?: "")
                formatAlamat(desa, kecamatan, kabupaten, provinsi)
            }

            else -> ""
        }

        onResult(alamat)
    }
}


fun formatAlamat(vararg parts: String?): String {
    return parts.filterNot { it.isNullOrBlank() }.joinToString(", ")
}