package com.example.eperpus

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.eperpus.constants.Constants
import com.example.eperpus.databinding.ActivityDaftarBinding
import com.example.eperpus.model.Fakultas
import com.example.eperpus.model.Prodi
import org.json.JSONArray
import org.json.JSONObject

class DaftarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarBinding
    private lateinit var fakultasSpinner: Spinner
    private lateinit var prodiSpinner: Spinner
    private lateinit var fakultasList: List<Fakultas>
    private lateinit var prodiList: List<Prodi>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_keyboard_arrow_left_24) // Ganti dengan resource ID icon panah kembali
        }

        // Handle tombol panah kembali (back)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        fakultasSpinner = binding.fakultas
        prodiSpinner = binding.prodi

        setupFakultasSpinner()

        binding.btnDaftar.setOnClickListener {
            val email = binding.email.text.toString()
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            val nama = binding.nama.text.toString()
            val telp = binding.telp.text.toString()

            val selectedFakultasPosition = fakultasSpinner.selectedItemPosition
            val selectedProdiPosition = prodiSpinner.selectedItemPosition

            if (selectedFakultasPosition != AdapterView.INVALID_POSITION && selectedProdiPosition != AdapterView.INVALID_POSITION) {
                val selectedFakultas = this@DaftarActivity.fakultasList[0]
                val selectedProdi = this@DaftarActivity.prodiList[0]

                registerUser(email, username, password, nama, telp, selectedFakultas.id, selectedProdi.id)
            } else {
                Toast.makeText(this@DaftarActivity, "Pilih Fakultas dan Prodi terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFakultasSpinner() {
        val apiUrl = "${Constants.BASE_URL}/fakultas"
        AndroidNetworking.get(apiUrl)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getBoolean("success")) {
                        fakultasList = parseFakultasData(response.getJSONArray("result"))

                        val fakultasAdapter = ArrayAdapter(this@DaftarActivity, android.R.layout.simple_spinner_item, fakultasList.map { it.namaFakultas })
                        fakultasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        fakultasSpinner.adapter = fakultasAdapter

                        fakultasSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val selectedFakultas = fakultasList[position]
                                prodiSpinner.visibility = View.VISIBLE
                                setupProdiSpinner(selectedFakultas.id)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // Handle nothing selected if needed
                            }
                        }
                    } else {
                        Toast.makeText(this@DaftarActivity, "Gagal mendapatkan data Fakultas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    Toast.makeText(this@DaftarActivity, "Error: ${anError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun setupProdiSpinner(fakultasId: Int) {
        val apiUrl = "${Constants.BASE_URL}/fakultas/${fakultasId}/prodi/"
        AndroidNetworking.get(apiUrl)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getBoolean("success")) {
                        Log.e("RESPON PRODI :", response.toString())
                        prodiList = parseProdiData(response.getJSONArray("result"))
                        val prodiAdapter = ArrayAdapter(this@DaftarActivity, android.R.layout.simple_spinner_item, prodiList.map { it.namaProdi })
                        prodiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        prodiSpinner.adapter = prodiAdapter

                    } else {
                        Toast.makeText(this@DaftarActivity, "Gagal mendapatkan data Prodi", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    Log.d("DaftarActivity", "Prodi URL: ${Constants.BASE_URL}/$fakultasId/prodi")

                    Log.d("DaftarActivity", "Selected Fakultas ID: $fakultasId")
                    Log.e("API PRODI", "Error: ${anError.message}", anError)
                    Toast.makeText(this@DaftarActivity, "Error: ${anError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun registerUser(email: String, username: String, password: String, nama: String, telepon: String, fakultasId: Int, prodiId: Int) {
        val requestBody = JSONObject().apply {
            put("email", email)
            put("username", username)
            put("password", password)
            put("nama_lengkap", nama)
            put("telp", telepon)
            put("fakultas_id", fakultasId)
            put("level", "user")
            put("prodi_id", prodiId)
        }

        AndroidNetworking.post("${Constants.BASE_URL}/register") // Ganti dengan URL API Register
            .addJSONObjectBody(requestBody)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val success = response.getBoolean("success")
                    if (success) {
                        showSuccessDialog()
                    } else {
                        // Handle error
                        val message = response.getString("message")
                        Toast.makeText(this@DaftarActivity, "Gagal mendaftar: $message", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    // Handle error
                    Toast.makeText(this@DaftarActivity, "Error: ${anError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showSuccessDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Daftar Akun Berhasil")
        alertDialogBuilder.setMessage("Terima kasih! Anda Berhasil Mendaftar. Silahkan Login")
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showGagalDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this@DaftarActivity)
        alertDialogBuilder.setTitle("Daftar Akun Gagal")
        alertDialogBuilder.setMessage("Maaf, Pendaftaran Anda gagal.")
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}

    private fun parseFakultasData(jsonArray: JSONArray): List<Fakultas> {
        val fakultasList = mutableListOf<Fakultas>()
        for (i in 0 until jsonArray.length()) {
            val fakultasObj = jsonArray.getJSONObject(i)
            val fakultas = Fakultas(fakultasObj.getInt("id"), fakultasObj.getString("nama_fakultas"))
            fakultasList.add(fakultas)
        }
        return fakultasList
    }

    private fun parseProdiData(jsonArray: JSONArray): List<Prodi> {
        val prodiList = mutableListOf<Prodi>()
        for (i in 0 until jsonArray.length()) {
            val prodiObj = jsonArray.getJSONObject(i)
            val prodi = Prodi(prodiObj.getInt("id"), prodiObj.getString("nama_prodi"))
            prodiList.add(prodi)
        }
        return prodiList
    }

