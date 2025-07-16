package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import id.creatodidak.kp3k.api.newModel.LahanResponseItem
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity
import id.creatodidak.kp3k.database.Relation.LahanWithTanaman

@Dao
interface LahanDao {
    @Query("SELECT * FROM LahanEntity WHERE komoditas = :komoditas AND status = 'VERIFIED' ORDER BY owner_id ASC")
    suspend  fun getAll(komoditas: String): List<LahanEntity>

    @Query("SELECT id FROM LahanEntity")
    suspend  fun getAllId(): List<Int>

    @Query("DELETE FROM LahanEntity WHERE status != 'OFFLINE'")
    suspend fun deleteOnlineData()

    @Insert
    suspend fun insertAll(lahanResponse: List<LahanEntity>)

    @Transaction
    @Query("SELECT * FROM LahanEntity WHERE status = 'VERIFIED'")
    suspend fun getLahanWithTanaman(): List<LahanWithTanaman>

    @Query("SELECT id FROM LahanEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lahanEntity: LahanEntity): Long

    @Delete
    suspend fun delete(lahanEntity: LahanEntity)

    @Query("DELETE FROM LahanEntity WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("DELETE FROM LahanEntity WHERE id = :id AND status = :status")
    suspend fun deleteByIdAndStatus(id: Int, status: String): Int

    @Query("SELECT * FROM LahanEntity WHERE status = 'OFFLINECREATE'")
    suspend fun getAllDraftCreate(): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE id = :id")
    suspend fun getLahanById(id: Int): LahanEntity

    @Query("SELECT * FROM LahanEntity WHERE status == 'VERIFIED' AND komoditas = :komoditas ORDER BY owner_id ASC")
    suspend fun getLVerifiedLahan(komoditas: String): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'VERIFIED' AND komoditas = :komoditas AND provinsi_id = :id ORDER BY owner_id ASC")
    suspend fun getVerifiedLahanByProvinsi(komoditas: String, id: Int): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'VERIFIED' AND komoditas = :komoditas AND kabupaten_id = :id ORDER BY owner_id ASC")
    suspend fun getVerifiedLahanByKabupaten(komoditas: String, id: Int): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'VERIFIED' AND komoditas = :komoditas AND kecamatan_id = :id ORDER BY owner_id ASC")
    suspend fun getVerifiedLahanByKecamatan(komoditas: String, id: Int): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'VERIFIED' AND komoditas = :komoditas AND kecamatan_id IN (:id) ORDER BY owner_id ASC")
    suspend fun getVerifiedLahanByKecamatans(komoditas: String, id: List<Int>): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'VERIFIED' AND komoditas = :komoditas AND desa_id = :id ORDER BY owner_id ASC")
    suspend fun getVerifiedLahanByDesa(komoditas: String, id: Int): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'VERIFIED' AND komoditas = :komoditas AND owner_id = :id ORDER BY owner_id ASC")
    suspend fun getVerifiedLahanByOwner(komoditas: String, id: Int): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'UNVERIFIED' AND komoditas = :komoditas ORDER BY owner_id ASC")
    suspend fun getUnverifiedLahan(komoditas: String): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'REJECTED' AND komoditas = :komoditas ORDER BY owner_id ASC")
    suspend fun getRejectedLahan(komoditas: String): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'REJECTED' AND komoditas = :komoditas AND kecamatan_id IN (:id) ORDER BY owner_id ASC")
    suspend fun getRejectedLahanKecamatan(komoditas: String, id: List<Int>): List<LahanEntity>

    @Query("SELECT * FROM LahanEntity WHERE status = 'REJECTED' AND komoditas = :komoditas AND desa_id = :desaid ORDER BY owner_id ASC")
    suspend fun getRejectedLahanDesa(komoditas: String, desaid: Int): List<LahanEntity>


}
