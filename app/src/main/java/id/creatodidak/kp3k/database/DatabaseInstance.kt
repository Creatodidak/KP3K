package id.creatodidak.kp3k.database

import android.content.Context
import androidx.room.Room

object DatabaseInstance {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "kp3k_db"
            ).build().also { INSTANCE = it }
        }
    }
}
