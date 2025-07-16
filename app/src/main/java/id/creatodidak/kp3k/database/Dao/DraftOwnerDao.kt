package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.creatodidak.kp3k.api.newModel.OwnerResponseItem
import id.creatodidak.kp3k.database.Entity.OwnerDraftEntity

@Dao
interface DraftOwnerDao {
    @Query("SELECT * FROM OwnerDraftEntity WHERE status = 'VERIFIED' ORDER BY nama ASC")
    suspend  fun getAll(): List<OwnerDraftEntity>

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend  fun getAllByKomoditas(komoditas: String): List<OwnerDraftEntity>

    @Query("SELECT * FROM OwnerDraftEntity WHERE id = :id")
    suspend fun getOwnerById(id: Int): OwnerDraftEntity?

    @Query("DELETE FROM OwnerDraftEntity WHERE status NOT IN ('OFFLINEUPDATE', 'OFFLINECREATE')")
    suspend fun deleteOnlineData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ownerResponse: List<OwnerDraftEntity>)

    @Query("SELECT id FROM OwnerDraftEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Int?

    @Insert
    suspend fun insertOwnerData(OwnerDraftEntity: OwnerDraftEntity): Long

    @Update
    suspend fun updateOwnerDatas(ownerResponse: OwnerDraftEntity): Int

    @Update
    suspend fun updateOwnerData(ownerResponse: OwnerDraftEntity)

    @Update
    suspend fun updateSomeOwnerData(ownerResponse: List<OwnerDraftEntity>)

    @Query("SELECT COUNT(*) FROM OwnerDraftEntity WHERE komoditas = :komoditas AND provinsi_id = :provinsiId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByProvinsi(komoditas: String, provinsiId: Int): Int

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND provinsi_id = :provinsiId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByProvinsi(komoditas: String, provinsiId: Int): List<OwnerDraftEntity>

    @Query("SELECT COUNT(*) FROM OwnerDraftEntity WHERE komoditas = :komoditas AND kabupaten_id = :kabupatenId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByKabupaten(komoditas: String, kabupatenId: Int): Int

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND kabupaten_id = :kabupatenId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByKabupaten(komoditas: String, kabupatenId: Int): List<OwnerDraftEntity>

    @Query("SELECT COUNT(*) FROM OwnerDraftEntity WHERE komoditas = :komoditas AND kecamatan_id = :kecamatanId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByKecamatan(komoditas: String, kecamatanId: Int): Int

    @Query("SELECT COUNT(*) FROM OwnerDraftEntity WHERE komoditas = :komoditas AND kecamatan_id IN (:kecamatanIds) AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByKecamatanIds(komoditas: String, kecamatanIds: List<Int>): Int

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND kecamatan_id = :kecamatanId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByKecamatan(komoditas: String, kecamatanId: Int): List<OwnerDraftEntity>

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND kecamatan_id IN (:kecamatanId) AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByKecamatans(komoditas: String, kecamatanId: List<Int>): List<OwnerDraftEntity>

    @Query("SELECT COUNT(*) FROM OwnerDraftEntity WHERE komoditas = :komoditas AND desa_id = :desaId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun countByDesa(komoditas: String, desaId: Int): Int

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND desa_id = :desaId AND status = 'VERIFIED' ORDER BY nama ASC")
    suspend fun getOwnerByDesa(komoditas: String, desaId: Int): List<OwnerDraftEntity>

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND status IN ('OFFLINEUPDATE', 'OFFLINECREATE')")
    suspend fun getOfflineData(komoditas: String): List<OwnerDraftEntity>

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND status = 'UNVERIFIED'")
    suspend fun getUnverifiedData(komoditas: String): List<OwnerDraftEntity>

    @Query("SELECT * FROM OwnerDraftEntity WHERE komoditas = :komoditas AND status = 'REJECTED'")
    suspend fun getRejectedData(komoditas: String): List<OwnerDraftEntity>

    @Delete
    suspend fun delete(owner: OwnerDraftEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleData(ownerResponse: OwnerDraftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(owner: OwnerDraftEntity): Long

}
