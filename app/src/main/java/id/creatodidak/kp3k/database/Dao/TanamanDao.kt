package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import id.creatodidak.kp3k.database.Entity.TanamanEntity

@Dao
interface TanamanDao {
    @Query("SELECT * FROM TanamanEntity WHERE komoditas = :komoditas")
    suspend  fun getAll(komoditas: String): List<TanamanEntity>

    @Query("SELECT id FROM TanamanEntity")
    suspend  fun getAllId(): List<Int>

    @Query("SELECT * FROM TanamanEntity WHERE status = 'VERIFIED' AND lahan_id = :lahan_id AND masatanam = :masatanam")
    suspend fun getVerifiedTanamanByLahanId(lahan_id: Int, masatanam: String): List<TanamanEntity>

    @Query("DELETE FROM TanamanEntity WHERE status != 'OFFLINE'")
    suspend fun deleteOnlineData()

    @Insert
    suspend fun insertAll(tanamanResponse: List<TanamanEntity>)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertSingle(tanamanResponse: TanamanEntity)

    @Query("SELECT * FROM TanamanEntity WHERE komoditas = :komoditas AND lahan_id = :lahan_id AND status = 'VERIFIED'")
    suspend fun getTanamanByLahanId(komoditas: String, lahan_id: Int): List<TanamanEntity>?

    @Query("SELECT * FROM TanamanEntity WHERE komoditas = :komoditas AND lahan_id IN (:lahanids)")
    suspend fun getTanamanByLahanIds(komoditas: String, lahanids: List<Int>): List<TanamanEntity>?

    @Query("SELECT * FROM TanamanEntity WHERE komoditas = :komoditas AND lahan_id IN (:lahanids) AND status = 'UNVERIFIED'")
    suspend fun getUnverifiedTanamanByLahanIds(komoditas: String, lahanids: List<Int>): List<TanamanEntity>?

    @Query("SELECT * FROM TanamanEntity WHERE komoditas = :komoditas AND lahan_id IN (:lahanids) AND status = 'REJECTED'")
    suspend fun getRejectedTanamanByLahanIds(komoditas: String, lahanids: List<Int>): List<TanamanEntity>?

    @Delete
    suspend fun delete(tanamanEntity: TanamanEntity)

    @Query("DELETE FROM TanamanEntity WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM TanamanEntity WHERE id = :id")
    suspend fun getById(id: Int): TanamanEntity?
}
