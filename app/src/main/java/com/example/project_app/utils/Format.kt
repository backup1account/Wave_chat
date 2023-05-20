package com.example.project_app.utils

object Format {
    fun formatUserIndex(index: Int): String {
        return index.toString().padStart(5, '0')
    }
}