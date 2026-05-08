package net.sytes.planealo.deudaconsulta.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory
import net.sytes.planealo.deudaconsulta.databinding.ActivityMainBinding
import net.sytes.planealo.deudaconsulta.databinding.DialogComentarioBinding
import net.sytes.planealo.deudaconsulta.data.repository.DeudaRepository
import net.sytes.planealo.deudaconsulta.viewmodel.DeudaViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DeudaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = DeudaRepository(applicationContext)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DeudaViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[DeudaViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnConsultar.isEnabled = !it
        }
        viewModel.deudaResult.observe(this) { deuda ->
            if (deuda != null) {
                binding.tvResultado.text = """
                    Nombre: ${deuda.nombre ?: "N/A"}
                    Deuda USD: ${deuda.deudaUsd ?: 0.0}
                    Deuda Bs: ${deuda.deudaBs ?: 0.0}
                    Quincenas pendientes: ${deuda.quincenasPendientes ?: 0}
                    Última actualización: ${deuda.actualizadoEn ?: "desconocida"}
                """.trimIndent()
            } else {
                binding.tvResultado.text = ""
            }
        }
        viewModel.modoActualizacion.observe(this) { modo ->
            binding.tvModo.text = modo ?: ""
        }
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        binding.btnConsultar.setOnClickListener {
            val ci = binding.etCedula.text.toString().trim()
            viewModel.consultarDeuda(ci)
        }
        binding.btnComentar.setOnClickListener {
            val ci = binding.etCedula.text.toString().trim()
            if (ci.isEmpty()) {
                Toast.makeText(this, "Consulte una cédula primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mostrarDialogoComentario(ci)
        }
        binding.btnCalificar.setOnClickListener {
            val reviewManager = ReviewManagerFactory.create(this)
            reviewManager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reviewManager.launchReviewFlow(this, task.result)
                } else {
                    Toast.makeText(this, "Error al abrir reseña", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarDialogoComentario(ci: String) {
        val dialogBinding = DialogComentarioBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Dejar comentario")
            .setView(dialogBinding.root)
            .setPositiveButton("Enviar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val puntuacion = dialogBinding.ratingBar.rating.toInt()
                val comentario = dialogBinding.etComentario.text.toString().trim()
                if (comentario.isEmpty()) {
                    Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.enviarComentario(ci, puntuacion, comentario) { success, msg ->
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    if (success) dialog.dismiss()
                }
            }
        }
        dialog.show()
    }
}