package com.example.project_app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_app.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import com.example.project_app.utils.Result


class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    fun signIn(email: String, password: String) : LiveData<Result<FirebaseUser>> {
        val signInResult = MutableLiveData<Result<FirebaseUser>>()

        viewModelScope.launch {
            signInResult.value = authRepository.signIn(email, password)
        }

        return signInResult
    }

}