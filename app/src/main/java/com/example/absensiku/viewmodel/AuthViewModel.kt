package com.example.absensiku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.absensiku.database.AppDatabase
import com.example.absensiku.model.ModelUser
import com.example.absensiku.utils.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Hasil proses daftar atau login. */
sealed class HasilAuth {
    data class Berhasil(val pesan: String, val user: ModelUser?) : HasilAuth()
    data class Gagal(val pesan: String) : HasilAuth()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).userDao()
    private val absensiDao = AppDatabase.get(app).absensiDao()

    private val _hasil = MutableLiveData<HasilAuth?>()
    val hasil: LiveData<HasilAuth?> = _hasil

    /** Mendaftarkan akun baru. Email harus unik. */
    fun daftar(nama: String, email: String, password: String) {
        viewModelScope.launch {
            if (dao.adaEmail(email)) {
                _hasil.value = HasilAuth.Gagal("Email sudah terdaftar")
                return@launch
            }
            val salt = PasswordHasher.saltBaru()
            val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password, salt) }
            val uid = dao.insert(
                ModelUser(
                    nama = nama,
                    email = email,
                    passwordHash = hash,
                    salt = salt,
                    dibuatPada = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
                )
            )
            // Catatan absen yang dibuat sebelum ada akun sekarang dimiliki akun pertama.
            if (uid == 1L) absensiDao.adopsiCatatanLama(uid.toInt())
            _hasil.value = HasilAuth.Berhasil("Akun berhasil dibuat. Silakan masuk.", null)
        }
    }

    /** Memeriksa email + password terhadap akun yang tersimpan. */
    fun masuk(email: String, password: String) {
        viewModelScope.launch {
            val user = dao.byEmail(email)
            if (user == null) {
                _hasil.value = HasilAuth.Gagal("Email atau password salah")
                return@launch
            }
            val valid = withContext(Dispatchers.Default) {
                PasswordHasher.cocok(password, user.salt, user.passwordHash)
            }
            if (!valid) {
                _hasil.value = HasilAuth.Gagal("Email atau password salah")
                return@launch
            }
            _hasil.value = HasilAuth.Berhasil("Selamat datang, ${user.nama}!", user)
        }
    }

    /** Mengosongkan hasil agar tidak diproses ulang saat activity dibuat ulang. */
    fun reset() {
        _hasil.value = null
    }
}
