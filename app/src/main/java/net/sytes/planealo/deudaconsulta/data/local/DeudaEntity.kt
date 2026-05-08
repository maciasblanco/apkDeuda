package net.sytes.planealo.deudaconsulta.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deuda_cache")
data class DeudaEntity(
    @PrimaryKey val ci: String,
    val nombre: String?,
    val deudaUsd: Double?,
    val deudaBs: Double?,
    val quincenasPendientes: Int?,
    val actualizadoEn: String?,
    val ultimaConsulta: Long = System.currentTimeMillis()
)