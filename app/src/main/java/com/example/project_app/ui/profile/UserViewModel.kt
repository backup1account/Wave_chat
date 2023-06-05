package com.example.project_app.ui.profile

import androidx.lifecycle.*
import com.example.project_app.auth.UserRepository
import com.example.project_app.auth.data_classes.Message
import com.example.project_app.auth.data_classes.User
import com.example.project_app.utils.Result
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch


class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    val searchUsersResult: MutableLiveData<List<User>> = MutableLiveData()

    fun signOut() {
        userRepository.signOut()
    }

    fun updateName(name: String): LiveData<Result<FirebaseUser>> {
        val result = MutableLiveData<Result<FirebaseUser>>()

        viewModelScope.launch {
            result.value = userRepository.updateName(name)
        }
        return result
    }

    fun updatePassword(newPassword: String): LiveData<Result<FirebaseUser>> {
        val result = MutableLiveData<Result<FirebaseUser>>()

        viewModelScope.launch {
            result.value = userRepository.updatePassword(newPassword)
        }
        return result
    }

    fun updateProfileUrl(profileUrl: String): LiveData<Result<FirebaseUser>> {
        val result = MutableLiveData<Result<FirebaseUser>>()

        viewModelScope.launch {
            result.value = userRepository.updateProfileUrl(profileUrl)
        }
        return result
    }

    // update city ?

    fun searchUsers(query: String) : LiveData<List<User>> {
        viewModelScope.launch {
            try {
                val users = userRepository.searchUsers(query)
                searchUsersResult.value = users
            } catch (e: Exception) {
                searchUsersResult.value = emptyList()
            }
        }
        return searchUsersResult
    }

    fun saveMessageProposition(messageContent: String) {
        viewModelScope.launch {
            try {
                userRepository.saveMessageProposition(messageContent)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

}