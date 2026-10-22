package com.example.absensiku.viewmodel

import android.app.Application
import com.example.absensiku.model.ModelDatabase
import com.example.absensiku.utils.AturanAbsen

class AbsenViewModel(app: Application) : BaseAbsensiViewModel(app) {
    /** Memeriksa ulang aturan absen harian sebelum menyimpan, null bila boleh. */
    suspend fun pesanTolak(status: String): String? =
        AturanAbsen.pesanTolak(status, hitungKondisi())

    suspend fun simpan(item: ModelDatabase) = dao.insert(item.copy(userId = userId))
}
