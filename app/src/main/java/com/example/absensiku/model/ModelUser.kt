package com.example.absensiku.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pengguna", indices = [Index(value = ["email"], unique = true)])
data class ModelUser(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "nama") val nama: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "salt") val salt: String,
    @ColumnInfo(name = "dibuat_pada") val dibuatPada: String
)
