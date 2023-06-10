package com.example.project_app.data_classes

data class Message(
    val senderId: String?,
    val receiverId: String?,
    val messageContent: String?,
    val messageType: String?,
    val timestamp: Long?
) {
    // Initialize empty constructor - messages cannot be viewed without it
    constructor() : this(null, null, null, null, null)
}