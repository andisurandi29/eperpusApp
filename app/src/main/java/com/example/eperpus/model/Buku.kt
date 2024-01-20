package com.example.eperpus.model

data class Buku(
    val judul: String,
    val deskripsi: String,
    val gambar: String,
    val id: Int,
    val kategori: String,
    val kategoriId: Int,
    val kodeBuku: String,
    val penerbit: String,
    val penulis: String,
    val stok: Int,
    val tahun: Int
)

data class Kategori(
    val id: Int,
    val namaKategori: String
)

data class BannerItem(
    val id: Int,
    val judul: String,
    val imageUrl: String,
    val description: String
)




