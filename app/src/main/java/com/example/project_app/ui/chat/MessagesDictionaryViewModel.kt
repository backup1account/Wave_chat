package com.example.project_app.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.project_app.auth.MessagesDictionaryRepository
import com.example.project_app.utils.Result


class MessagesDictionaryViewModel(private val messagesDictionaryRepository: MessagesDictionaryRepository): ViewModel() {

    fun saveMessageToDictionary(messageContent: String) {
        viewModelScope.launch {
            try {
                messagesDictionaryRepository.saveMessageToDictionary(messageContent)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    fun getDictionaryMessages(): LiveData<List<String>> {
        val getConversationResult = MutableLiveData<List<String>>()

        viewModelScope.launch {
            try {
                val result = messagesDictionaryRepository.getDictionaryMessages()
                getConversationResult.value = result
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
        return getConversationResult
    }

}