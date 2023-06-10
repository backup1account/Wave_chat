package com.example.project_app.chat

import androidx.lifecycle.*
import com.example.project_app.repositories.MessageRepository
import com.example.project_app.data_classes.Conversation
import com.example.project_app.data_classes.Message
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
        return liveData(Dispatchers.IO) {
            try {
                val result = messageRepository.getConversation(senderId, receiverId)
                emitSource(result.asLiveData())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
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