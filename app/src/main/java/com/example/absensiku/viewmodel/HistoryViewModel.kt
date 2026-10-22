package com.example.absensiku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.absensiku.database.AppDatabase
import com.example.absensiku.model.ModelDatabase
import com.example.absensiku.model.RekapBulanan
import com.example.absensiku.utils.RekapAbsen
import com.example.absensiku.utils.SessionLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.YearMonth

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).absensiDao()
    private val userId = SessionLogin(app).userId
    val data = dao.getAll(userId)

    /** Bulan yang sedang dipilih pada rekap. */
    private val _bulan = MutableLiveData(YearMonth.now())
    val bulan: LiveData<YearMonth> = _bulan

    /** Rekap absensi bulan yang dipilih. */
    private val _rekap = MediatorLiveData<RekapBulanan>()
    val rekap: LiveData<RekapBulanan> = _rekap

    /** Catatan absen milik bulan yang dipilih. */
    private val _catatanBulan = MediatorLiveData<List<ModelDatabase>>()
    val catatanBulan: LiveData<List<ModelDatabase>> = _catatanBulan

    init {
        _rekap.addSource(data) { hitung() }
        _rekap.addSource(_bulan) { hitung() }
    }

    /** Menggeser pilihan bulan mundur (-) atau maju (+). */
    fun geserBulan(delta: Long) {
        _bulan.value = (_bulan.value ?: YearMonth.now()).plusMonths(delta)
    }

    /** Menampilkan kembali bulan berjalan. */
    fun bulanSekarang() {
        _bulan.value = YearMonth.now()
    }

    private fun hitung() {
        val bulan = _bulan.value ?: return
        val semua = data.value ?: emptyList()
        _rekap.value = RekapAbsen.hitung(semua, bulan)
        _catatanBulan.value = RekapAbsen.catatanBulan(semua, bulan)
    }

    fun hapus(item: ModelDatabase) = viewModelScope.launch {
        dao.delete(item)
        launch(Dispatchers.IO) { File(item.fotoPath).delete() }
    }
}
