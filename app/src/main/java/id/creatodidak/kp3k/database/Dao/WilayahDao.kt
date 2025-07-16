package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.creatodidak.kp3k.database.Entity.DesaEntity
import id.creatodidak.kp3k.database.Entity.KabupatenEntity
import id.creatodidak.kp3k.database.Entity.KecamatanEntity
import id.creatodidak.kp3k.database.Entity.PolsekPivotEntity
import id.creatodidak.kp3k.database.Entity.ProvinsiEntity
import id.creatodidak.kp3k.database.Entity.SatkerEntity

@Dao
interface WilayahDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvinsi(list: List<ProvinsiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKabupaten(list: List<KabupatenEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKecamatan(list: List<KecamatanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesa(list: List<DesaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSatker(list: List<SatkerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolsekPivot(list: List<PolsekPivotEntity>)

    @Query("SELECT * FROM satker")
    suspend fun getSatker(): List<SatkerEntity>

    @Query("SELECT * FROM polsekpivot")
    suspend fun getPolsekPivot(): List<PolsekPivotEntity>

    @Query("SELECT * FROM provinsi")
    suspend fun getProvinsi(): List<ProvinsiEntity>

    @Query("SELECT * FROM kabupaten")
    suspend fun getKabupaten(): List<KabupatenEntity>

    @Query("SELECT * FROM kecamatan")
    suspend fun getKecamatan(): List<KecamatanEntity>

    @Query("SELECT * FROM desa")
    suspend fun getDesa(): List<DesaEntity>

    @Query("SELECT * FROM kabupaten WHERE provinsiId = :provinsiId")
    suspend fun getKabupatenByProvinsi(provinsiId: Int): List<KabupatenEntity>

    @Query("SELECT * FROM kecamatan WHERE kabupatenId = :kabupatenId")
    suspend fun getKecamatanByKabupaten(kabupatenId: Int): List<KecamatanEntity>

    @Query("SELECT * FROM desa WHERE kecamatanId = :kecamatanId")
    suspend fun getDesaByKecamatan(kecamatanId: Int): List<DesaEntity>

    @Query("SELECT * FROM kecamatan WHERE kabupatenId IN (SELECT id FROM kabupaten WHERE provinsiId = :provinsi)")
    suspend fun getKecamatanByProvinsi(provinsi: Int): List<KecamatanEntity>

    @Query("SELECT * FROM desa WHERE kecamatanId IN (SELECT id FROM kecamatan WHERE kabupatenId IN (SELECT id FROM kabupaten WHERE provinsiId = :provinsi))")
    suspend fun getDesaByProvinsi(provinsi: Int): List<DesaEntity>

    @Query("SELECT * FROM desa WHERE kecamatanId IN (SELECT id FROM kecamatan WHERE kabupatenId = :kabupaten)")
    suspend fun getDesaByKabupaten(kabupaten: Int): List<DesaEntity>

    @Query("SELECT * FROM provinsi WHERE id = :id")
    suspend fun getProvinsiById(id: Int): ProvinsiEntity

    @Query("SELECT * FROM kabupaten WHERE id = :id")
    suspend fun getKabupatenById(id: Int): KabupatenEntity

    @Query("SELECT * FROM kecamatan WHERE id = :id")
    suspend fun getKecamatanById(id: Int): KecamatanEntity

    @Query("SELECT * FROM kecamatan WHERE id IN (:ids)")
    suspend fun getKecamatanByIds(ids: List<Int>): List<KecamatanEntity>

    @Query("SELECT * FROM desa WHERE id = :id")
    suspend fun getDesaById(id: Int): DesaEntity

    @Query("SELECT * FROM satker WHERE id = :id")
    suspend fun getSatkerById(id: Int): SatkerEntity

    @Query("SELECT * FROM satker WHERE id = :id")
    suspend fun getSatkerOnListById(id: Int): List<SatkerEntity>

    @Query("SELECT * FROM satker WHERE parentId = :id")
    suspend fun getSatkerByParentId(id: Int): List<SatkerEntity>

    @Query("SELECT * FROM satker WHERE provinsiId = :id")
    suspend fun getSatkerByProvId(id: Int): List<SatkerEntity>

    @Query("SELECT * FROM satker WHERE kabupatenId = :id")
    suspend fun getSatkerByKabId(id: Int): List<SatkerEntity>

    @Query("SELECT * FROM polsekpivot WHERE satkerId = :satkerId")
    suspend fun getKecamatanIdByPolsekId(satkerId: Int): List<PolsekPivotEntity>

    @Query("SELECT * FROM polsekpivot WHERE satkerId = :satkerId")
    fun getKecamatanIdByPolsekIdHelper(satkerId: Int): List<PolsekPivotEntity>

    @Query("SELECT * FROM kecamatan WHERE id IN (SELECT kecamatanId FROM polsekpivot WHERE satkerId = :satkerId)")
    suspend fun getDataKecamatanByPolsekId(satkerId: Int): List<KecamatanEntity>

    @Query("SELECT * FROM provinsi WHERE id = :id")
    fun getProvinsiByIdAdapter(id: Int): ProvinsiEntity

    @Query("SELECT * FROM kabupaten WHERE id = :id")
    fun getKabupatenByIdAdapter(id: Int): KabupatenEntity

    @Query("SELECT * FROM kecamatan WHERE id = :id")
    fun getKecamatanByIdAdapter(id: Int): KecamatanEntity

    @Query("SELECT * FROM desa WHERE id = :id")
    fun getDesaByIdAdapter(id: Int): DesaEntity

    @Query("SELECT * FROM satker WHERE level = 'POLRES' AND parentId = :id ")
    fun getPolresOnMyPolda(id: Int): List<SatkerEntity>

    @Query("SELECT * FROM satker WHERE level = 'POLSEK' AND parentId IN (:polresIdList) ")
    fun getPolsekOnMyPolda(polresIdList: List<Int>): List<SatkerEntity>

    @Query("SELECT * FROM satker WHERE level = 'POLSEK' AND parentId = :id")
    fun getPolsekOnMyPolres(id: Int): List<SatkerEntity>
}
