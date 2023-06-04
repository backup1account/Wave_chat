package com.example.project_app.ui.chat

import com.example.project_app.auth.data_classes.Conversation

interface OnConversationClickListener {
    fun onConversationClick(conversation: Conversation)
}