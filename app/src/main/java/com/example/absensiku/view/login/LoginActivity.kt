package com.example.absensiku.view.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.absensiku.databinding.ActivityLoginBinding
import com.example.absensiku.utils.SessionLogin
import com.example.absensiku.utils.ValidasiAkun
import com.example.absensiku.view.main.MainActivity
import com.example.absensiku.view.register.RegisterActivity
import com.example.absensiku.viewmodel.AuthViewModel
import com.example.absensiku.viewmodel.HasilAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionLogin
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionLogin(this)
        if (session.isLoggedIn) {
            // Sesi lama (belum ada id akun) tidak lagi berlaku, paksa login ulang.
            if (session.userId > 0) { openMain(); return }
            session.logout()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra(EXTRA_EMAIL)?.let { binding.etEmail.setText(it) }

        binding.btnLogin.setOnClickListener { masuk() }
        binding.tvDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.hasil.observe(this) { hasil ->
            when (hasil) {
                is HasilAuth.Berhasil -> {
                    hasil.user?.let { session.login(it) }
                    viewModel.reset()
                    openMain()
                }
                is HasilAuth.Gagal -> {
                    binding.tilPassword.error = hasil.pesan
                    viewModel.reset()
                }
                null -> Unit
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_EMAIL)?.let { binding.etEmail.setText(it) }
    }

    private fun masuk() {
        val email = binding.etEmail.text.toString().trim().lowercase()
        val pass = binding.etPassword.text.toString()
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        when {
            email.isEmpty() -> binding.tilEmail.error = "Email wajib diisi"
            pass.isEmpty() -> binding.tilPassword.error = "Password wajib diisi"
            pass.length < ValidasiAkun.MIN_PASSWORD ->
                binding.tilPassword.error = "Minimal ${ValidasiAkun.MIN_PASSWORD} karakter"
            else -> viewModel.masuk(email, pass)
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
