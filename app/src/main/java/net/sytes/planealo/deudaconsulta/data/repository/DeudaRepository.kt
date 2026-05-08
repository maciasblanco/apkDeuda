package net.sytes.planealo.deudaconsulta.data.repository

import android.content.Context
import net.sytes.planealo.deudaconsulta.data.local.AppDatabase
import net.sytes.planealo.deudaconsulta.data.local.DeudaEntity
import net.sytes.planealo.deudaconsulta.data.remote.RetrofitClient
import net.sytes.planealo.deudaconsulta.utils.NetworkUtils

class DeudaRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val api = RetrofitClient.apiService

    suspend fun getDeuda(ci: String): Result<Pair<DeudaEntity?, Boolean>> {
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        return if (isOnline) {
            try {
                val response = api.getDeuda(ci)
                if (response.success && response.nombre != null) {
                    val entity = DeudaEntity(
                        ci = ci,
                        nombre = response.nombre,
                        deudaUsd = response.deudaUsd,
                        deudaBs = response.deudaBs,
                        quincenasPendientes = response.quincenasPendientes,
                        actualizadoEn = response.actualizadoEn ?: ""
                    )
                    db.deudaDao().insertOrUpdate(entity)
                    Result.success(Pair(entity, true))
                } else {
                    Result.failure(Exception(response.error ?: "Error del servidor"))
                }
            } catch (e: Exception) {
                val cached = db.deudaDao().getByCi(ci)
                if (cached != null) Result.success(Pair(cached, false))
                else Result.failure(Exception("Sin conexión y sin datos previos"))
            }
        } else {
            val cached = db.deudaDao().getByCi(ci)
            if (cached != null) Result.success(Pair(cached, false))
            else Result.failure(Exception("Sin internet y sin datos guardados"))
        }
    }

    suspend fun enviarComentario(ci: String, puntuacion: Int, comentario: String): Result<Boolean> {
        if (!NetworkUtils.isNetworkAvailable(context))
            return Result.failure(Exception("Se necesita internet para comentar"))
        return try {
            val body = mapOf("ci" to ci, "puntuacion" to puntuacion, "comentario" to comentario)
            val response = api.enviarComentario(body)
            if (response.success) Result.success(true)
            else Result.failure(Exception(response.message ?: "Error al enviar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}