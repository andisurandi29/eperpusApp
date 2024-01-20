package com.example.eperpus.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eperpus.DetailBukuActivity
import com.example.eperpus.DetailRiwayatActivity
import com.example.eperpus.R
import com.example.eperpus.constants.Constants
import com.example.eperpus.model.RiwayatAktif

class RiwayatAktifAdapter(private val context: Context, private val riwayatList: MutableList<RiwayatAktif>) :
    RecyclerView.Adapter<RiwayatAktifAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_riwayat, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val riwayat = riwayatList[position]

        // Bind data to the views in the ViewHolder
        holder.judulBuku.text = riwayat.judulBuku
        holder.kategori.text = "Kategori : ${riwayat.kategori}"
        holder.idPeminjaman.text = "Kode : ${riwayat.kodePeminjaman}"
        holder.status.text = "Status : ${riwayat.status}"

        // Jika Anda menggunakan gambar, Anda dapat menggunakan library seperti Glide
        // Contoh menggunakan placeholder gambar hitam jika gambar tidak tersedia
        Glide.with(context).load("${Constants.BASE_URL}/static/uploads/${riwayat.gambarUrl}").placeholder(R.color.black).into(holder.imageBuku)

        holder.btnDetail.setOnClickListener {
            val intent = Intent(context, DetailRiwayatActivity::class.java)
            intent.putExtra("RIWAYAT_ID", riwayat.idPeminjaman)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return riwayatList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judulBuku: TextView = itemView.findViewById(R.id.judul_buku)
        val kategori: TextView = itemView.findViewById(R.id.kategori)
        val idPeminjaman: TextView = itemView.findViewById(R.id.id_peminjaman)
        val imageBuku: ImageView = itemView.findViewById(R.id.image_buku)
        val btnDetail: Button = itemView.findViewById(R.id.btnDetail)
        val status: TextView = itemView.findViewById(R.id.status)
    }
}