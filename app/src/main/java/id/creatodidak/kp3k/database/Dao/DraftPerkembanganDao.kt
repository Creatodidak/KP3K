package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.creatodidak.kp3k.database.Entity.OwnerEntity
import id.creatodidak.kp3k.database.Entity.PerkembanganDraftEntity
import id.creatodidak.kp3k.database.Entity.TanamanEntity

@Dao
interface DraftPerkembanganDao {
    @Query("SELECT * FROM PerkembanganDraftEntity")
    suspend  fun getAll(): List<PerkembanganDraftEntity>

    @Query("SELECT id FROM PerkembanganDraftEntity")
    suspend  fun getAllId(): List<Int>

    @Query("DELETE FROM PerkembanganDraftEntity WHERE status != 'OFFLINE'")
    suspend fun deleteOnlineData()

    @Insert
    suspend fun insertAll(tanamanResponse: List<PerkembanganDraftEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(tanamanResponse: PerkembanganDraftEntity): Long

    @Query("SELECT * FROM perkembanganDraftEntity WHERE id = :id")
    suspend fun getPanenById(id: Int): PerkembanganDraftEntity?

    @Query("SELECT * FROM perkembangandraftentity WHERE tanaman_id = :id ORDER BY createAt ASC")
    suspend fun getPanenByTanamanIdSorted(id: Int): List<PerkembanganDraftEntity>

    @Query("SELECT * FROM perkembanganDraftEntity WHERE tanaman_id = :id AND status = 'VERIFIED'")
    suspend fun getPanenByTanamanId(id: Int): List<PerkembanganDraftEntity>?

    @Query("SELECT * FROM perkembanganDraftEntity WHERE tanaman_id IN (:ids)")
    suspend fun getPanenByTanamanIds(ids: List<Int>): List<PerkembanganDraftEntity>

    @Query("SELECT * FROM perkembanganDraftEntity WHERE submitter = :nrp AND status = 'REJECTED'")
    suspend fun getPanenRejected(nrp: String): List<PerkembanganDraftEntity>?

    @Query("SELECT * FROM perkembanganDraftEntity WHERE status = 'UNVERIFIED'")
    suspend fun getPanenUnverified(): List<PerkembanganDraftEntity>?

    @Query("UPDATE perkembanganDraftEntity SET status = :status, alasan = :alasan, updateAt = :updateAt WHERE id = :id")
    suspend fun updateVerified(id: Int, status: String, alasan: String, updateAt: String): Int

    @Query("DELETE FROM perkembanganDraftEntity WHERE id = :id")
    suspend fun delete(id: Int)
}
