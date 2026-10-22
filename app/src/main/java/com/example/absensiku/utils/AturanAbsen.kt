package com.example.absensiku.utils

/**
 * Aturan absen harian:
 * maksimal satu absen masuk dan satu absen keluar per hari,
 * absen keluar hanya boleh dilakukan setelah absen masuk.
 */
object AturanAbsen {
    /** Pesan penolakan bila absen tidak boleh dilakukan, null bila boleh. */
    fun pesanTolak(status: String, kondisi: KondisiAbsen): String? = when (status) {
        StatusAbsen.MASUK ->
            if (kondisi.sudahMasuk) "Absen masuk hari ini sudah dilakukan pukul ${kondisi.jamMasuk}"
            else null
        StatusAbsen.KELUAR -> when {
            !kondisi.sudahMasuk -> "Absen keluar hanya bisa dilakukan setelah absen masuk"
            kondisi.sudahKeluar -> "Absen keluar hari ini sudah dilakukan pukul ${kondisi.jamKeluar}"
            else -> null
        }
        else -> null
    }

    /** Keterangan singkat yang ditampilkan pada kartu menu. */
    fun keteranganKartu(status: String, kondisi: KondisiAbsen): String = when (status) {
        StatusAbsen.MASUK -> if (kondisi.sudahMasuk)
            "Sudah absen masuk pukul ${kondisi.jamMasuk}${kondisi.labelKeterlambatan}"
        else "Belum absen masuk hari ini"
        StatusAbsen.KELUAR -> when {
            kondisi.sudahKeluar -> "Sudah absen pulang pukul ${kondisi.jamKeluar}"
            !kondisi.sudahMasuk -> "Absen masuk dulu"
            else -> "Boleh absen pulang"
        }
        else -> ""
    }
}
