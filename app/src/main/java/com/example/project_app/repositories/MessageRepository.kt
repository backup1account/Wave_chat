package com.example.project_app.repositories

import android.util.Log
import com.example.project_app.FirebaseManager
import com.example.project_app.data_classes.Conversation
import com.example.project_app.data_classes.Message
import com.example.project_app.data_classes.User
import com.example.project_app.utils.Result
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MessageRepository(val auth: FirebaseAuth) {
    private val MESSAGE_SEND_TAG = "SEND MESSAGE RESULT"
    private val CREATE_DOCUMENT_TAG = "CREATE DOCUMENT RESULT"
    private val FETCH_CHAT = "FETCH CHAT RESULT"

    private val conversationsCollection = FirebaseManager.firestore.collection("conversations")
    private val usersCollection = FirebaseManager.firestore.collection("users")

    suspend fun sendMessage(messageToSend: Message) = withContext(Dispatchers.IO) {
        try {
            val sender = messageToSend.senderId
            val receiver = messageToSend.receiverId

            val query = conversationsCollection.where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("senderId", sender),
                        Filter.equalTo("receiverId", receiver)
                    ),
                    Filter.and(
                        Filter.equalTo("senderId", receiver),
                        Filter.equalTo("receiverId", sender)
                    )
                )
            )

            // Add message as a field in conversation document
            // Field needs to be Map, so use Gson to convert Message data class object to proper type
            val gson = Gson()
            val messageMap = gson.fromJson(gson.toJson(messageToSend), Map::class.java)

            query.get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        val querySnapshot = task.result

                        // conversation does not exist yet
                        if (querySnapshot.isEmpty) {
                            val conversationRef = conversationsCollection.document()
                            val conversationId = conversationRef.id

                            val conversationDocFields = hashMapOf(
                                "conversationId" to conversationId,
                                "senderId" to sender,
                                "receiverId" to receiver
                            )

                            conversationsCollection.add(conversationDocFields)
                                .addOnSuccessListener { docRef ->
                                    val documentId = docRef.id

                                    conversationsCollection.document(documentId).collection("messages").add(messageMap)
                                        .addOnSuccessListener {
                                            Log.d(MESSAGE_SEND_TAG, "Message saved successfully to new conversation")
                                        }.addOnFailureListener {
                                            Log.e(MESSAGE_SEND_TAG, "Error saving message to new conversation: $it")
                                        }
                                    Log.d(CREATE_DOCUMENT_TAG, "Document created with ID: $documentId")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(CREATE_DOCUMENT_TAG, "Error creating document: $e")
                                }

                        }
                        // conversation already exists
                        else {
                            val conversationDocument = querySnapshot.documents.single()
                            val conversationId = conversationDocument.id

                            conversationsCollection.document(conversationId).collection("messages")
                                .add(messageMap)
                                .addOnSuccessListener { docRef ->
                                    val messageId = docRef.id
                                    Log.d(MESSAGE_SEND_TAG, "Message added to existing conversation: $conversationId, " +
                                            "Message ID: $messageId")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(MESSAGE_SEND_TAG, "Error adding message to existing conversation: $e")
                                }
                        }

                    } else {
                        Log.e("NOT_FOUND", "Error getting user conversation for sending message: ${task.exception}")
                    }
                }
            Result.Success("message sent successfully!")
        } catch (e: Exception) {
            Result.Error(e)
        }
    }


    suspend fun getConversation(senderId: String, receiverId: String): Flow<List<Message>> = flow {
        val messagesList = mutableListOf<Message>()

        try {
            val query = conversationsCollection.where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("senderId", senderId),
                        Filter.equalTo("receiverId", receiverId)
                    ),
                    Filter.and(
                        Filter.equalTo("senderId", receiverId),
                        Filter.equalTo("receiverId", senderId)
                    )
                )
            )

            val querySnapshot = query.get().await()

            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot) {
                    Log.d(FETCH_CHAT, "${document.id} -> ${document.data}")
                    val conversationId = document.id

                    val messagesSnapshot = conversationsCollection
                        .document(conversationId)
                        .collection("messages")
                        .get()
                        .await()

                    for (messageDoc in messagesSnapshot) {
                        val message = messageDoc.toObject(Message::class.java)
                        message.let {
                            messagesList.add(it)
                        }
                    }
                }
            } else {
                Log.e(FETCH_CHAT, "Error fetching conversation")
            }

            messagesList.sortBy { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        emit(messagesList)
    }


    fun getAllUsersConversations(): Flow<List<Conversation>> = flow {
        val conversationsDocLists = mutableListOf<Conversation>()

        try {
            val userId = FirebaseManager.auth.uid
            val query = conversationsCollection.where(Filter.equalTo("senderId", userId))
            val querySnapshot = query.get().await()

            for (document in querySnapshot.documents) {
                val messagesCollection = document.reference.collection("messages")
                val latestMessageQuery = messagesCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)

                val latestMessageSnapshot = latestMessageQuery.get().await()

                if (!latestMessageSnapshot.isEmpty) {
                    val receiverDocId = document.getString("receiverId")

                    if (receiverDocId != null) {
                        usersCollection.document(receiverDocId).get().await()?.let { receiverSnapshot ->
                            val receiverUser = receiverSnapshot.toObject(User::class.java)
                            val conversationId = document.id

                            val latestMessageDocument = latestMessageSnapshot.documents[0]
                            val latestMessage = latestMessageDocument.toObject(Message::class.java)

                            val conversation = Conversation(
                                conversationId = conversationId,
                                latestMessage = latestMessage,
                                receiverUser = receiverUser
                            )

                            conversationsDocLists.add(conversation)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        emit(conversationsDocLists)
    }

}