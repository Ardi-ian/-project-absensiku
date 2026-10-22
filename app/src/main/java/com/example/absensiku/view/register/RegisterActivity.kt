package com.example.absensiku.view.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.absensiku.databinding.ActivityRegisterBinding
import com.example.absensiku.utils.ValidasiAkun
import com.example.absensiku.view.login.LoginActivity
import com.example.absensiku.viewmodel.AuthViewModel
import com.example.absensiku.viewmodel.HasilAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvMasuk.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }
        binding.btnDaftar.setOnClickListener { daftar() }

        viewModel.hasil.observe(this) { hasil ->
            when (hasil) {
                is HasilAuth.Berhasil -> {
                    Toast.makeText(this, hasil.pesan, Toast.LENGTH_SHORT).show()
                    val email = binding.etEmail.text.toString().trim().lowercase()
                    viewModel.reset()
                    startActivity(
                        Intent(this, LoginActivity::class.java)
                            .putExtra(LoginActivity.EXTRA_EMAIL, email)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    finish()
                }
                is HasilAuth.Gagal -> {
                    binding.tilEmail.error = hasil.pesan
                    viewModel.reset()
                }
                null -> Unit
            }
        }
    }

    private fun daftar() {
        val nama = binding.etNama.text.toString().trim()
        val email = binding.etEmail.text.toString().trim().lowercase()
        val pass = binding.etPassword.text.toString()
        val konfirmasi = binding.etKonfirmasi.text.toString()
        binding.tilNama.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilKonfirmasi.error = null
        when {
            nama.isEmpty() -> binding.tilNama.error = "Nama wajib diisi"
            email.isEmpty() -> binding.tilEmail.error = "Email wajib diisi"
            !ValidasiAkun.POLA_EMAIL.matches(email) -> binding.tilEmail.error = "Format email tidak valid"
            pass.length < ValidasiAkun.MIN_PASSWORD ->
                binding.tilPassword.error = "Minimal ${ValidasiAkun.MIN_PASSWORD} karakter"
            konfirmasi != pass -> binding.tilKonfirmasi.error = "Konfirmasi password tidak sama"
            else -> viewModel.daftar(nama, email, pass)
        }
    }
}
