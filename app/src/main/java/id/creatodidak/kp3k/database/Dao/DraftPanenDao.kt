package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.PanenDraftEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity

@Dao
interface DraftPanenDao {
    @Query("SELECT * FROM panendraftentity WHERE komoditas = :komoditas")
    suspend  fun getAll(komoditas: String): List<PanenDraftEntity>

    @Query("SELECT id FROM panendraftentity")
    suspend  fun getAllId(): List<Int>

    @Query("DELETE FROM panendraftentity WHERE status != 'OFFLINE'")
    suspend fun deleteOnlineData()

    @Insert
    suspend fun insertAll(panenResponse: List<PanenDraftEntity>)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertSingle(panen: PanenDraftEntity): Long
    
    @Delete
    suspend fun delete(panen: PanenDraftEntity)

    @Query("SELECT * FROM panendraftentity WHERE tanaman_id = :id AND status = 'VERIFIED'")
    suspend fun getPanenByTanamanId(id: Int): PanenDraftEntity?

    @Query("SELECT * FROM panendraftentity WHERE tanaman_id IN (:ids) AND komoditas = :komoditas")
    suspend fun getPanenDraftByTanamanIds(komoditas: String, ids: List<Int>): List<PanenDraftEntity>

    @Query("SELECT id FROM panendraftentity ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Int?

    @Query("DELETE FROM panendraftentity WHERE id = :id")
    suspend fun deleteDraftById(id: Int): Int

    @Query("DELETE FROM panendraftentity WHERE currentId = :id")
    suspend fun deleteDraftByCurrentId(id: Int): Int

    @Query("UPDATE panendraftentity SET foto1 = :foto1, foto2 = :foto2, foto3 = :foto3, foto4 = :foto4 WHERE id = :id")
    suspend fun updateFoto(id: Int, foto1: String, foto2: String, foto3: String, foto4: String): Int
}
