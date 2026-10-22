package com.example.absensiku.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "absensi")
data class ModelDatabase(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "nama") val nama: String,
    @ColumnInfo(name = "foto_path") val fotoPath: String,
    @ColumnInfo(name = "tanggal") val tanggal: String,
    @ColumnInfo(name = "status") val status: String,      // Masuk / Keluar / Izin
    @ColumnInfo(name = "lokasi") val lokasi: String,
    @ColumnInfo(name = "keterangan") val keterangan: String,
    @ColumnInfo(name = "ketepatan", defaultValue = "''") val ketepatan: String = "",
    @ColumnInfo(name = "keterlambatan", defaultValue = "0") val keterlambatan: Int = 0,
    @ColumnInfo(name = "user_id", defaultValue = "0") val userId: Int = 0
)
