package com.example.project_app.utils

object Validators {
    fun checkEmail(email: String): Boolean {
        val pattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return pattern.matches(email)
    }

    // Password must contain at least 5 letters, 1 number and 1 uppercase letter
    fun checkPassword(password: String): Boolean {
        val pattern = "(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{5,}".toRegex()
        return pattern.matches(password)
    }

    fun checkName(name: String): Boolean {
        val regex = Regex("[A-Za-z]+")
        return name.matches(regex)
    }
}