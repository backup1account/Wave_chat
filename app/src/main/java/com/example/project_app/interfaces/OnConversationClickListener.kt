package com.example.project_app.interfaces

import com.example.project_app.data_classes.Conversation

interface OnConversationClickListener {
    fun onConversationClick(conversation: Conversation)
}