package com.example.eperpus

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.eperpus.adapter.RiwayatAktifAdapter
import com.example.eperpus.constants.Constants
import com.example.eperpus.databinding.FragmentRiwayatAktifBinding
import com.example.eperpus.model.RiwayatAktif
import com.example.eperpus.`object`.SharedPrefsUtil
import com.example.eperpus.session.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONObject

class RiwayatAktifFragment : Fragment() {
    private lateinit var binding: FragmentRiwayatAktifBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatAktifBinding.inflate(inflater, container, false)
        val view = binding.root

        val recyclerView = binding.recycleRiwayatAktif

        // Cek orientasi layar
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Jika landscape, gunakan GridLayoutManager dengan dua kolom
            recyclerView.layoutManager = GridLayoutManager(context, 2)
        } else {
            // Jika portrait, gunakan LinearLayoutManager
            recyclerView.layoutManager = LinearLayoutManager(context)
        }

        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // Saat swipe dijalankan, load data baru
            getDataRiwayatAktif()
            Toast.makeText(requireContext(), "Memperbarui data..", Toast.LENGTH_SHORT).show()
        }

        if (SharedPrefsUtil.isFirstTimeLaunch(requireContext())) {
            getDataRiwayatAktif()
            SharedPrefsUtil.setFirstTimeLaunch(requireContext(), false)
        } else {
            val cachedData = loadDataFromCache()
            if (cachedData.isNotEmpty()) {
                val riwayatAdapter = RiwayatAktifAdapter(requireContext(), cachedData)
                binding.recycleRiwayatAktif.adapter = riwayatAdapter

            } else {
                getDataRiwayatAktif()
            }
        }




        return view
    }

    private fun loadDataFromCache(): MutableList<RiwayatAktif> {
        val sharedPreferences = requireContext().getSharedPreferences("PrefDataRiwayatAktif", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("cachedData", "")
        return if (json!!.isNotEmpty()) {
            val type = object : TypeToken<MutableList<RiwayatAktif>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    private fun saveDataToCache(data: MutableList<RiwayatAktif>) {
        val json = Gson().toJson(data)
        val sharedPreferences = requireContext().getSharedPreferences("PrefDataRiwayatAktif", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("cachedData", json)
        editor.apply()
    }

    private fun getDataRiwayatAktif() {
        lifecycleScope.launch {
            val apiUrl = "${Constants.BASE_URL}/peminjaman/pending"
            val sessionManager = SessionManager(requireContext())
            AndroidNetworking.get(apiUrl)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "Bearer "+sessionManager.getAccessToken())
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val success = response.getBoolean("success")
                        if (success) {
                            val resultArray = response.getJSONArray("result")
                            val items = mutableListOf<RiwayatAktif>()

                            for (i in 0 until resultArray.length()) {
                                val resultObject = resultArray.getJSONObject(i)
                                val bukuObject = resultObject.getJSONObject("buku")

                                val kategoriObject = bukuObject.getJSONObject("kategori")

                                val kategori = kategoriObject.getString("nama_kategori")
                                val gambar = bukuObject.getString("gambar")
                                val judulBuku = bukuObject.getString("judul_buku")
                                val idPinjam = resultObject.getString("id")
                                val kodePeminjaman = resultObject.getString("kode_peminjaman")
                                val status = resultObject.getString("status")


                                val riwayatAktif = RiwayatAktif(
                                   judulBuku,
                                    kategori,
                                    idPinjam.toInt(),
                                    kodePeminjaman,
                                    gambar,
                                    status
                                )
                                items.add(riwayatAktif)
                            }

                            val riwayatAdapter = RiwayatAktifAdapter(requireContext(),items)

                            val recyclerView = binding.recycleRiwayatAktif
                            recyclerView.adapter = riwayatAdapter
                            val orientation = resources.configuration.orientation
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                // Jika landscape, gunakan GridLayoutManager dengan dua kolom
                                recyclerView.layoutManager = GridLayoutManager(context, 2)
                            } else {
                                // Jika portrait, gunakan LinearLayoutManager
                                recyclerView.layoutManager = LinearLayoutManager(context)
                            }

                            saveDataToCache(items)
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                    }

                    override fun onError(error: ANError) {
                        // Handle error
                    }
                })
        }
    }
}