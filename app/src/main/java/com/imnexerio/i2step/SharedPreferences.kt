package com.imnexerio.i2step

import android.content.Context
import android.content.SharedPreferences


class SharedPrefManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)

    fun saveDisplayName(displayName: String) {
        val editor = sharedPreferences.edit()
        editor.putString("DISPLAY_NAME", displayName)
        editor.apply()
    }
    fun getDisplayName(): String? {
        return sharedPreferences.getString("DISPLAY_NAME", null)
    }

    fun saveRole(role: String) {
        val editor = sharedPreferences.edit()
        editor.putString("ROLE", role)
        editor.apply()
    }

    fun getRole(): String? {
        return sharedPreferences.getString("ROLE", null)
    }
}