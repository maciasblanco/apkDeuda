package net.sytes.planealo.deudaconsulta.data.remote

import com.google.gson.annotations.SerializedName

data class DeudaResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("deuda_usd") val deudaUsd: Double?,
    @SerializedName("deuda_bs") val deudaBs: Double?,
    @SerializedName("quincenas_pendientes") val quincenasPendientes: Int?,
    @SerializedName("actualizado_en") val actualizadoEn: String?,
    @SerializedName("error") val error: String?
)