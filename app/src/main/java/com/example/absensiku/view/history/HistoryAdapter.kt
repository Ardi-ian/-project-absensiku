package com.example.absensiku.view.history

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.absensiku.databinding.ItemHistoryBinding
import com.example.absensiku.model.ModelDatabase
import com.example.absensiku.utils.StatusAbsen
import com.example.absensiku.utils.WorkRules

class HistoryAdapter(
    private val onLongClick: (ModelDatabase) -> Unit
) : ListAdapter<ModelDatabase, HistoryAdapter.VH>(Diff) {

    inner class VH(val b: ItemHistoryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.b) {
            tvNama.text = item.nama
            tvTanggal.text = item.tanggal
            tvLokasi.text = item.lokasi
            tvKeterangan.text = item.keterangan
            tvKeterangan.visibility = if (item.keterangan.isEmpty()) View.GONE else View.VISIBLE
            tvKetepatan.visibility = if (item.ketepatan.isEmpty()) View.GONE else View.VISIBLE
            tvKetepatan.text = if (item.ketepatan == StatusAbsen.TERLAMBAT && item.keterlambatan > 0)
                "${item.ketepatan} ${WorkRules.formatDurasi(item.keterlambatan)}" else item.ketepatan
            tvKetepatan.setTextColor(
                Color.parseColor(if (item.ketepatan == StatusAbsen.TERLAMBAT) "#C62828" else "#2E7D32")
            )
            tvStatus.text = item.status
            val warna = when (item.status) {
                StatusAbsen.MASUK -> "#2E7D32"
                StatusAbsen.KELUAR -> "#C62828"
                else -> "#EF6C00"
            }
            tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor(warna))
            Glide.with(ivFoto).load(item.fotoPath).centerCrop().into(ivFoto)
            root.setOnLongClickListener { onLongClick(item); true }
        }
    }

    private object Diff : DiffUtil.ItemCallback<ModelDatabase>() {
        override fun areItemsTheSame(a: ModelDatabase, b: ModelDatabase) = a.uid == b.uid
        override fun areContentsTheSame(a: ModelDatabase, b: ModelDatabase) = a == b
    }
}
