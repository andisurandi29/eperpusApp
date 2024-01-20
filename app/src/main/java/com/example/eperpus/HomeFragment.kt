package com.example.eperpus

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.eperpus.adapter.GridAdapter
import com.example.eperpus.adapter.SlideAdapter
import com.example.eperpus.constants.Constants
import com.example.eperpus.databinding.FragmentHomeBinding
import com.example.eperpus.model.Buku
import com.example.eperpus.`object`.SharedPrefsUtil
import com.example.eperpus.session.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var horizontalAdapter: SlideAdapter
    private val handler = Handler()
    private var currentPage = 0
    private val slideInterval: Long = 1000
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val refreshInterval: Long = 5 * 60 * 1000 // Set interval (dalam milidetik), contoh: 5 menit
    private val refreshHandler = Handler()

    private val refreshRunnable = object : Runnable {
        override fun run() {
            // Panggil fungsi untuk memperbarui data
            fetchDataForGridAdapter()

            // Jadwalkan pembaruan berikutnya setelah interval tertentu
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        val banner = binding.horizontalRecyclerView
        banner.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val recyclerView = binding.gridRecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // Saat swipe dijalankan, load data baru
            fetchDataForGridAdapter()
            Toast.makeText(requireContext(), "Memperbarui data..", Toast.LENGTH_SHORT).show()
        }


        if (SharedPrefsUtil.isFirstTimeLaunch(requireContext())) {
            fetchDataForGridAdapter()
            SharedPrefsUtil.setFirstTimeLaunch(requireContext(), false)
        } else {
            val cachedData = loadDataFromCache()
            if (cachedData.isNotEmpty()) {
                val gridAdapter = GridAdapter(requireContext(), cachedData)
                binding.gridRecyclerView.adapter = gridAdapter

                val bannerAdapter = SlideAdapter(cachedData)
                binding.horizontalRecyclerView.adapter = bannerAdapter
                startSlideAuto()
            } else {
                fetchDataForGridAdapter()
            }
        }

        return view
    }

    private fun loadDataFromCache(): MutableList<Buku> {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("cachedData", "")
        return if (json!!.isNotEmpty()) {
            val type = object : TypeToken<MutableList<Buku>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    private fun saveDataToCache(data: MutableList<Buku>) {
        val json = Gson().toJson(data)
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("cachedData", json)
        editor.apply()
    }

    private fun startSlideAuto() {
        if (::horizontalAdapter.isInitialized) {
            val update = Runnable {
                if (currentPage == horizontalAdapter.itemCount) {
                    currentPage = 0
                }
                binding.horizontalRecyclerView.smoothScrollToPosition(currentPage++)
            }

            handler.postDelayed(update, slideInterval)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        startSlideAuto()
        startAutoRefresh()
    }
    override fun onPause() {
        super.onPause()
        stopAutoRefresh()
    }

    private fun startAutoRefresh() {
        // Jalankan pembaruan pertama kali
        fetchDataForGridAdapter()

        // Jadwalkan pembaruan berikutnya setelah interval tertentu
        refreshHandler.postDelayed(refreshRunnable, refreshInterval)
    }

    private fun stopAutoRefresh() {
        // Hentikan pembaruan otomatis
        refreshHandler.removeCallbacks(refreshRunnable)
    }


    private fun fetchDataForGridAdapter() {
        lifecycleScope.launch {
            val apiUrl = "${Constants.BASE_URL}/buku"
            val sessionManager = SessionManager(requireContext())
            AndroidNetworking.get(apiUrl)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "Bearer "+sessionManager.getAccessToken())
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (!isAdded) {
                            // Fragment tidak terpasang, keluar dari fungsi
                            return
                        }
                        val success = response.getBoolean("success")
                        if (success) {
                            val resultArray = response.getJSONArray("result")
                            val items = mutableListOf<Buku>()

                            for (i in 0 until resultArray.length()) {
                                val resultObject = resultArray.getJSONObject(i)
                                val kategoriObject = resultObject.getJSONObject("kategori")
                                val kategori = kategoriObject.getString("nama_kategori")


                                val buku = Buku(
                                    resultObject.getString("judul_buku"),
                                    resultObject.getString("deskripsi"),
                                    resultObject.getString("gambar"),
                                    resultObject.getInt("id"),
                                    kategori,
                                    resultObject.getInt("kategori_id"),
                                    resultObject.getString("kode_buku"),
                                    resultObject.getString("penerbit"),
                                    resultObject.getString("penulis"),
                                    resultObject.getInt("stok"),
                                    resultObject.getInt("tahun")
                                )
                                items.add(buku)
                            }

                            val gridAdapter = GridAdapter(requireContext(),items)
                            binding.gridRecyclerView.adapter = gridAdapter
                            val bannerAdapter = SlideAdapter(items)
                            binding.horizontalRecyclerView.adapter = bannerAdapter
                            startSlideAuto()
                            saveDataToCache(items)
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }

                    override fun onError(error: ANError) {
                        // Handle error
                    }
                })
        }
    }
}