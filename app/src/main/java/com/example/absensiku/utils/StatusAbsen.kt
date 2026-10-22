package com.example.absensiku.utils

import com.example.absensiku.model.ModelDatabase

/** Status absen dan penyusunan ringkasan absen hari ini. */
object StatusAbsen {
    const val MASUK = "Masuk"
    const val KELUAR = "Keluar"
    const val IZIN = "Izin"

    const val BELUM_MASUK = "Belum absen masuk"
    const val TEPAT_WAKTU = "Tepat Waktu"
    const val TERLAMBAT = "Terlambat"

    /** Menyusun kondisi absen hari ini dari daftar catatan absen hari ini. */
    fun hitung(catatan: List<ModelDatabase>): KondisiAbsen {
        val masuk = catatan.filter { it.status == MASUK }.lastOrNull()
        val keluar = catatan.filter { it.status == KELUAR }.lastOrNull()
        return KondisiAbsen(
            sudahMasuk = masuk != null,
            jamMasuk = masuk?.let { jam(it) }.orEmpty(),
            ketepatan = masuk?.ketepatan.orEmpty(),
            keterlambatan = masuk?.keterlambatan ?: 0,
            sudahKeluar = keluar != null,
            jamKeluar = keluar?.let { jam(it) }.orEmpty()
        )
    }

    /** Mengambil bagian jam (HH:mm) dari tanggal tersimpan. */
    private fun jam(catatan: ModelDatabase): String =
        catatan.tanggal.substringAfter(", ", catatan.tanggal)
}

/** Kondisi absen hari ini, dipakai untuk menyesuaikan tampilan menu. */
data class KondisiAbsen(
    val sudahMasuk: Boolean = false,
    val jamMasuk: String = "",
    val ketepatan: String = "",
    val keterlambatan: Int = 0,
    val sudahKeluar: Boolean = false,
    val jamKeluar: String = ""
) {
    /** Label keterlambatan, contoh: " (Terlambat 1 jam 5 menit)". */
    val labelKeterlambatan: String
        get() = if (ketepatan == StatusAbsen.TERLAMBAT && keterlambatan > 0)
            " (${StatusAbsen.TERLAMBAT} ${WorkRules.formatDurasi(keterlambatan)})" else ""

    /** Ringkasan singkat untuk header layar utama. */
    val ringkasan: String
        get() = when {
            !sudahMasuk -> StatusAbsen.BELUM_MASUK
            !sudahKeluar -> "Sudah masuk pukul $jamMasuk$labelKeterlambatan"
            else -> "Sudah pulang pukul $jamKeluar"
        }
}
