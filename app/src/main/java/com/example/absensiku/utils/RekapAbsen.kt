package com.example.absensiku.utils

import com.example.absensiku.model.ModelDatabase
import com.example.absensiku.model.RekapBulanan
import com.example.absensiku.model.RekapHarian
import java.time.LocalDate
import java.time.YearMonth

/**
 * Menghitung rekap absensi bulanan dari daftar catatan absen.
 */
object RekapAbsen {

    /** Catatan absen yang berada pada bulan yang dipilih. */
    fun catatanBulan(catatan: List<ModelDatabase>, bulan: YearMonth): List<ModelDatabase> {
        val kunci = Waktu.kunciBulan(bulan)
        return catatan.filter { Waktu.kunciBulanDariTersimpan(it.tanggal) == kunci }
    }

    /** Menyusun rekap satu bulan beserta rincian per hari. */
    fun hitung(catatan: List<ModelDatabase>, bulan: YearMonth): RekapBulanan {
        val perTanggal = catatanBulan(catatan, bulan).groupBy { it.tanggal.substringBefore(",") }

        val rincian = perTanggal.map { (tanggal, catatanHari) ->
            val masuk = catatanHari.firstOrNull { it.status == StatusAbsen.MASUK }
            val keluar = catatanHari.firstOrNull { it.status == StatusAbsen.KELUAR }
            val izin = catatanHari.firstOrNull { it.status == StatusAbsen.IZIN }
            RekapHarian(
                tanggal = tanggal,
                jamMasuk = masuk?.let { jam(it) }.orEmpty(),
                jamKeluar = keluar?.let { jam(it) }.orEmpty(),
                durasiKerja = durasi(masuk, keluar),
                ketepatan = masuk?.ketepatan.orEmpty(),
                keterlambatan = masuk?.keterlambatan ?: 0,
                keterangan = izin?.keterangan ?: catatanHari.firstOrNull { it.keterangan.isNotEmpty() }?.keterangan.orEmpty(),
                izin = izin != null
            )
        }.sortedBy { it.tanggal }

        val hadir = rincian.count { it.jamMasuk.isNotEmpty() }
        return RekapBulanan(
            bulan = Waktu.kunciBulan(bulan),
            hariBerjalan = hariBerjalan(bulan),
            hariHadir = hadir,
            tepatWaktu = rincian.count { it.ketepatan == StatusAbsen.TEPAT_WAKTU },
            terlambat = rincian.count { it.ketepatan == StatusAbsen.TERLAMBAT },
            totalKeterlambatan = rincian.sumOf { it.keterlambatan },
            hariIzin = rincian.count { it.izin },
            totalJamKerja = rincian.sumOf { it.durasiKerja },
            hariBelumPulang = rincian.count { it.jamMasuk.isNotEmpty() && it.jamKeluar.isEmpty() },
            rincian = rincian
        )
    }

    /** Jumlah hari yang sudah berjalan di bulan tersebut. */
    private fun hariBerjalan(bulan: YearMonth): Int {
        val sekarang = YearMonth.now()
        return when {
            bulan.isAfter(sekarang) -> 0
            bulan == sekarang -> LocalDate.now().dayOfMonth
            else -> bulan.lengthOfMonth()
        }
    }

    /** Selisih menit antara jam absen masuk dan jam absen pulang. */
    private fun durasi(masuk: ModelDatabase?, keluar: ModelDatabase?): Int {
        if (masuk == null || keluar == null) return 0
        return (menit(keluar) - menit(masuk)).coerceAtLeast(0)
    }

    private fun menit(catatan: ModelDatabase): Int {
        val jam = jam(catatan).split(":")
        if (jam.size < 2) return 0
        return (jam[0].toIntOrNull() ?: 0) * 60 + (jam[1].toIntOrNull() ?: 0)
    }

    private fun jam(catatan: ModelDatabase): String =
        catatan.tanggal.substringAfter(", ", catatan.tanggal)
}
