package com.example.project_app.auth.data_classes

class Conversation(
    val conversationId: String?,
    val latestMessage: Message?,
    val receiverUser: User?
)