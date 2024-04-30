package com.example.ownchatbot.android.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ownchatbot.android.view.ChatState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Reader
import java.io.StringReader

class PreferencesManager(context: Context) {


    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("HistPref", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun saveChatState(chatState: ChatState) {
        sharedPreferences.edit().putString("chat_hist", chatState.toString()).apply()
    }

    fun saveChatStateFlow(file: MutableStateFlow<ChatState>) {
        val chatState = Gson().toJson(file)
        sharedPreferences.edit().putString("chat_hist", chatState).apply()
    }

    fun getChatStateFlow(): MutableStateFlow<ChatState>? {
        var retVal: MutableStateFlow<ChatState>? = null
        val chatStateString = sharedPreferences.getString("chat_hist", "")
        val reader = JsonReader(StringReader(chatStateString) as Reader?)
        reader.isLenient = true
        try {
            retVal = Gson().fromJson(reader, object : TypeToken<MutableStateFlow<ChatState>>() {}.type)
        } catch (_: Exception) { }
        return retVal
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences("HistPref", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("chat_hist", "")!!
        editor.apply()

    }

}