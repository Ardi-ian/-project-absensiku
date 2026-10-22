package com.example.absensiku.utils

import com.example.absensiku.model.RekapBulanan

/**
 * Menyusun laporan absensi dalam bentuk teks (untuk dibagikan) dan CSV (untuk Excel).
 */
object LaporanAbsen {

    /** Laporan ringkas siap kirim ke WhatsApp/email. */
    fun teks(rekap: RekapBulanan, nama: String): String {
        val isi = StringBuilder()
        isi.append("LAPORAN ABSENSI\n")
        isi.append("Nama    : $nama\n")
        isi.append("Periode : ${Waktu.namaBulanDariKunci(rekap.bulan)}\n")
        isi.append("Hari hadir       : ${rekap.hariHadir} hari\n")
        isi.append("Tepat waktu      : ${rekap.tepatWaktu} hari\n")
        isi.append("Terlambat        : ${rekap.terlambat} hari (total ${rekap.labelKeterlambatan})\n")
        isi.append("Izin             : ${rekap.hariIzin} hari\n")
        isi.append("Total jam kerja  : ${rekap.labelTotalJamKerja}\n")
        isi.append("Rata-rata/hari   : ${rekap.labelRataRata}\n")
        isi.append("Tanpa absen      : ${rekap.hariTanpaAbsen} hari\n")
        if (rekap.hariBelumPulang > 0) {
            isi.append("Belum absen pulang: ${rekap.hariBelumPulang} hari\n")
        }
        isi.append("\nRincian harian:\n")
        if (rekap.rincian.isEmpty()) {
            isi.append("-")
        } else {
            rekap.rincian.forEach { hari ->
                isi.append("${hari.tanggal} | Masuk ${hari.jamMasuk.ifEmpty { "-" }}")
                isi.append(" | Keluar ${hari.jamKeluar.ifEmpty { "-" }}")
                isi.append(" | ${hari.labelDurasi}")
                if (hari.ketepatan.isNotEmpty()) {
                    isi.append(" | ${hari.ketepatan}")
                    if (hari.keterlambatan > 0) {
                        isi.append(" ${WorkRules.formatDurasi(hari.keterlambatan)}")
                    }
                }
                if (hari.izin) {
                    isi.append(" | Izin")
                }
                if (hari.keterangan.isNotEmpty()) {
                    isi.append(" | ${hari.keterangan}")
                }
                isi.append("\n")
            }
        }
        return isi.toString()
    }

    /** Isi file CSV (dipisah titik koma) agar rapi dibuka di Excel. */
    fun csv(rekap: RekapBulanan): String {
        val isi = StringBuilder()
        isi.append("﻿")
        isi.append("Tanggal;Masuk;Keluar;Durasi;Ketepatan;Keterlambatan (menit);Keterangan\n")
        rekap.rincian.forEach { hari ->
            isi.append(kolom(hari.tanggal)).append(';')
            isi.append(kolom(hari.jamMasuk)).append(';')
            isi.append(kolom(hari.jamKeluar)).append(';')
            isi.append(kolom(hari.labelDurasi)).append(';')
            isi.append(kolom(hari.ketepatan)).append(';')
            isi.append(hari.keterlambatan).append(';')
            isi.append(kolom(hari.keterangan)).append('\n')
        }
        return isi.toString()
    }

    /** Nama file laporan, contoh "laporan-absensi-Okt-2026.csv". */
    fun namaFile(bulan: String, ekstensi: String): String =
        "laporan-absensi-${bulan.replace(' ', '-')}.$ekstensi"

    private fun kolom(nilai: String): String =
        "\"" + nilai.replace("\"", "\"\"").replace("\n", " ") + "\""
}
