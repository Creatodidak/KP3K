package id.creatodidak.kp3k.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.creatodidak.kp3k.database.Entity.PejabatEntity
import id.creatodidak.kp3k.database.Entity.PersonilEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPejabat(list: List<PejabatEntity>)

    @Query("SELECT * FROM pejabatentity ORDER BY id ASC")
    suspend fun getPejabat(): List<PejabatEntity>

    @Query("SELECT * FROM pejabatentity where satkerId in (:ids) ORDER BY id ASC")
    suspend fun getPejabatBySatkerId(ids: List<Int>): List<PejabatEntity>

    @Query("SELECT * FROM pejabatentity WHERE nrp = :nrp")
    suspend fun getPejabatByNrp(nrp: String): PejabatEntity?

    @Query("SELECT * FROM pejabatentity WHERE role = 'PAMATWIL' ORDER BY id ASC")
    suspend fun getAllPamatwil(): List<PejabatEntity>

    @Query("SELECT * FROM pejabatentity WHERE role = 'PAMATWIL' AND wilayah = :wilayahId ORDER BY id ASC")
    suspend fun getPamatwilByWilayahId(wilayahId: Int): List<PejabatEntity>

    @Query("SELECT * FROM pejabatentity WHERE role = 'PJUPOLRES' ORDER BY id ASC")
    suspend fun getAllPjupolres(): List<PejabatEntity>

    @Query("SELECT * FROM pejabatentity WHERE role = 'PJUPOLRES' AND satkerId= :id ORDER BY id ASC")
    suspend fun getPjupolresBySatkerId(id: Int): List<PejabatEntity>

    @Query("SELECT * FROM pejabatentity WHERE role = 'PJUPOLRES' AND wilayah= :id ORDER BY id ASC")
    suspend fun getPjupolresByWilayahId(id: Int): List<PejabatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonil(list: List<PersonilEntity>)

    @Query("SELECT * FROM personilentity")
    suspend fun getPersonil(): List<PersonilEntity>

    @Query("SELECT * FROM personilentity WHERE nrp = :nrp")
    suspend fun getPersonilByNrp(nrp: String): PersonilEntity?

    @Query("SELECT * FROM personilentity WHERE satkerId = :satkerId ORDER BY nama ASC")
    suspend fun getPersonilBySatkerId(satkerId: Int): List<PersonilEntity>

    @Query("SELECT * FROM personilentity WHERE satkerId in (:satkerId) ORDER BY nama ASC")
    suspend fun getPersonilBySatkerIds(satkerId: List<Int>): List<PersonilEntity>

    @Query("SELECT * FROM personilentity WHERE satkerId = :satkerId AND role = :role ORDER BY nama ASC")
    suspend fun getPersonilBySatkerIdAndRole(satkerId: Int, role: String): List<PersonilEntity>

    @Query("SELECT * FROM personilentity WHERE desaBinaanId = :id")
    suspend fun getPersonilByDesaBinaanId(id: Int): PersonilEntity?

}