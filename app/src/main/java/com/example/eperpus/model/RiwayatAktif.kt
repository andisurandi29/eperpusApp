package com.example.eperpus.model

data class RiwayatAktif(
    val judulBuku: String,
    val kategori: String,
    val idPeminjaman: Int,
    val kodePeminjaman: String,
    val gambarUrl: String,
    val status : String
)
