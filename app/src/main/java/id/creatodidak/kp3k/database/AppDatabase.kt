package id.creatodidak.kp3k.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.creatodidak.kp3k.database.Dao.*
import id.creatodidak.kp3k.database.Entity.*
import id.creatodidak.kp3k.helper.Converters

@Database(
    entities = [
        LahanEntity::class,
        LahanDraftEntity::class,
        MediaEntity::class,
        MediaDraftEntity::class,
        OwnerEntity::class,
        OwnerDraftEntity::class,
        PanenEntity::class,
        PanenDraftEntity::class,
        PerkembanganEntity::class,
        PerkembanganDraftEntity::class,
        TanamanEntity::class,
        TanamanDraftEntity::class,
        ProvinsiEntity::class,
        KabupatenEntity::class,
        KecamatanEntity::class,
        DesaEntity::class,
        SatkerEntity::class,
        PolsekPivotEntity::class,
        PejabatEntity::class,
        PersonilEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun draftMediaDao(): DraftMediaDao
    abstract fun lahanDao(): LahanDao
    abstract fun draftLahanDao(): DraftLahanDao
    abstract fun ownerDao(): OwnerDao
    abstract fun draftOwnerDao(): DraftOwnerDao
    abstract fun panenDao(): PanenDao
    abstract fun draftPanenDao(): DraftPanenDao
    abstract fun perkembanganDao(): PerkembanganDao
    abstract fun draftPerkembanganDao(): DraftPerkembanganDao
    abstract fun draftTanamanDao(): DraftTanamanDao
    abstract fun tanamanDao(): TanamanDao
    abstract fun wilayahDao(): WilayahDao
    abstract fun userDao() : UserDao
}
