package com.example.eperpus.`object`

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsUtil {
    private const val PREFS_NAME = "MyPrefs"
    private const val KEY_FIRST_TIME_LAUNCH = "firstTimeLaunch"

    fun isFirstTimeLaunch(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.getBoolean(KEY_FIRST_TIME_LAUNCH, true)
    }

    fun setFirstTimeLaunch(context: Context, isFirstTime: Boolean) {
        val prefs = getSharedPreferences(context)
        val editor = prefs.edit()
        editor.putBoolean(KEY_FIRST_TIME_LAUNCH, isFirstTime)
        editor.apply()
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}