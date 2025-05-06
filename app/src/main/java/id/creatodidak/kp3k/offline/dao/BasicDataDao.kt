package id.creatodidak.kp3k.offline.dao

import androidx.room.*
import id.creatodidak.kp3k.offline.entity.BasicDataEntity

@Dao
interface BasicDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: BasicDataEntity)

    @Query("SELECT * FROM basic_data WHERE id = 1")
    suspend fun get(): BasicDataEntity?
}
