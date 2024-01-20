package com.example.eperpus.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eperpus.DetailBukuActivity
import com.example.eperpus.R
import com.example.eperpus.constants.Constants
import com.example.eperpus.model.Buku
import kotlinx.coroutines.withContext

class GridAdapter(private val context: Context?, private val items: MutableList<Buku>) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gambarCover: ImageView = itemView.findViewById(R.id.cover)
        val judulBuku: TextView = itemView.findViewById(R.id.judul)
        val listBuku: LinearLayout = itemView.findViewById(R.id.gridBuku)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_buku, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Menggunakan Glide untuk memuat gambar
        Glide.with(holder.itemView.context)
            .load("${Constants.BASE_URL}/static/uploads/" + currentItem.gambar)
            .into(holder.gambarCover)

        holder.judulBuku.text = currentItem.judul
        holder.listBuku.setOnClickListener {
            // Mengirim data buku ID ke DetailBukuActivity saat item diklik
            val intent = Intent(context, DetailBukuActivity::class.java)
            intent.putExtra("BUKU_ID", currentItem.id)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}