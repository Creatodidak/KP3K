package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.PerkembanganEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity

@Dao
interface PerkembanganDao {
    @Query("SELECT * FROM PerkembanganEntity")
    suspend  fun getAll(): List<PerkembanganEntity>

    @Query("SELECT id FROM PerkembanganEntity")
    suspend  fun getAllId(): List<Int>

    @Query("DELETE FROM PerkembanganEntity WHERE status != 'OFFLINE'")
    suspend fun deleteOnlineData()

    @Insert
    suspend fun insertAll(tanamanResponse: List<PerkembanganEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(tanamanResponse: PerkembanganEntity): Long

    @Query("SELECT * FROM perkembanganEntity WHERE id = :id")
    suspend fun getPanenById(id: Int): PerkembanganEntity?

    @Query("SELECT * FROM perkembanganEntity WHERE tanaman_id = :id ORDER BY createAt ASC")
    suspend fun getPanenByTanamanIdSorted(id: Int): List<PerkembanganEntity>

    @Query("SELECT * FROM perkembanganEntity WHERE tanaman_id = :id AND status = 'VERIFIED'")
    suspend fun getPanenByTanamanId(id: Int): List<PerkembanganEntity>?

    @Query("SELECT * FROM perkembanganEntity WHERE tanaman_id IN (:ids)")
    suspend fun getPanenByTanamanIds(ids: List<Int>): List<PerkembanganEntity>

    @Query("SELECT * FROM perkembanganEntity WHERE submitter = :nrp AND status = 'REJECTED'")
    suspend fun getPanenRejected(nrp: String): List<PerkembanganEntity>?

    @Query("UPDATE perkembanganEntity SET status = :status, alasan = :alasan, updateAt = :updateAt WHERE id = :id")
    suspend fun updateVerified(id: Int, status: String, alasan: String, updateAt: String): Int

    @Query("DELETE FROM perkembanganEntity WHERE id = :id")
    suspend fun delete(id: Int)
}
