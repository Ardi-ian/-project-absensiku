package com.example.absensiku.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/** Format tanggal dan jam berbahasa Indonesia. */
object Waktu {
    val locale: Locale = Locale("id", "ID")

    /** "Rabu, 21 Oktober 2026" */
    fun tanggal(now: Date = Date()): String =
        SimpleDateFormat("EEEE, dd MMMM yyyy", locale).format(now)

    /** "15:42:07" */
    fun jam(now: Date = Date()): String =
        SimpleDateFormat("HH:mm:ss", locale).format(now)

    /** "21 Okt 2026" - dipakai untuk pencocokan tanggal yang tersimpan. */
    fun tanggalPendek(now: Date = Date()): String =
        SimpleDateFormat("dd MMM yyyy", locale).format(now)

    /** "15:42" */
    fun jamPendek(now: Date = Date()): String =
        SimpleDateFormat("HH:mm", locale).format(now)

    /** Tanggal lengkap beserta jam, otomatis mengikuti waktu saat ini. */
    fun tanggalJam(now: Date = Date()): String = "${tanggalPendek(now)}, ${jamPendek(now)}"

    /** "Okt 2026" - kunci bulan, dipakai untuk mencocokkan periode rekap. */
    fun bulanTahun(now: Date = Date()): String = formatBulan.format(now)

    /** "Okt 2026" - kunci bulan dari bulan yang dipilih. */
    fun kunciBulan(bulan: YearMonth): String = formatBulan.format(tanggalBulan(bulan))

    /** Nama bulan lengkap, contoh "Oktober 2026". */
    fun namaBulan(bulan: YearMonth): String =
        SimpleDateFormat("MMMM yyyy", locale).format(tanggalBulan(bulan))

    /** "Okt 2026" -> "Oktober 2026". */
    fun namaBulanDariKunci(kunci: String): String = try {
        val tanggal = SimpleDateFormat("MMM yyyy", locale).parse(kunci)
        if (tanggal == null) kunci else SimpleDateFormat("MMMM yyyy", locale).format(tanggal)
    } catch (e: Exception) {
        kunci
    }

    /** Kunci bulan dari tanggal yang tersimpan, contoh "21 Okt 2026, 16:20" -> "Okt 2026". */
    fun kunciBulanDariTersimpan(tanggal: String): String {
        val bagian = tanggal.substringBefore(",").trim()
        return if (bagian.length > 3) bagian.substring(3) else bagian
    }

    private fun tanggalBulan(bulan: YearMonth): Date =
        Date.from(bulan.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant())

    private val formatBulan = SimpleDateFormat("MMM yyyy", locale)
}

/**
 * Menjalankan [tiapDetik] setiap detik selama layar terlihat saja.
 * Blok otomatis dibatalkan saat activity tidak aktif sehingga tidak ada kebocoran.
 */
fun LifecycleOwner.jalankanTiapDetik(tiapDetik: () -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                tiapDetik()
                delay(1000)
            }
        }
    }
}
