package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.creatodidak.kp3k.api.newModel.OwnerResponseItem
import id.creatodidak.kp3k.database.Entity.OwnerEntity

@Dao
interface OwnerDao {
    @Query("SELECT * FROM OwnerEntity WHERE status = 'VERIFIED' ORDER BY nama ASC")
    suspend  fun getAll(): List<OwnerEntity>

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend  fun getAllByKomoditas(komoditas: String): List<OwnerEntity>

    @Query("SELECT * FROM OwnerEntity WHERE id = :id")
    suspend fun getOwnerById(id: Int): OwnerEntity?

    @Query("DELETE FROM OwnerEntity WHERE status NOT IN ('OFFLINEUPDATE', 'OFFLINECREATE')")
    suspend fun deleteOnlineData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ownerResponse: List<OwnerEntity>)

    @Query("SELECT id FROM OwnerEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Int?

    @Insert
    suspend fun insertOwnerData(OwnerEntity: OwnerEntity): Long

    @Update
    suspend fun updateOwnerDatas(ownerResponse: OwnerEntity): Int

    @Update
    suspend fun updateOwnerData(ownerResponse: OwnerEntity)

    @Update
    suspend fun updateSomeOwnerData(ownerResponse: List<OwnerEntity>)

    @Query("SELECT COUNT(*) FROM OwnerEntity WHERE komoditas = :komoditas AND provinsi_id = :provinsiId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByProvinsi(komoditas: String, provinsiId: Int): Int

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND provinsi_id = :provinsiId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByProvinsi(komoditas: String, provinsiId: Int): List<OwnerEntity>

    @Query("SELECT COUNT(*) FROM OwnerEntity WHERE komoditas = :komoditas AND kabupaten_id = :kabupatenId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByKabupaten(komoditas: String, kabupatenId: Int): Int

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND kabupaten_id = :kabupatenId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByKabupaten(komoditas: String, kabupatenId: Int): List<OwnerEntity>

    @Query("SELECT COUNT(*) FROM OwnerEntity WHERE komoditas = :komoditas AND kecamatan_id = :kecamatanId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByKecamatan(komoditas: String, kecamatanId: Int): Int

    @Query("SELECT COUNT(*) FROM OwnerEntity WHERE komoditas = :komoditas AND kecamatan_id IN (:kecamatanIds) AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByKecamatanIds(komoditas: String, kecamatanIds: List<Int>): Int

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND kecamatan_id = :kecamatanId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByKecamatan(komoditas: String, kecamatanId: Int): List<OwnerEntity>

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND kecamatan_id IN (:kecamatanId) AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByKecamatans(komoditas: String, kecamatanId: List<Int>): List<OwnerEntity>

    @Query("SELECT COUNT(*) FROM OwnerEntity WHERE komoditas = :komoditas AND desa_id = :desaId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByDesa(komoditas: String, desaId: Int): Int

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND desa_id = :desaId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByDesa(komoditas: String, desaId: Int): List<OwnerEntity>

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND status IN ('OFFLINEUPDATE', 'OFFLINECREATE')")
    suspend fun getOfflineData(komoditas: String): List<OwnerEntity>

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND status = 'UNVERIFIED'")
    suspend fun getUnverifiedData(komoditas: String): List<OwnerEntity>

    @Query("SELECT * FROM OwnerEntity WHERE komoditas = :komoditas AND status = 'REJECTED'")
    suspend fun getRejectedData(komoditas: String): List<OwnerEntity>

    @Delete
    suspend fun delete(owner: OwnerEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleData(ownerResponse: OwnerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(owner: OwnerEntity): Long

}
