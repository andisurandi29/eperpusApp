package com.example.eperpus

import android.R
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.example.eperpus.constants.Constants
import com.example.eperpus.databinding.ActivityDetailBukuBinding
import com.example.eperpus.databinding.ActivityPinjamBinding
import com.example.eperpus.model.Buku
import com.example.eperpus.session.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class PinjamActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPinjamBinding
    private val calendar = Calendar.getInstance()
    private var bukuJudul: String? = null
    private var bukuKategori: String? = null
    private var bukuKode: String? = null
    private var bukuGambar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinjamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(com.example.eperpus.R.drawable.baseline_keyboard_arrow_left_24) // Ganti dengan resource ID icon panah kembali
        }

        // Handle tombol panah kembali (back)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val spinner = binding.spinner
        val items = arrayOf(1, 2, 3, 4, 5, 6, 7)
        val adapter = ArrayAdapter(this,R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Tindakan yang akan diambil ketika suatu item dipilih
                selectedItemView?.let {
                    val selectedOption = items[position]
                    Toast.makeText(applicationContext, "Anda memilih: $selectedOption", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Tindakan yang akan diambil ketika tidak ada item yang dipilih
            }
        }

        if (savedInstanceState != null) {
            // Pulihkan data dari Bundle
            bukuJudul = savedInstanceState.getString("bukuJudul")
            bukuKategori = savedInstanceState.getString("bukuKategori")
            bukuKode = savedInstanceState.getString("bukuKode")
            bukuGambar = savedInstanceState.getString("bukuGambar")

            // Update UI dengan data yang dipulihkan
            updateUI()
        } else {
            // Jika tidak ada data yang dipulihkan dari save state, ambil data dari intent
            val bukuId = intent.getIntExtra("ID_BUKU", -1)
            if (bukuId != -1) {
                fetchDataForDetailAdapter(bukuId)
            }
        }

        // Tambahkan OnClickListener pada EditText
        binding.tglPeminjaman.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonPosesPinjam.setOnClickListener {
            postPeminjaman()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Simpan data ke dalam Bundle
        outState.putString("bukuJudul", bukuJudul)
        outState.putString("bukuKategori", bukuKategori)
        outState.putString("bukuKode", bukuKode)
        outState.putString("bukuGambar", bukuGambar)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Pulihkan data dari Bundle
        bukuJudul = savedInstanceState.getString("bukuJudul")
        bukuKategori = savedInstanceState.getString("bukuKategori")
        bukuKode = savedInstanceState.getString("bukuKode")
        bukuGambar = savedInstanceState.getString("bukuGambar")

        // Update UI dengan data yang dipulihkan
        updateUI()
    }

    private fun updateUI() {
        // Update UI dengan data yang dipulihkan atau dari intent
        binding.judulBuku.text = bukuJudul
        binding.kategori.text = "Kategori : $bukuKategori"
        binding.kodeBuku.text = "Kode Buku : $bukuKode"
        Glide.with(this)
            .load(bukuGambar)
            .into(binding.imageBuku)
    }

    private fun postPeminjaman() {
        val apiUrl = "${Constants.BASE_URL}/peminjaman"
        val bukuId = intent.getIntExtra("ID_BUKU", -1)
        val durasiPeminjaman = binding.spinner.selectedItem.toString()
        val tglPinjam = binding.tglPeminjaman.text.toString()

        if (bukuId != -1) {
            val sessionManager = SessionManager(this@PinjamActivity)
            val jsonObject = JSONObject().apply {
                put("durasi_peminjaman", durasiPeminjaman.toInt())
                put("buku_id", bukuId)
                put("tgl_pinjam", tglPinjam)
            }

            AndroidNetworking.post(apiUrl)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "Bearer ${sessionManager.getAccessToken()}")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        showSuccessDialog()
                    }

                    override fun onError(error: ANError) {
                        showGagalDialog()
                    }
                })
        } else {
            showGagalDialog()
        }
    }

    private fun showSuccessDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Peminjaman Berhasil")
        alertDialogBuilder.setMessage("Terima kasih! Peminjaman Anda berhasil.")
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showGagalDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Peminjaman Gagal")
        alertDialogBuilder.setMessage("Maaf, peminjaman Anda gagal.")
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showDatePickerDialog() {
        // Mendapatkan tanggal awal dari EditText jika sudah diisi sebelumnya
        val dateString = binding.tglPeminjaman.text.toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val date = dateFormat.parse(dateString)
            calendar.time = date
        } catch (e: Exception) {
            // Tangani kesalahan parsing tanggal
        }

        // Membuat DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Update tanggal pada calendar
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Format tanggal dan set pada EditText
                val selectedDate = dateFormat.format(calendar.time)
                binding.tglPeminjaman.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Tampilkan DatePickerDialog
        datePickerDialog.show()
    }

    private fun fetchDataForDetailAdapter(bukuId: Int) {
        lifecycleScope.launch {
            val apiUrl = "${Constants.BASE_URL}/buku/$bukuId"
            val sessionManager = SessionManager(this@PinjamActivity)

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

                            // Lakukan sesuatu dengan data buku, misalnya tampilkan ke UI
                            bukuJudul = resultObject.getString("judul_buku")
                            bukuKategori = "Kategori : ${kategori}"
                            bukuKode   = "Kode Buku : ${resultObject.getString("kode_buku")}"
                            bukuGambar = "${Constants.BASE_URL}/static/uploads/${resultObject.getString("gambar")}"
                            binding.judulBuku.text =  resultObject.getString("judul_buku")
                            binding.kategori.text =  "Kategori : ${kategori}"
                            binding.kodeBuku.text =  "Kode Buku : ${resultObject.getString("kode_buku")}"
                            Glide.with(this@PinjamActivity)
                                .load("${Constants.BASE_URL}/static/uploads/${resultObject.getString("gambar")}")
                                .into(binding.imageBuku)
                        }
                    }

                    override fun onError(error: ANError) {
                        // Handle error
                    }
                })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}