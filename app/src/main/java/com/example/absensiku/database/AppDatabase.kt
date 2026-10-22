package com.example.absensiku.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.absensiku.model.ModelDatabase
import com.example.absensiku.model.ModelUser

@Database(entities = [ModelDatabase::class, ModelUser::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun absensiDao(): AbsensiDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        /** Menambahkan tabel pengguna tanpa menghapus data absensi yang sudah ada. */
        val MIGRASI_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS pengguna (
                        uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nama TEXT NOT NULL,
                        email TEXT NOT NULL,
                        password_hash TEXT NOT NULL,
                        salt TEXT NOT NULL,
                        dibuat_pada TEXT NOT NULL
                    )"""
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_pengguna_email ON pengguna (email)"
                )
            }
        }

        /** Menambahkan kolom ketepatan & keterlambatan tanpa menghapus data yang sudah ada. */
        val MIGRASI_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE absensi ADD COLUMN ketepatan TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE absensi ADD COLUMN keterlambatan INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Menambahkan kolom user_id agar aturan absen berlaku per akun. */
        val MIGRASI_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE absensi ADD COLUMN user_id INTEGER NOT NULL DEFAULT 0")
                // Catatan lama (sebelum ada akun) dimiliki oleh akun pertama yang terdaftar.
                db.execSQL(
                    "UPDATE absensi SET user_id = COALESCE((SELECT uid FROM pengguna ORDER BY uid ASC LIMIT 1), 0) WHERE user_id = 0"
                )
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "absensi.db"
                ).addMigrations(MIGRASI_1_2, MIGRASI_2_3, MIGRASI_3_4).build().also { instance = it }
            }
    }
}
