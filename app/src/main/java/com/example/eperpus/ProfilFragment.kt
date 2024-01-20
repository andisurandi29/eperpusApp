package com.example.eperpus

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.eperpus.adapter.RiwayatAktifAdapter
import com.example.eperpus.constants.Constants
import com.example.eperpus.databinding.FragmentHomeBinding
import com.example.eperpus.databinding.FragmentProfilBinding
import com.example.eperpus.model.RiwayatAktif
import com.example.eperpus.session.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class ProfilFragment : Fragment() {
    private lateinit var binding: FragmentProfilBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this
        binding = FragmentProfilBinding.inflate(inflater, container, false)
        val view = binding.root


        getDataProfil()
        binding.btnLogout.setOnClickListener {
            val sessionManager = SessionManager(requireContext())
            sessionManager.clearSession()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    private fun getDataProfil() {
        lifecycleScope.launch {
            val apiUrl = "${Constants.BASE_URL}/profil"
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
                            val result = response.getJSONObject("result")

                            binding.username.text = result.getString("email")
                            binding.email.text = result.getString("email")
                            binding.telp.text = result.getString("telp")
                            binding.nama.text = sessionManager.getName()
                            }
                        }

                    override fun onError(error: ANError) {
                        // Handle error
                    }
                })
        }
    }
}