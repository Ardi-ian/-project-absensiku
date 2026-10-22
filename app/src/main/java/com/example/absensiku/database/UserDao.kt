package com.example.absensiku.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.absensiku.model.ModelUser

@Dao
interface UserDao {
    @Query("SELECT * FROM pengguna WHERE email = :email LIMIT 1")
    suspend fun byEmail(email: String): ModelUser?

    @Query("SELECT EXISTS(SELECT 1 FROM pengguna WHERE email = :email)")
    suspend fun adaEmail(email: String): Boolean

    @Insert
    suspend fun insert(user: ModelUser): Long
}
