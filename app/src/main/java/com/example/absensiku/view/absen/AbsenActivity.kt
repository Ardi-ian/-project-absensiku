package com.example.absensiku.view.absen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.absensiku.R
import com.example.absensiku.databinding.ActivityAbsenBinding
import com.example.absensiku.model.ModelDatabase
import com.example.absensiku.utils.AturanAbsen
import com.example.absensiku.utils.KondisiAbsen
import com.example.absensiku.utils.SessionLogin
import com.example.absensiku.utils.StatusAbsen
import com.example.absensiku.utils.Waktu
import com.example.absensiku.utils.WorkRules
import com.example.absensiku.utils.jalankanTiapDetik
import com.example.absensiku.viewmodel.AbsenViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.Locale

class AbsenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbsenBinding
    private val viewModel: AbsenViewModel by viewModels()
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var status: String

    private var photoFile: File? = null
    private var hasPhoto = false
    private var lokasi: String? = null
    private var tanggalTampil = ""
    private var kondisi: KondisiAbsen = KondisiAbsen()

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            hasPhoto = true
            Glide.with(this).load(photoFile).centerCrop().into(binding.ivFoto)
        } else {
            photoFile?.delete()
        }
    }
    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else toast("Izin kamera diperlukan untuk mengambil foto")
    }
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) fetchLocation() else binding.tvLokasi.text = "Izin lokasi ditolak. Ketuk untuk coba lagi."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbsenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        status = intent.getStringExtra(EXTRA_STATUS) ?: StatusAbsen.MASUK
        fused = LocationServices.getFusedLocationProviderClient(this)

        binding.toolbar.title = "Absen $status"
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.etNama.setText(SessionLogin(this).nama)
        binding.tilKeterangan.visibility = if (status == StatusAbsen.IZIN) View.VISIBLE else View.GONE

        setTanggal(Waktu.tanggal())
        jalankanTiapDetik {
            binding.tvJam.text = Waktu.jam()
            val tanggal = Waktu.tanggal()
            if (tanggal != tanggalTampil) {
                setTanggal(tanggal)
                viewModel.perbaruiStatusHariIni()
            }
            perbaruiKetepatan()
        }
        viewModel.statusHariIni.observe(this) { binding.tvStatus.text = it }
        viewModel.kondisiHariIni.observe(this) { terapkanKondisi(it) }

        binding.ivFoto.setOnClickListener { ambilFoto() }
        binding.btnFoto.setOnClickListener { ambilFoto() }
        binding.tvLokasi.setOnClickListener { mintaLokasi() }
        binding.btnKirim.setOnClickListener { simpan() }

        mintaLokasi()
    }

    override fun onStart() {
        super.onStart()
        viewModel.perbaruiStatusHariIni()
    }

    private fun setTanggal(tanggal: String) {
        tanggalTampil = tanggal
        binding.tvTanggal.text = tanggal
    }

    /** Menyesuaikan tombol kirim & keterangan aturan sesuai kondisi absen hari ini. */
    private fun terapkanKondisi(kondisi: KondisiAbsen) {
        this.kondisi = kondisi
        val pesan = AturanAbsen.pesanTolak(status, kondisi)
        binding.btnKirim.isEnabled = pesan == null
        binding.tvAturan.text = pesan ?: catatanAturan()
        binding.tvAturan.setTextColor(
            ContextCompat.getColor(this, if (pesan == null) R.color.text_secondary else R.color.merah)
        )
    }

    /** Keterangan aturan yang sedang berlaku, contoh: batas absen masuk & status ketepatan. */
    private fun catatanAturan(): String = when (status) {
        StatusAbsen.MASUK ->
            "Batas absen masuk ${WorkRules.JAM_MASUK} · toleransi ${WorkRules.TOLERANSI_MENIT} menit · ${WorkRules.labelKetepatan()}"
        StatusAbsen.KELUAR -> "Jam kerja ${WorkRules.JAM_MASUK} - ${WorkRules.JAM_PULANG}"
        else -> "Tuliskan alasan izin lalu kirim"
    }

    /** Memperbarui status ketepatan absen masuk setiap detik. */
    private fun perbaruiKetepatan() {
        if (status != StatusAbsen.MASUK) return
        if (AturanAbsen.pesanTolak(status, kondisi) != null) return
        binding.tvAturan.text = catatanAturan()
    }

    private fun ambilFoto() {
        if (granted(Manifest.permission.CAMERA)) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun launchCamera() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(dir, "absen_${System.currentTimeMillis()}.jpg").also { photoFile = it }
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        takePicture.launch(uri)
    }

    private fun mintaLokasi() {
        if (granted(Manifest.permission.ACCESS_FINE_LOCATION) || granted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fetchLocation()
        } else {
            locationPermission.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        binding.tvLokasi.text = "Mencari lokasi…"
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    binding.tvLokasi.text = "Lokasi tidak ditemukan. Aktifkan GPS lalu ketuk untuk coba lagi."
                    return@addOnSuccessListener
                }
                lifecycleScope.launch {
                    val alamat = withContext(Dispatchers.IO) { resolveAddress(loc.latitude, loc.longitude) }
                    lokasi = alamat
                    binding.tvLokasi.text = alamat
                }
            }
            .addOnFailureListener { binding.tvLokasi.text = "Gagal mengambil lokasi. Ketuk untuk coba lagi." }
    }

    @Suppress("DEPRECATION")
    private fun resolveAddress(lat: Double, lng: Double): String = try {
        Geocoder(this, Locale("id", "ID")).getFromLocation(lat, lng, 1)
            ?.firstOrNull()?.getAddressLine(0) ?: koordinat(lat, lng)
    } catch (e: Exception) {
        koordinat(lat, lng)
    }

    private fun koordinat(lat: Double, lng: Double) = String.format(Locale.US, "%.5f, %.5f", lat, lng)

    private fun simpan() {
        val nama = binding.etNama.text.toString().trim()
        val ket = binding.etKeterangan.text.toString().trim()
        binding.tilNama.error = null
        binding.tilKeterangan.error = null
        when {
            nama.isEmpty() -> binding.tilNama.error = "Nama wajib diisi"
            !hasPhoto -> toast("Ambil foto terlebih dahulu")
            lokasi == null -> toast("Lokasi belum tersedia")
            status == StatusAbsen.IZIN && ket.isEmpty() -> binding.tilKeterangan.error = "Alasan izin wajib diisi"
            else -> {
                binding.btnKirim.isEnabled = false
                lifecycleScope.launch {
                    // Periksa ulang aturan harian langsung dari database sebelum menyimpan.
                    val pesan = viewModel.pesanTolak(status)
                    if (pesan != null) {
                        toast(pesan)
                        binding.btnKirim.isEnabled = true
                        viewModel.perbaruiStatusHariIni()
                        return@launch
                    }
                    val now = Date()
                    val item = ModelDatabase(
                        nama = nama,
                        fotoPath = photoFile!!.absolutePath,
                        tanggal = Waktu.tanggalJam(now),
                        status = status,
                        lokasi = lokasi!!,
                        keterangan = ket,
                        ketepatan = if (status == StatusAbsen.MASUK) WorkRules.ketepatan(now) else "",
                        keterlambatan = if (status == StatusAbsen.MASUK) WorkRules.keterlambatan(now) else 0
                    )
                    viewModel.simpan(item)
                    toast("Absen $status berhasil disimpan")
                    finish()
                }
            }
        }
    }

    private fun granted(p: String) = ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_STATUS = "extra_status"
    }
}
