package id.creatodidak.kp3k.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.creatodidak.kp3k.offline.dao.BasicDataDao
import id.creatodidak.kp3k.offline.entity.BasicDataEntity

@Database(entities = [BasicDataEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun basicDataDao(): BasicDataDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offlinedata"
                )
                    .fallbackToDestructiveMigration() // Handle schema changes
                    .build().also { INSTANCE = it }
            }
        }
    }
}
