package com.example.absensiku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.absensiku.database.AppDatabase
import com.example.absensiku.model.ModelDatabase
import com.example.absensiku.utils.KondisiAbsen
import com.example.absensiku.utils.SessionLogin
import com.example.absensiku.utils.StatusAbsen
import com.example.absensiku.utils.Waktu
import kotlinx.coroutines.launch

/** Dasar viewmodel yang menyediakan kondisi absen hari ini. */
abstract class BaseAbsensiViewModel(app: Application) : AndroidViewModel(app) {
    protected val dao = AppDatabase.get(app).absensiDao()

    /** Id akun yang sedang login, dipakai agar aturan absen berlaku per pengguna. */
    protected val userId: Int = SessionLogin(app).userId

    private val _kondisiHariIni = MutableLiveData(KondisiAbsen())
    val kondisiHariIni: LiveData<KondisiAbsen> = _kondisiHariIni

    private val _statusHariIni = MutableLiveData(StatusAbsen.BELUM_MASUK)

    /** Ringkasan status absen hari ini untuk header. */
    val statusHariIni: LiveData<String> = _statusHariIni

    /** Menghitung ulang kondisi absen untuk hari ini. */
    fun perbaruiStatusHariIni() {
        viewModelScope.launch {
            val kondisi = hitungKondisi()
            _kondisiHariIni.value = kondisi
            _statusHariIni.value = kondisi.ringkasan
        }
    }

    /** Semua catatan absen hari ini milik pengguna yang sedang login. */
    protected suspend fun catatanHariIni(): List<ModelDatabase> =
        dao.byPolaTanggal("${Waktu.tanggalPendek()}%", userId)

    /** Kondisi absen hari ini yang dihitung langsung dari database. */
    protected suspend fun hitungKondisi(): KondisiAbsen = StatusAbsen.hitung(catatanHariIni())
}
