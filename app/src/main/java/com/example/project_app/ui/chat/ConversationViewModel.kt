package com.example.project_app.ui.chat

import androidx.lifecycle.*
import com.example.project_app.auth.MessageRepository
import com.example.project_app.auth.data_classes.Conversation
import com.example.project_app.auth.data_classes.Message
import com.example.project_app.utils.Result
import kotlinx.coroutines.Dispatchers
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

    fun getAllUsersConversations(): LiveData<List<Conversation>> {
        return liveData(Dispatchers.IO) {
            try {
                val conversationsList = messageRepository.getAllUsersConversations()
                emitSource(conversationsList.asLiveData())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}