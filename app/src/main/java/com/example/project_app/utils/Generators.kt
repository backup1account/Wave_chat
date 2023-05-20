package com.example.project_app.utils

import kotlin.random.Random

object Generators {
    fun generateRandomUserIndex(): Int {
        val random = Random.Default
        val min = 1
        val max = 99999
        return random.nextInt(min, max + 1)
    }
}