package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Insert
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

    @Query("SELECT * FROM panenentity WHERE tanaman_id = :id AND status = 'VERIFIED'")
    suspend fun getPanenByTanamanId(id: Int): PanenEntity?
}
