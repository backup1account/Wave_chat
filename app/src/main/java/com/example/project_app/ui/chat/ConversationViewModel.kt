package com.example.project_app.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project_app.auth.MessageRepository
import com.example.project_app.auth.data_classes.Message
import com.example.project_app.utils.Result
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.launch


class ConversationViewModel(private val messageRepository: MessageRepository): ViewModel() {

    fun sendMessage(messageToSend: Message) {
        viewModelScope.launch {
            try {
                messageRepository.sendMessage(messageToSend)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }


    fun getConversation(senderId: String, receiverId: String): LiveData<List<Message>> {
        val getConversationResult = MutableLiveData<List<Message>>()

        viewModelScope.launch {
            try {
                val result = messageRepository.getConversation(senderId, receiverId)
                getConversationResult.value = result
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
        return getConversationResult
    }


    fun getAllUsersConversations(): LiveData<List<DocumentSnapshot>> {
        val getUserConversationsResult = MutableLiveData<List<DocumentSnapshot>>()

        viewModelScope.launch {
            try {
                val res = messageRepository.getAllUsersConversations()
                getUserConversationsResult.value = res
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
        return getUserConversationsResult
    }
}