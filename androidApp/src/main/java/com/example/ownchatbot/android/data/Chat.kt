package com.example.ownchatbot.android.data

import android.graphics.Bitmap
import java.util.Date

data class Chat (
    val prompt: String,
    val bitmap: Bitmap?,
    val time: Date,
    val isFromUser: Boolean
)