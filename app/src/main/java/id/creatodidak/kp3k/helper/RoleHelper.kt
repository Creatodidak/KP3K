package id.creatodidak.kp3k.helper

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.annotations.SerializedName
import id.creatodidak.kp3k.database.DatabaseInstance

data class RoleData(
    @SerializedName("id") val id: Int,
    @SerializedName("ids") val ids: List<Int>,
    @SerializedName("type") val type: String,
    @SerializedName("role") val role: String
)

data class SatkerData(
    @SerializedName("id") val id: Int,
    @SerializedName("nama") val nama: String,
    @SerializedName("level") val level: String,
)

suspend fun RoleHelper(ctx: Context): RoleData {
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")

    val sh = ctx.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
    val role = sh.getString("role", "").orEmpty()

    val provinsiId = sh.getInt("satker_provinsiId", 0)
    val kabupatenId = sh.getInt("satker_kabupatenId", 0)
    val desaId = sh.getInt("desa_id", 0)
    val satkerId = sh.getInt("satkerId", 0)

    val wilayahDao = DatabaseInstance.getDatabase(ctx).wilayahDao()
    val kecamatanIds = if (role in KEC_TYPE) {
        wilayahDao.getKecamatanIdByPolsekId(satkerId).map { it.kecamatanId }.distinct()
    } else {
        emptyList()
    }

    return when (role) {
        in PROV_TYPE -> RoleData(id = provinsiId, ids = listOf(0), type = "provinsi", role = role)
        in KAB_TYPE -> RoleData(id = kabupatenId, ids = listOf(0), type = "kabupaten", role = role)
        in KEC_TYPE -> RoleData(id = 0, ids = kecamatanIds, type = "kecamatan", role = role)
        in DESA_TYPE -> RoleData(id = desaId, ids = listOf(0), type = "desa", role = role)
        else -> RoleData(id = 0, ids = listOf(0), type = "indonesia", role = role)
    }
}

fun getMyLevel(ctx: Context) : String{
    val PROV_TYPE = setOf("SUPERADMIN", "ADMINPOLDA", "PJUPOLDA", "PERSONILPOLDA")
    val KAB_TYPE = setOf("PAMATWIL", "PJUPOLRES", "ADMINPOLRES", "PERSONILPOLRES")
    val KEC_TYPE = setOf("KAPOLSEK", "ADMINPOLSEK", "PERSONILPOLSEK")
    val DESA_TYPE = setOf("BPKP")
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    val role = sh.getString("role", "").orEmpty()
    return when (role) {
        in PROV_TYPE -> "provinsi"
        in KAB_TYPE -> "kabupaten"
        in KEC_TYPE -> "kecamatan"
        in DESA_TYPE -> "desa"
        else -> "indonesia"
    }
}

fun getMyRole(ctx: Context) : String{
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    return sh.getString("role", "").orEmpty()
}
fun getMyNrp(ctx: Context) : String{
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    return sh.getString("nrp", "").orEmpty()
}

fun getMyNamePangkat(ctx: Context) : String{
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    return sh.getString("pangkat", "").orEmpty()+" "+sh.getString("nama", "").orEmpty()
}

fun isPejabat(ctx: Context): Boolean =
    ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
        .getString("role", "")
        .orEmpty() in setOf("PJUPOLDA", "PAMATWIL", "PJUPOLRES", "KAPOLSEK")

fun isCanCRUD(ctx: Context): Boolean =
    ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
        .getString("role", "")
        .orEmpty() in setOf("ADMINPOLRES","ADMINPOLSEK", "BPKP")

fun getMyKabId(ctx: Context) : String{
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    return sh.getInt("satker_kabupatenId", 0).toString()
}

fun getMySatkerId(ctx: Context) : Int{
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    return sh.getInt("satker_id", 0)
}

fun getMyWilayah(ctx: Context) : Int{
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    return sh.getInt("wilayah", 0)
}

fun getMyUsername(ctx: Context) : String{
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    return sh.getString("username", "").orEmpty()
}

fun getMySatker(ctx: Context) : SatkerData{
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    val id = sh.getInt("satker_id", 0)
    val level = sh.getString("satker_level", "")
    val nama = sh.getString("satker_nama", "")
    return SatkerData(id, nama!!, level!!)
}
fun isCanVideoCall(ctx: Context): Boolean {
    val sh = ctx.getSharedPreferences("USER_DATA", MODE_PRIVATE)
    val role = sh.getString("role", "")
    return when(role){
        in setOf("PJUPOLDA", "PAMATWIL", "SUPERADMIN") -> true
        "PJUPOLRES" -> {
            !getMyUsername(ctx).contains("KABAG")
        }
        else -> {
            getMyNrp(ctx) == "98070129"
        }
    }
}