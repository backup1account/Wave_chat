package com.example.project_app.auth

import android.util.Log
import com.example.project_app.FirebaseManager
import com.example.project_app.auth.data_classes.Message
import com.example.project_app.utils.Result
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.snapshots
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MessageRepository(val auth: FirebaseAuth) {
    private val MESSAGE_SEND_TAG = "SEND MESSAGE RESULT"
    private val CREATE_DOCUMENT_TAG = "CREATE DOCUMENT RESULT"
    private val conversationsCollection = FirebaseManager.firestore.collection("conversations")

    private fun generateDocumentRef(Id1: String?, Id2: String?): String {
        return "${Id1}_${Id2}"
    }

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
                                            Log.d(MESSAGE_SEND_TAG, "Error saving message to new conversation: $it")
                                        }
                                    Log.d(CREATE_DOCUMENT_TAG, "Document created with ID: $documentId")
                                }
                                .addOnFailureListener { e ->
                                    Log.d(CREATE_DOCUMENT_TAG, "Error creating document: $e")
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
                                    Log.d(MESSAGE_SEND_TAG, "Error adding message to existing conversation: $e")
                                }
                        }

                    } else {
                        // Handle the error case
                        Log.e("NOT_FOUND", "Error getting user conversation for sending message: ${task.exception}")
                    }
                }
            Result.Success("message sent successfully!")
        } catch (e: Exception) {
            Result.Error(e)
        }
    }


    suspend fun getConversation(senderId: String, receiverId: String): MutableList<Message> = withContext(Dispatchers.IO) {
        val messagesList = mutableListOf<Message>()

        try {
            // Generate unique ID for conversation document - then use both users' ID
            val documentConversationRef1 = generateDocumentRef(senderId, receiverId)
            val documentConversationRef2 = generateDocumentRef(receiverId, senderId)

            val docRef1 = conversationsCollection.document(documentConversationRef1).collection("messages")
            val docRef2 = conversationsCollection.document(documentConversationRef2).collection("messages")

            val getDocRef1 = docRef1.get().await()
            val getDocRef2 = docRef2.get().await()

            val docToDisplay = when {
                !getDocRef1.isEmpty -> getDocRef1
                !getDocRef2.isEmpty -> getDocRef2
                else -> null
            }

            docToDisplay?.let { snapshot ->
                for (document in snapshot.documents) {
                    val message = document.toObject(Message::class.java)
                    message?.let {
                        messagesList.add(it)
                    }
                }
            }

            messagesList.sortBy { it.timestamp }
        } catch (e: Exception) {
            messagesList.clear()
            Result.Error(e)
        }

        messagesList
    }


    // get conversations with match id
    suspend fun getAllUsersConversations(): List<DocumentSnapshot> {
        val conversationsDocLists = mutableListOf<DocumentSnapshot>()
        val userId = FirebaseManager.auth.uid

        fun matchDocumentParts(documentId: String, searchPart: String): Boolean {
            val parts = documentId.split("_")
            val partBeforeUnderscore = parts.getOrNull(0)
            val partAfterUnderscore = parts.getOrNull(1)
            return partBeforeUnderscore == searchPart || partAfterUnderscore == searchPart
        }

        try {
            val querySnapshot = conversationsCollection
                // Add additional filters or conditions if needed
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val messagesCollection = document.reference.collection("messages")
                val latestMessageQuery = messagesCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)

                val latestMessageSnapshot = latestMessageQuery.get().await()

                if (!latestMessageSnapshot.isEmpty) {
                    val latestMessageDocument = latestMessageSnapshot.documents[0]
                    conversationsDocLists.add(latestMessageDocument)
                }
            }

            if (conversationsDocLists.isNotEmpty()) {
                return conversationsDocLists
            } else {
                throw Exception("No conversations found")
            }
        } catch (e: Exception) {
            // Handle the exception or log the error message
            e.printStackTrace()
            throw e
        }
    }

}