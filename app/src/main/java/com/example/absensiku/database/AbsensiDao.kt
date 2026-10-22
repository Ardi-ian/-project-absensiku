package com.example.absensiku.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.absensiku.model.ModelDatabase

@Dao
interface AbsensiDao {
    @Query("SELECT * FROM absensi WHERE user_id = :userId ORDER BY uid DESC")
    fun getAll(userId: Int): LiveData<List<ModelDatabase>>

    /** Catatan absen pada tanggal tertentu, contoh pola: "21 Okt 2026%". */
    @Query("SELECT * FROM absensi WHERE tanggal LIKE :pola AND user_id = :userId ORDER BY uid ASC")
    suspend fun byPolaTanggal(pola: String, userId: Int): List<ModelDatabase>

    /** Mengalihkan catatan lama (yang belum punya pemilik) ke seorang pengguna. */
    @Query("UPDATE absensi SET user_id = :userId WHERE user_id = 0")
    suspend fun adopsiCatatanLama(userId: Int)

    @Insert
    suspend fun insert(item: ModelDatabase)

    @Delete
    suspend fun delete(item: ModelDatabase)
}
