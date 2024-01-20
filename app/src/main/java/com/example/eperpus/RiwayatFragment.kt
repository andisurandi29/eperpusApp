package com.example.eperpus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class RiwayatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_riwayat, container, false)

        val riwayatAktif = view.findViewById<LinearLayout>(R.id.riwayataktif)
        val riwayatSelesai = view.findViewById<LinearLayout>(R.id.riwayatselesai)

        val indicatorRiwayatAktif = view.findViewById<View>(R.id.indicator_aktif)
        val indicatorRiwayatSelesai = view.findViewById<View>(R.id.indicator_selesai)

        defaultFragment(indicatorRiwayatSelesai, indicatorRiwayatAktif)

        riwayatAktif.setOnClickListener {
            val fragmentRiwayatAktif = RiwayatAktifFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentRiwayatAktif)
                .commit()
            indicatorRiwayatSelesai.setBackgroundResource(R.color.primary) // Ganti dengan primary color yang sesuai
            indicatorRiwayatAktif.setBackgroundResource(R.color.oren) // Ganti dengan warna yang sesuai
        }

        riwayatSelesai.setOnClickListener {
            val fragmentRiwayatSelesai = RiwayatSelesaiFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragmentRiwayatSelesai)
                .commit()
            indicatorRiwayatAktif.setBackgroundResource(R.color.primary) // Ganti dengan primary color yang sesuai
            indicatorRiwayatSelesai.setBackgroundResource(R.color.oren) // Ganti dengan warna yang sesuai
        }

        return view
    }

    private fun defaultFragment(indicatorRiwayatSelesai: View, indicatorRiwayatAktif: View) {
        val fragmentRiwayatAktif = RiwayatAktifFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragmentRiwayatAktif)
            .commit()
        indicatorRiwayatSelesai.setBackgroundResource(R.color.primary) // Ganti dengan primary color yang sesuai
        indicatorRiwayatAktif.setBackgroundResource(R.color.oren) // Ganti dengan warna yang sesuai
    }
}
