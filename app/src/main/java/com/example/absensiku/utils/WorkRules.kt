package com.example.absensiku.utils

import java.util.Calendar
import java.util.Date

/**
 * Aturan jam kerja absensi.
 * Ubah nilai di sini saja agar berlaku ke seluruh aplikasi.
 */
object WorkRules {
    /** Jam mulai kerja, format "HH:mm". */
    const val JAM_MASUK = "08:00"

    /** Toleransi keterlambatan (menit). Absen masuk pada jam ini masih dianggap tepat waktu. */
    const val TOLERANSI_MENIT = 15

    /** Jam akhir kerja, format "HH:mm". */
    const val JAM_PULANG = "17:00"

    /** Jam masuk dalam bentuk menit sejak tengah malam. */
    private val jamMasukMenit: Int = JAM_MASUK.toMenit()

    /** Ringkasan aturan jam kerja untuk ditampilkan di layar utama. */
    val ringkasan: String
        get() = "Jam kerja $JAM_MASUK - $JAM_PULANG · toleransi $TOLERANSI_MENIT menit"

    /**
     * Menit keterlambatan absen masuk.
     * 0 berarti tidak terlambat (tepat waktu atau masih sebelum jam masuk).
     */
    fun keterlambatan(now: Date = Date()): Int {
        val kalender = Calendar.getInstance().apply { time = now }
        val menit = kalender.get(Calendar.HOUR_OF_DAY) * 60 + kalender.get(Calendar.MINUTE)
        return (menit - jamMasukMenit).coerceAtLeast(0)
    }

    /** Status ketepatan absen masuk: "Tepat Waktu" atau "Terlambat". */
    fun ketepatan(now: Date = Date()): String =
        if (keterlambatan(now) > TOLERANSI_MENIT) StatusAbsen.TERLAMBAT else StatusAbsen.TEPAT_WAKTU

    /** Label ketepatan lengkap, contoh: "Tepat Waktu" atau "Terlambat 1 jam 5 menit". */
    fun labelKetepatan(now: Date = Date()): String {
        val menit = keterlambatan(now)
        return if (menit > TOLERANSI_MENIT)
            "${StatusAbsen.TERLAMBAT} ${formatDurasi(menit)}" else StatusAbsen.TEPAT_WAKTU
    }

    /** Mengubah durasi menit menjadi bahasa manusia, contoh: "1 jam 5 menit". */
    fun formatDurasi(menit: Int): String {
        val jam = menit / 60
        val sisa = menit % 60
        return when {
            jam > 0 && sisa > 0 -> "$jam jam $sisa menit"
            jam > 0 -> "$jam jam"
            else -> "$sisa menit"
        }
    }

    private fun String.toMenit(): Int {
        val bagian = split(":")
        return bagian[0].toInt() * 60 + bagian[1].toInt()
    }
}
