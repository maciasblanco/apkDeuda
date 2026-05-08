package net.sytes.planealo.deudaconsulta.data.remote

import retrofit2.http.*

interface ApiService {
    @GET("api/deuda")
    suspend fun getDeuda(@Query("ci") ci: String): DeudaResponse

    @POST("api/comentario")
    suspend fun enviarComentario(@Body comentario: Map<String, Any>): ComentarioResponse
}