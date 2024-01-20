package com.example.eperpus.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.eperpus.R
import com.example.eperpus.constants.Constants
import com.example.eperpus.model.Buku

class SlideAdapter(private val items: MutableList<Buku>) : RecyclerView.Adapter<SlideAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_buku)
        private val judulBuku: TextView = itemView.findViewById(R.id.judul_buku)
        private val deskripsiBuku: TextView = itemView.findViewById(R.id.deskripsi)
        private val kategori: TextView = itemView.findViewById(R.id.kategori)

        fun bind(item: Buku) {
            Glide.with(itemView.context)
                .load("${Constants.BASE_URL}/static/uploads/" + item.gambar)
                .into(imageView)

            judulBuku.text = item.judul
            kategori.text = item.kategori
            // Batasi deskripsi menjadi 50 karakter
            val limitedDescription = if (item.deskripsi.length > 70) {
                "${item.deskripsi.substring(0, 70)}..."
            } else {
                item.deskripsi
            }

            deskripsiBuku.text = limitedDescription
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_slide, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }
}