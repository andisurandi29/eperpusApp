package com.example.eperpus

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.example.eperpus.constants.Constants
import com.example.eperpus.databinding.ActivityDetailRiwayatBinding
import com.example.eperpus.model.Buku
import com.example.eperpus.model.RiwayatAktif
import com.example.eperpus.session.SessionManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.launch
import org.json.JSONObject

class DetailRiwayatActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailRiwayatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val riwayatId = intent.getIntExtra("RIWAYAT_ID", -1)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            title = "Detail Riwayat"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_keyboard_arrow_left_24) // Ganti dengan resource ID icon panah kembali
        }

        // Handle tombol panah kembali (back)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        getDetailRiwayat(riwayatId)
    }

    private fun generateQRCode(text: String): Bitmap? {
        val multiFormatWriter = MultiFormatWriter()
        try {
            // Set ukuran dan format QR code
            val bitMatrix: BitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 2000, 2000)
            // Konversi BitMatrix ke Bitmap
            val width: Int = bitMatrix.width
            val height: Int = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) -0x1000000 else -0x1
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            // Return placeholder jika terjadi kesalahan
            return null
        }
    }

    private fun getDetailRiwayat(riwayatId: Int) {
        lifecycleScope.launch {
            val apiUrl = "${Constants.BASE_URL}/peminjaman/$riwayatId"
            val sessionManager = SessionManager(this@DetailRiwayatActivity)

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


                            val bukuObject = resultObject.getJSONObject("buku")
                            val judulBuku = bukuObject.getString("judul_buku")
                            val kodePeminjaman = resultObject.getString("kode_peminjaman")
                            val tglPinjam = resultObject.getString("tgl_pinjam")
                            val tglKembali = resultObject.getString("tgl_pengembalian")

                            binding.judulBuku.text = judulBuku
                            binding.kodePinjam.text = kodePeminjaman
                            binding.tglPinjam.text = tglPinjam
                            binding.tglKembali.text = tglKembali

                            // Panggil fungsi untuk membuat QR code dengan teks yang diinginkan
                            val bitmap = generateQRCode(kodePeminjaman)
                            binding.qrCode.setImageBitmap(bitmap)

                        }
                    }

                    override fun onError(error: ANError) {
                        Log.e("GAGAL MENGAMBIL DATA DETAIL RIWAYAT :",error.toString())
                        Toast.makeText(this@DetailRiwayatActivity, "Terjadi Kesalahan", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }


}