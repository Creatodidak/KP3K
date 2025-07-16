package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import id.creatodidak.kp3k.api.newModel.TanamanResponse
import id.creatodidak.kp3k.database.Entity.LahanEntity
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.TanamanDraftEntity

@Dao
interface DraftTanamanDao {
    @Query("SELECT * FROM TanamanDraftEntity WHERE komoditas = :komoditas")
    suspend  fun getAll(komoditas: String): List<TanamanDraftEntity>

    @Query("SELECT id FROM TanamanDraftEntity")
    suspend  fun getAllId(): List<Int>

    @Query("SELECT id FROM TanamanDraftEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Int?

    @Query("SELECT * FROM TanamanDraftEntity WHERE status IN ('OFFLINECREATE', 'OFFLINEUPDATE') AND lahan_id = :lahan_id AND masatanam = :masatanam")
    suspend fun getDraftTanamanByLahanId(lahan_id: Int, masatanam: String): List<TanamanDraftEntity>

    @Query("DELETE FROM TanamanDraftEntity WHERE status != 'OFFLINE'")
    suspend fun deleteOnlineData()

    @Query("DELETE FROM TanamanDraftEntity WHERE id = :id")
    suspend fun deleteById(id: Int): Int

    @Query("DELETE FROM TanamanDraftEntity WHERE currentId = :id")
    suspend fun deleteByCurrentId(id: Int): Int

    @Delete
    suspend fun delete(tanaman: TanamanDraftEntity)

    @Insert
    suspend fun insertAll(tanamanResponse: List<TanamanDraftEntity>)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertTanaman(tanaman: TanamanDraftEntity): Long

    @Query("SELECT * FROM TanamanDraftEntity WHERE komoditas = :komoditas AND lahan_id = :lahan_id AND status = 'VERIFIED'")
    suspend fun getTanamanByLahanId(komoditas: String, lahan_id: Int): List<TanamanDraftEntity>?

    @Query("SELECT * FROM TanamanDraftEntity WHERE komoditas = :komoditas AND lahan_id IN (:lahanids)")
    suspend fun getTanamanByLahanIds(komoditas: String, lahanids: List<Int>): List<TanamanDraftEntity>?
}
