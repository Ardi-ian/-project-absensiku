package com.example.absensiku.view.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.absensiku.databinding.ActivityMainBinding
import com.example.absensiku.utils.AturanAbsen
import com.example.absensiku.utils.KondisiAbsen
import com.example.absensiku.utils.SessionLogin
import com.example.absensiku.utils.StatusAbsen
import com.example.absensiku.utils.Waktu
import com.example.absensiku.utils.WorkRules
import com.example.absensiku.utils.jalankanTiapDetik
import com.example.absensiku.view.absen.AbsenActivity
import com.example.absensiku.view.history.HistoryActivity
import com.example.absensiku.view.login.LoginActivity
import com.example.absensiku.viewmodel.MainViewModel
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionLogin
    private val viewModel: MainViewModel by viewModels()

    private var tanggalTampil = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionLogin(this)

        binding.tvHalo.text = "Halo, ${session.nama}!"
        setTanggal(Waktu.tanggal())

        jalankanTiapDetik {
            binding.tvJam.text = Waktu.jam()
            val tanggal = Waktu.tanggal()
            if (tanggal != tanggalTampil) {
                setTanggal(tanggal)
                viewModel.perbaruiStatusHariIni()
            }
        }

        viewModel.statusHariIni.observe(this) { binding.tvStatus.text = it }
        viewModel.kondisiHariIni.observe(this) { terapkanKondisi(it) }

        binding.cardMasuk.setOnClickListener { buka(StatusAbsen.MASUK) }
        binding.cardKeluar.setOnClickListener { buka(StatusAbsen.KELUAR) }
        binding.cardIzin.setOnClickListener { buka(StatusAbsen.IZIN) }
        binding.cardRiwayat.setOnClickListener { startActivity(Intent(this, HistoryActivity::class.java)) }
        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.perbaruiStatusHariIni()
    }

    private fun setTanggal(tanggal: String) {
        tanggalTampil = tanggal
        binding.tvTanggal.text = tanggal
    }

    /** Menyesuaikan tampilan kartu Masuk/Keluar dengan kondisi absen hari ini. */
    private fun terapkanKondisi(kondisi: KondisiAbsen) {
        binding.tvJamKerja.text = WorkRules.ringkasan
        terapkanKartu(
            binding.cardMasuk, binding.tvKetMasuk,
            AturanAbsen.keteranganKartu(StatusAbsen.MASUK, kondisi),
            aktif = !kondisi.sudahMasuk
        )
        terapkanKartu(
            binding.cardKeluar, binding.tvKetKeluar,
            AturanAbsen.keteranganKartu(StatusAbsen.KELUAR, kondisi),
            aktif = kondisi.sudahMasuk && !kondisi.sudahKeluar
        )
    }

    private fun terapkanKartu(card: MaterialCardView, label: TextView, keterangan: String, aktif: Boolean) {
        label.text = keterangan
        card.isEnabled = aktif
        card.isClickable = aktif
        card.isFocusable = aktif
        card.alpha = if (aktif) 1f else 0.5f
    }

    private fun buka(status: String) {
        val kondisi = viewModel.kondisiHariIni.value ?: KondisiAbsen()
        val pesan = AturanAbsen.pesanTolak(status, kondisi)
        if (pesan != null) {
            toast(pesan)
            return
        }
        startActivity(Intent(this, AbsenActivity::class.java).putExtra(AbsenActivity.EXTRA_STATUS, status))
    }

    private fun toast(pesan: String) = Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
}
