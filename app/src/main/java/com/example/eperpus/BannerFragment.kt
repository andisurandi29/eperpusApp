package com.example.eperpus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class BannerFragment : Fragment() {
    private var imageResId: Int = 0
    private var judul: String = ""
    private var deskripsibuku: String = ""

    companion object {
        fun newInstance(imageResId: Int, judul: String, dekripsi: String): BannerFragment {
            val fragment = BannerFragment()
            fragment.imageResId = imageResId
            fragment.judul = judul
            fragment.deskripsibuku = dekripsi
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_banner, container, false)
        val gambar = view.findViewById<ImageView>(R.id.image_buku)
        val judulbuku = view.findViewById<TextView>(R.id.judul_buku)
        val deskripsi = view.findViewById<TextView>(R.id.deskripsi)
        gambar.setImageResource(imageResId)
        judulbuku.text = judul
        deskripsi.text = deskripsibuku
        return view
    }


}