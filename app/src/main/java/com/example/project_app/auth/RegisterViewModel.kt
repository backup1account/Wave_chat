package com.example.project_app.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_app.repositories.AuthRepository
import com.example.project_app.data_classes.User
import com.example.project_app.utils.Result
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch


class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    fun signUp(email: String, password: String) : LiveData<Result<FirebaseUser>> {
        val signOutResult = MutableLiveData<Result<FirebaseUser>>()

        viewModelScope.launch {
            signOutResult.value = authRepository.signUp(email, password)
        }

        return signOutResult
    }

    fun addUserToDatabase(user: User) : LiveData<Result<String>> {
        val addUserResult = MutableLiveData<Result<String>>()

        viewModelScope.launch {
            try {
                val result = authRepository.addUserToDatabase(user)
                addUserResult.value = result
            } catch (e: Exception) {
                addUserResult.value = Result.Error(e)
            }
        }

        return addUserResult
    }

}