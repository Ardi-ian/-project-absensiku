package com.example.absensiku.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Mengubah password menjadi hash dengan PBKDF2 + salt acak.
 * Memakai javax.crypto bawaan Android sehingga tanpa dependency tambahan.
 */
object PasswordHasher {
    private const val ALGORITMA = "PBKDF2WithHmacSHA256"
    private const val ITERASI = 120_000
    private const val PANJANG_SALT = 16
    private const val PANJANG_KEY = 256

    /** Membuat salt baru dalam format Base64. */
    fun saltBaru(): String {
        val salt = ByteArray(PANJANG_SALT)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /** Menghitung hash password berdasarkan salt. */
    fun hash(password: String, saltBase64: String): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            Base64.decode(saltBase64, Base64.NO_WRAP),
            ITERASI,
            PANJANG_KEY
        )
        val kunci = SecretKeyFactory.getInstance(ALGORITMA).generateSecret(spec)
        return Base64.encodeToString(kunci.encoded, Base64.NO_WRAP)
    }

    /** Membandingkan password dengan hash tersimpan dalam waktu konstan. */
    fun cocok(password: String, saltBase64: String, hashBase64: String): Boolean {
        val tersimpan = Base64.decode(hashBase64, Base64.NO_WRAP)
        val dihitung = Base64.decode(hash(password, saltBase64), Base64.NO_WRAP)
        return MessageDigest.isEqual(tersimpan, dihitung)
    }
}
