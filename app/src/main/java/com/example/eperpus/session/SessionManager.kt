package com.example.eperpus.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context : Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        const val KEY_NAME = "username"
        const val KEY_FINGERPRINT_STATUS = "fingerprint_status"
        const val KEY_ACCESS_TOKEN = "access_token"
    }

    fun saveSession(namaUser: String, fingerprintStatus: Boolean, accessToken: String) {
        editor.putString(KEY_NAME, namaUser)
        editor.putBoolean(KEY_FINGERPRINT_STATUS, fingerprintStatus)
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.apply()
    }

    fun getName(): String? {
        return sharedPreferences.getString(KEY_NAME, null)
    }

    fun getFingerprintStatus(): Boolean {
        return sharedPreferences.getBoolean(KEY_FINGERPRINT_STATUS, false)
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}