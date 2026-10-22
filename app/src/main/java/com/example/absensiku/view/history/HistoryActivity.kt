package com.example.absensiku.view.history

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.absensiku.databinding.ActivityHistoryBinding
import com.example.absensiku.model.RekapBulanan
import com.example.absensiku.utils.LaporanAbsen
import com.example.absensiku.utils.SessionLogin
import com.example.absensiku.utils.Waktu
import com.example.absensiku.viewmodel.HistoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()

    private val simpanCsv = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) simpanLaporan(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = HistoryAdapter { item ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Hapus data?")
                .setMessage("Absen ${item.status} pada ${item.tanggal} akan dihapus.")
                .setPositiveButton("Hapus") { _, _ -> viewModel.hapus(item) }
                .setNegativeButton("Batal", null)
                .show()
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        viewModel.bulan.observe(this) { bulan ->
            binding.tvBulan.text = Waktu.namaBulan(bulan)
            binding.btnBerikutnya.isEnabled = bulan.isBefore(YearMonth.now())
        }
        viewModel.rekap.observe(this) { terapkanRekap(it) }
        viewModel.catatanBulan.observe(this) { list ->
            adapter.submitList(list)
            binding.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            val bulan = viewModel.bulan.value?.let { Waktu.namaBulan(it) } ?: ""
            binding.tvKosong.text = "Belum ada data absen pada $bulan"
        }

        binding.btnSebelumnya.setOnClickListener { viewModel.geserBulan(-1) }
        binding.btnBerikutnya.setOnClickListener { viewModel.geserBulan(1) }
        binding.btnEkspor.setOnClickListener { pilihEkspor() }
    }

    /** Menampilkan angka rekap pada kartu rekap. */
    private fun terapkanRekap(rekap: RekapBulanan) {
        binding.statHadir.tvNilai.text = rekap.hariHadir.toString()
        binding.statHadir.tvLabel.text = "Hari hadir"
        binding.statTepat.tvNilai.text = rekap.tepatWaktu.toString()
        binding.statTepat.tvLabel.text = "Tepat waktu"
        binding.statTerlambat.tvNilai.text = rekap.terlambat.toString()
        binding.statTerlambat.tvLabel.text = "Terlambat"
        binding.statIzin.tvNilai.text = rekap.hariIzin.toString()
        binding.statIzin.tvLabel.text = "Izin"
        binding.statTotalJam.tvNilai.text = rekap.labelTotalJamKerja
        binding.statTotalJam.tvLabel.text = "Total jam kerja"
        binding.statRataRata.tvNilai.text = rekap.labelRataRata
        binding.statRataRata.tvLabel.text = "Rata-rata/hari"

        val catatan = buildList {
            add("Total keterlambatan: ${rekap.labelKeterlambatan}")
            add("Hari tanpa absen: ${rekap.hariTanpaAbsen}")
            if (rekap.hariBelumPulang > 0) add("Belum absen pulang: ${rekap.hariBelumPulang} hari")
        }
        binding.tvCatatan.text = catatan.joinToString(" · ")
    }

    /** Pilihan cara mengekspor laporan. */
    private fun pilihEkspor() {
        val rekap = viewModel.rekap.value ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Ekspor Laporan ${Waktu.namaBulanDariKunci(rekap.bulan)}")
            .setItems(arrayOf("Bagikan ringkasan (teks)", "Simpan sebagai CSV (Excel)")) { _, which ->
                when (which) {
                    0 -> bagikan()
                    1 -> simpanCsv.launch(LaporanAbsen.namaFile(rekap.bulan, "csv"))
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /** Membagikan laporan lewat WhatsApp, email, dan lainnya. */
    private fun bagikan() {
        val rekap = viewModel.rekap.value ?: return
        val kirim = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Absensi ${Waktu.namaBulanDariKunci(rekap.bulan)}")
            putExtra(Intent.EXTRA_TEXT, LaporanAbsen.teks(rekap, SessionLogin(this@HistoryActivity).nama))
        }
        startActivity(Intent.createChooser(kirim, "Bagikan laporan"))
    }

    /** Menulis laporan CSV ke berkas yang dipilih pengguna. */
    private fun simpanLaporan(uri: Uri) {
        val rekap = viewModel.rekap.value ?: return
        val isi = LaporanAbsen.csv(rekap)
        lifecycleScope.launch(Dispatchers.IO) {
            val berhasil = try {
                contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(isi.toByteArray(Charsets.UTF_8))
                } != null
            } catch (e: Exception) {
                false
            }
            withContext(Dispatchers.Main) {
                toast(if (berhasil) "Laporan tersimpan" else "Gagal menyimpan laporan")
            }
        }
    }

    private fun toast(pesan: String) = Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
}
