package com.example.absensiku.model

import com.example.absensiku.utils.WorkRules

/** Rekap absensi satu bulan. */
data class RekapBulanan(
    /** Nama bulan, contoh "Okt 2026". */
    val bulan: String = "",
    /** Jumlah hari yang sudah berjalan di bulan tersebut. */
    val hariBerjalan: Int = 0,
    /** Jumlah hari dengan absen masuk. */
    val hariHadir: Int = 0,
    /** Jumlah hari dengan status tepat waktu. */
    val tepatWaktu: Int = 0,
    /** Jumlah hari dengan status terlambat. */
    val terlambat: Int = 0,
    /** Akumulasi keterlambatan dalam menit. */
    val totalKeterlambatan: Int = 0,
    /** Jumlah hari dengan absen izin. */
    val hariIzin: Int = 0,
    /** Akumulasi jam kerja dalam menit. */
    val totalJamKerja: Int = 0,
    /** Jumlah hari masuk tanpa absen pulang. */
    val hariBelumPulang: Int = 0,
    /** Rincian per hari. */
    val rincian: List<RekapHarian> = emptyList()
) {
    /** Rata-rata jam kerja per hari hadir, dalam menit. */
    val rataRataJamKerja: Int
        get() = if (hariHadir > 0) totalJamKerja / hariHadir else 0

    /** Jumlah hari tanpa absen masuk maupun izin. */
    val hariTanpaAbsen: Int
        get() = (hariBerjalan - hariHadir - hariIzin).coerceAtLeast(0)

    val labelTotalJamKerja: String
        get() = WorkRules.formatDurasi(totalJamKerja)

    val labelRataRata: String
        get() = WorkRules.formatDurasi(rataRataJamKerja)

    val labelKeterlambatan: String
        get() = WorkRules.formatDurasi(totalKeterlambatan)
}

/** Rekap absensi satu hari. */
data class RekapHarian(
    /** Tanggal absen, contoh "21 Okt 2026". */
    val tanggal: String = "",
    /** Jam absen masuk, contoh "08:02". */
    val jamMasuk: String = "",
    /** Jam absen pulang, contoh "17:05". */
    val jamKeluar: String = "",
    /** Durasi kerja dalam menit. */
    val durasiKerja: Int = 0,
    /** Status ketepatan absen masuk. */
    val ketepatan: String = "",
    /** Keterlambatan dalam menit. */
    val keterlambatan: Int = 0,
    /** Alasan izin bila ada. */
    val keterangan: String = "",
    /** True bila hari tersebut berstatus izin. */
    val izin: Boolean = false
) {
    val labelDurasi: String
        get() = if (durasiKerja > 0) WorkRules.formatDurasi(durasiKerja) else "-"
}
