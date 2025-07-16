package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import id.creatodidak.kp3k.api.newModel.LahanResponseItem
import id.creatodidak.kp3k.database.Entity.LahanDraftEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.database.Relation.LahanWithTanaman

@Dao
interface DraftLahanDao {
    @Query("SELECT * FROM LahanDraftEntity WHERE komoditas = :komoditas")
    suspend  fun getAll(komoditas: String): List<LahanDraftEntity>

    @Query("SELECT id FROM LahanDraftEntity")
    suspend  fun getAllId(): List<Int>

    @Query("DELETE FROM LahanDraftEntity WHERE status != 'OFFLINE'")
    suspend fun deleteOnlineData()

    @Insert
    suspend fun insertAll(lahanResponse: List<LahanDraftEntity>)

    @Transaction
    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE')")
    suspend fun getLahanWithTanaman(): List<LahanWithTanaman>

    @Query("SELECT id FROM LahanDraftEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(LahanDraftEntity: LahanDraftEntity): Long

    @Delete
    suspend fun delete(LahanDraftEntity: LahanDraftEntity)

    @Query("DELETE FROM LahanDraftEntity WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("DELETE FROM LahanDraftEntity WHERE currentId = :id")
    suspend fun deleteByCurrentId(id: Int): Int

    @Query("SELECT * FROM LahanDraftEntity WHERE status = 'OFFLINECREATE'")
    suspend fun getAllDraftCreate(): List<LahanDraftEntity>

    @Query("SELECT * FROM LahanDraftEntity WHERE id = :id AND status IN ('OFFLINECREATE', 'OFFLINEUPDATE')")
    suspend fun getLahanById(id: Int): LahanDraftEntity

    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND komoditas = :komoditas")
    suspend fun getLDraftLahan(komoditas: String): List<LahanDraftEntity>

    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND komoditas = :komoditas AND provinsi_id = :id ORDER BY owner_id ASC")
    suspend fun getDraftLahanByProvinsi(komoditas: String, id: Int): List<LahanDraftEntity>

    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND komoditas = :komoditas AND kabupaten_id = :id ORDER BY owner_id ASC")
    suspend fun getDraftLahanByKabupaten(komoditas: String, id: Int): List<LahanDraftEntity>

    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND komoditas = :komoditas AND kecamatan_id = :id ORDER BY owner_id ASC")
    suspend fun getDraftLahanByKecamatan(komoditas: String, id: Int): List<LahanDraftEntity>

    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND komoditas = :komoditas AND kecamatan_id IN (:id) ORDER BY owner_id ASC")
    suspend fun getDraftLahanByKecamatans(komoditas: String, id: List<Int>): List<LahanDraftEntity>

    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND komoditas = :komoditas AND desa_id = :id ORDER BY owner_id ASC")
    suspend fun getDraftLahanByDesa(komoditas: String, id: Int): List<LahanDraftEntity>

    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND komoditas = :komoditas AND owner_id = :id ORDER BY owner_id ASC")
    suspend fun getDraftLahanByOwner(komoditas: String, id: Int): List<LahanDraftEntity>

    @Query("SELECT * FROM LahanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND komoditas = :komoditas ORDER BY owner_id ASC")
    suspend fun getOfflineLahan(komoditas: String): List<LahanDraftEntity>
}
