package com.example.absensiku.utils

import android.content.Context
import com.example.absensiku.model.ModelUser

/** Menyimpan sesi login sederhana di SharedPreferences. */
class SessionLogin(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    val isLoggedIn: Boolean get() = prefs.getBoolean(KEY_LOGIN, false)
    val userId: Int get() = prefs.getInt(KEY_USER_ID, 0)
    val nama: String get() = prefs.getString(KEY_NAMA, "") ?: ""
    val email: String get() = prefs.getString(KEY_EMAIL, "") ?: ""

    fun login(user: ModelUser) = prefs.edit()
        .putBoolean(KEY_LOGIN, true)
        .putInt(KEY_USER_ID, user.uid)
        .putString(KEY_NAMA, user.nama)
        .putString(KEY_EMAIL, user.email)
        .apply()

    fun logout() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_LOGIN = "is_login"
        const val KEY_USER_ID = "user_id"
        const val KEY_NAMA = "nama"
        const val KEY_EMAIL = "email"
    }
}
