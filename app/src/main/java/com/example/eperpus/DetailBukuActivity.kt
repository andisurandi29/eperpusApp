package com.example.eperpus

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.example.eperpus.constants.Constants
import com.example.eperpus.databinding.ActivityDetailBukuBinding
import com.example.eperpus.model.Buku
import com.example.eperpus.session.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

@Suppress("DEPRECATION")
class DetailBukuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBukuBinding
    private lateinit var buku: Buku

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBukuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            title = "Detail Buku"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_keyboard_arrow_left_24) // Ganti dengan resource ID icon panah kembali
        }

        // Handle tombol panah kembali (back)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val bukuId = intent.getIntExtra("BUKU_ID", -1)

        if (bukuId != -1) {
            fetchDataForDetailAdapter(bukuId)
        } else {

        }

        binding.buttonPinjam?.setOnClickListener {
            // Dapatkan ID buku dari data buku yang sedang ditampilkan
            val bukuId = intent.getIntExtra("BUKU_ID", -1)

            if (bukuId != -1) {
                // Buat Intent untuk pindah ke activity Pinjam
                val intent = Intent(this@DetailBukuActivity, PinjamActivity::class.java)

                // Sisipkan data ID buku ke dalam Intent
                intent.putExtra("ID_BUKU", bukuId)

                // Mulai activity baru dengan Intent
                startActivity(intent)
            } else {
                // Handle jika ID buku tidak valid
                Toast.makeText(this@DetailBukuActivity, "ID buku tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Simpan data ke dalam Bundle
        outState.putString("judulBuku", binding.judulBuku.text.toString())
        outState.putString("kategoriBuku", binding.kategoriBuku.text.toString())
        outState.putString("tahunTerbit", binding.tahunTerbit.text.toString())
        outState.putString("penulisBuku", binding.penulisBuku.text.toString())
        outState.putString("penerbitBuku", binding.penerbitBuku.text.toString())
        outState.putString("deskripsiBuku", binding.deskripsiBuku.text.toString())
        outState.putString("gambarUri", if (::buku.isInitialized) "${Constants.BASE_URL}/static/uploads/${buku.gambar}" else "")

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Pulihkan data dari Bundle
        binding.judulBuku.text = savedInstanceState.getString("judulBuku", "")
        binding.kategoriBuku.text = savedInstanceState.getString("kategoriBuku", "")
        binding.tahunTerbit.text = savedInstanceState.getString("tahunTerbit", "")
        binding.penulisBuku.text = savedInstanceState.getString("penulisBuku", "")
        binding.penerbitBuku.text = savedInstanceState.getString("penerbitBuku", "")
        binding.deskripsiBuku.text = savedInstanceState.getString("deskripsiBuku", "")
        Glide.with(this)
            .load(savedInstanceState.getString("gambarUri", ""))
            .into(binding.coverBuku)
        // ... tambahkan pemulihan data lainnya
    }
    private fun fetchDataForDetailAdapter(bukuId: Int) {
        lifecycleScope.launch {
            val apiUrl = "${Constants.BASE_URL}/buku/$bukuId"
            val sessionManager = SessionManager(this@DetailBukuActivity)

            AndroidNetworking.get(apiUrl)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "Bearer " + sessionManager.getAccessToken())
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val success = response.getBoolean("success")
                        if (success) {
                            val resultObject = response.getJSONObject("result")
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

                            // Lakukan sesuatu dengan data buku, misalnya tampilkan ke UI
                            displayBukuDetail(buku)
                        }
                    }

                    override fun onError(error: ANError) {
                        // Handle error
                    }
                })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayBukuDetail(buku: Buku) {
        if (!isDestroyed) {
            binding.judulBuku.text = buku.judul
            binding.kategoriBuku.text = "Kategori : ${buku.kategori}"
            binding.tahunTerbit.text = "Tahun: ${buku.tahun}"
            binding.penulisBuku.text = buku.penulis
            binding.penerbitBuku.text = buku.penerbit
            binding.deskripsiBuku.text = buku.deskripsi

            Glide.with(this)
                .load("${Constants.BASE_URL}/static/uploads/${buku.gambar}")
                .into(binding.coverBuku)
        }
    }

    override fun onStop() {
        super.onStop()
        // Hentikan permintaan Glide jika perlu
        Glide.with(this).clear(binding.coverBuku)
    }
    override fun onDestroy() {
        super.onDestroy()

    }
}