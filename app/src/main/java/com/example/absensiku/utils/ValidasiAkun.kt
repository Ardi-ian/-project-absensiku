package com.example.absensiku.utils

/** Aturan form akun yang dipakai bersama oleh layar daftar dan login. */
object ValidasiAkun {
    const val MIN_PASSWORD = 6
    val POLA_EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
}
