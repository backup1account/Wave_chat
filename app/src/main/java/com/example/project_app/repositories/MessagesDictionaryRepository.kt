package com.example.project_app.repositories

import android.util.Log
import com.example.project_app.FirebaseManager
import com.example.project_app.data_classes.MessagesDictionary
import com.example.project_app.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MessagesDictionaryRepository(val auth: FirebaseAuth) {

    private val collection = FirebaseManager.firestore.collection("messagesDictionary")
    private val SAVE_MSG_D_TAG = "Save message dictionary"

    private val max_messages_number = 8

    suspend fun saveMessageToDictionary(messageContent: String) = withContext(Dispatchers.IO) {
        try {
            val uId = auth.uid
            val query = collection.whereEqualTo("userId", uId).limit(1)

            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result?.documents

                    // if document with uId already exists
                    if (!documents.isNullOrEmpty()) {
                        val documentId = documents[0].id

                        collection.document(documentId).get()
                            .addOnSuccessListener { documentSnapshot ->
                            val existingMessages = documentSnapshot.get("messages") as? MutableList<String>

                            if ( (existingMessages != null) && (existingMessages.size <= max_messages_number) ) {
                                existingMessages.add(messageContent)

                                collection.document(documentId)
                                    .update("messages", existingMessages)
                                    .addOnSuccessListener {
                                        Log.d(SAVE_MSG_D_TAG, "Message list updated successfully")
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(SAVE_MSG_D_TAG, "Error updating message list: $exception")
                                    }
                            }
                        }
                    }
                    // if document with uId does not exist
                    else {
                        val dictionaryMessage = MessagesDictionary(
                            userId = uId,
                            messages = mutableListOf(messageContent)
                        )

                        collection.add(dictionaryMessage)
                            .addOnSuccessListener {
                                Log.d(SAVE_MSG_D_TAG, "Message added to new document successfully")
                            }
                            .addOnFailureListener { exception ->
                                Log.e(SAVE_MSG_D_TAG, "Error adding new dictionary message: $exception")
                            }
                    }
                }
                else {
                    Log.e("NOT_FOUND", "Error getting user from message dictionary: ${task.exception}")
                }
            }

            Result.Success("Dictionary message saved successfully!")
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getDictionaryMessages(): List<String> = withContext(Dispatchers.IO) {
        val messagesList = mutableListOf<String>()

        try {
            val uId = auth.uid
            val query = collection.whereEqualTo("userId", uId).limit(1)
            val querySnapshot = query.get().await()

            if (!querySnapshot.isEmpty) {
                val documentSnapshot = querySnapshot.documents[0]
                val messages = documentSnapshot.get("messages") as? List<String>

                if (messages != null) {
                    for (message in messages) {
                        messagesList.add(message)
                    }
                }
            } else {
                Log.e("NOT_FOUND", "Error: document with the field does not exist")
            }
        } catch (e: Exception) {
            messagesList.clear()
            Result.Error(e)
        }

        messagesList
    }

}