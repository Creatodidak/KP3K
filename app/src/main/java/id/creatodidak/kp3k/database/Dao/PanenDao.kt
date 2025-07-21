package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.PanenEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity

@Dao
interface PanenDao {
    @Query("SELECT * FROM PanenEntity WHERE komoditas = :komoditas")
    suspend  fun getAll(komoditas: String): List<PanenEntity>

    @Query("SELECT id FROM PanenEntity")
    suspend  fun getAllId(): List<Int>

    @Query("DELETE FROM PanenEntity WHERE status != 'OFFLINE'")
    suspend fun deleteOnlineData()

    @Insert
    suspend fun insertAll(tanamanResponse: List<PanenEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(tanamanResponse: PanenEntity): Long

    @Query("SELECT * FROM panenentity WHERE id = :id")
    suspend fun getPanenById(id: Int): PanenEntity?

    @Query("SELECT * FROM panenentity WHERE tanaman_id = :id ORDER BY createAt ASC")
    suspend fun getPanenByTanamanIdSorted(id: Int): List<PanenEntity>

    @Query("SELECT * FROM panenentity WHERE tanaman_id = :id AND status = 'VERIFIED'")
    suspend fun getPanenByTanamanId(id: Int): List<PanenEntity>?

    @Query("SELECT * FROM panenentity WHERE tanaman_id IN (:ids) AND komoditas = :komoditas")
    suspend fun getPanenByTanamanIds(komoditas: String, ids: List<Int>): List<PanenEntity>

    @Query("SELECT * FROM panenentity WHERE komoditas = :komoditas AND submitter = :nrp AND status = 'REJECTED'")
    suspend fun getPanenRejected(komoditas: String, nrp: String): List<PanenEntity>?

    @Query("SELECT * FROM panenentity WHERE komoditas = :komoditas AND status = 'UNVERIFIED'")
    suspend fun getPanenUnverified(komoditas: String): List<PanenEntity>?

    @Query("UPDATE panenentity SET status = :status, alasan = :alasan, updateAt = :updateAt WHERE id = :id")
    suspend fun updateVerified(id: Int, status: String, alasan: String, updateAt: String): Int

    @Query("DELETE FROM panenentity WHERE id = :id")
    suspend fun delete(id: Int)
}
