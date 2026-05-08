package net.sytes.planealo.deudaconsulta.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.sytes.planealo.deudaconsulta.data.local.DeudaEntity
import net.sytes.planealo.deudaconsulta.data.repository.DeudaRepository
import kotlinx.coroutines.launch

class DeudaViewModel(private val repository: DeudaRepository) : ViewModel() {

    private val _deudaResult = MutableLiveData<DeudaEntity?>()
    val deudaResult: LiveData<DeudaEntity?> = _deudaResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _modo = MutableLiveData<String?>()
    val modoActualizacion: LiveData<String?> = _modo

    fun consultarDeuda(ci: String) {
        if (ci.isBlank() || !ci.matches(Regex("\\d+"))) {
            _errorMessage.value = "Cédula inválida"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getDeuda(ci)
            _isLoading.value = false
            result.onSuccess { (entity, fromOnline) ->
                _deudaResult.value = entity
                _modo.value = if (fromOnline) "✅ ACTUALIZADO (en línea)" else "⚠️ MONTO NO ACTUALIZADO (sin conexión)"
            }.onFailure { e ->
                _errorMessage.value = e.message
                _deudaResult.value = null
                _modo.value = null
            }
        }
    }

    fun enviarComentario(ci: String, puntuacion: Int, comentario: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.enviarComentario(ci, puntuacion, comentario)
            result.onSuccess { onResult(true, "Comentario enviado") }
                .onFailure { onResult(false, it.message ?: "Error") }
        }
    }

    fun clearError() { _errorMessage.value = null }
}