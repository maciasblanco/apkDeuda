package net.sytes.planealo.deudaconsulta.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeudaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(deuda: DeudaEntity)

    @Query("SELECT * FROM deuda_cache WHERE ci = :ci LIMIT 1")
    suspend fun getByCi(ci: String): DeudaEntity?
}