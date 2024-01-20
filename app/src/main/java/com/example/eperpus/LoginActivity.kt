package com.example.eperpus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.eperpus.constants.Constants
import com.example.eperpus.databinding.ActivityLoginBinding
import com.example.eperpus.session.SessionManager
import org.json.JSONObject
import java.util.concurrent.Executor
import kotlin.math.log

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Inisiasi AndroidNetworking
        binding.btnLogin.setOnClickListener {
            val email = binding.username.text.toString()
            val password = binding.password.text.toString()

            // Perform login using Fast Android Networking
            performLogin(email, password)
        }
        binding.daftar.setOnClickListener {
            val intent = Intent(this@LoginActivity, DaftarActivity::class.java)
            startActivity(intent)
        }

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val sessionManager = SessionManager(this)
                if (sessionManager.getFingerprintStatus()){
                    binding.btnscanfinger.isClickable = true
                    setupBiometricPrompt()
                    binding.btnscanfinger.setOnClickListener {
                        showBiometricPrompt()
                    }
                } else {
                    binding.btnscanfinger.isVisible = false
                }

            }
            else -> binding.btnscanfinger.isVisible = false
        }
    }

    private fun setupBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@LoginActivity, "Biometric authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@LoginActivity, "Biometric authentication successful", Toast.LENGTH_SHORT).show()
                moveActivity()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@LoginActivity, "Biometric authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Scan Sidik Jari")
            .setDescription("Place your fingerprint on the sensor")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }

    private fun performLogin(username: String, password: String) {
        val requestBody = JSONObject()
        requestBody.put("username", username)
        requestBody.put("password", password)

        AndroidNetworking.post("${Constants.BASE_URL}/login")
            .addJSONObjectBody(requestBody)
            .addHeaders("Content-Type", "application/json")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val success = response.getBoolean("success")
                    val message = response.getString("message")
                    Log.d("respon", response.toString())
                    if (success) {
                        val accessToken = response.getString("access_token")
                        val name = response.getString("nama_user")
                        val fingerprintStatus = true

                        val sessionManager = SessionManager(this@LoginActivity)
                        sessionManager.saveSession(name, fingerprintStatus, accessToken)

                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                        moveActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(error: ANError) {
                    Log.d("respon", error.errorBody.toString())
                    Toast.makeText(this@LoginActivity, "Login failed: " + error.errorBody, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun moveActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}