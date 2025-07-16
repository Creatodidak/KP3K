package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.creatodidak.kp3k.database.Entity.MediaDraftEntity
import id.creatodidak.kp3k.database.Entity.MediaEntity
import id.creatodidak.kp3k.helper.MediaType

@Dao
interface DraftMediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaDraftEntity)

    @Query("SELECT * FROM MediaDraftEntity WHERE currentId = :id AND type = :type")
    suspend fun getDraftsByCurrentId(id: Int, type: MediaType): List<MediaDraftEntity>

    @Query("SELECT id FROM MediaDraftEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Int?

    @Query("DELETE FROM MediaDraftEntity WHERE url IN (:urls)")
    suspend fun deleteByUrls(urls: List<String>)

    @Delete
    suspend fun delete(media: List<MediaDraftEntity>)
}
