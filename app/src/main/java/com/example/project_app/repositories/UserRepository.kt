package com.example.project_app.repositories

import android.net.Uri
import android.util.Log
import com.example.project_app.FirebaseManager
import com.example.project_app.data_classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.example.project_app.utils.Result
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class UserRepository(private val auth: FirebaseAuth) {
    private val changeUserInfo = "Change_user_info"
    private val usersCollection = FirebaseManager.firestore.collection("users")
    private val MESSAGE_PROPOSITION = "MESSAGE PROPOSITION"

    fun getUserProfile() {
        val user = auth.currentUser

        user?.let {
            val name = it.displayName
            val email = it.email
            val photoUrl = it.photoUrl

            val uid = it.uid
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun updateName(name: String) : Result<FirebaseUser> {
        return try {
            val user = auth.currentUser
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }

            user!!.updateProfile(profileUpdates)
                .addOnCompleteListener { updateProfileTask ->
                    if (updateProfileTask.isSuccessful) {
                        val userDocRef = usersCollection.document(user.uid)
                        val updatedData = mapOf("name" to name)

                        userDocRef.set(updatedData, SetOptions.merge())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(changeUserInfo, "Name updated in Firestore")
                                } else {
                                    Log.e(changeUserInfo, "Failed to update name in Firestore", task.exception)
                                }
                            }
                    } else {
                        Log.e(changeUserInfo, "Failed to update name", updateProfileTask.exception)
                    }
                }.await()

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateEmail(newEmail: String) : Result<FirebaseUser> {
        return try {
            val user = auth.currentUser

            user!!.updateEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(changeUserInfo, "User e-mail updated")
                    }
                }.await()

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updatePassword(newPassword: String) : Result<FirebaseUser> {
        return try {
            val user = auth.currentUser

            user!!.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(changeUserInfo, "User password updated")
                    }
                }.await()

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateProfileUrl(profileUrl: String) : Result<FirebaseUser> {
        return try {
            val user = auth.currentUser
            val profileUpdates = userProfileChangeRequest {
                photoUri = Uri.parse(profileUrl)
            }
            user!!.updateProfile(profileUpdates)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(changeUserInfo, "Profile picture updated")
                    }
                }.await()
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

//  to mi nie dzialalo
//    suspend fun updateProfilePic(user: FirebaseUser) : Result<FirebaseUser> {
//        val profilePicFileName = "profile_pictures/${user.uid}.jpg"
//        val profilePicRef = FirebaseManager.firebaseStorage.reference.child(profilePicFileName)
//
//        val downloadTask = profilePicRef.putFile(user.profPictureUrl!!.toUri()).await()
//
//        if (downloadTask.task.isSuccessful) {
//            val downloadUrl = profilePicRef.downloadUrl.await()
//
//            userDocRef.update("profilePicUrl", downloadUrl.toString()).await()
//            Result.Success("Profile picture successfully added to Firestore")
//        } else {
//            Result.Error(downloadTask.task.exception ?: Exception("Error uploading profile picture to Firebase Storage"))
//        }
//    }

    suspend fun searchUsers(query: String): List<User> {
        val currentUserId = FirebaseManager.auth.currentUser?.uid

        fun getIndexValue(query: String): Int? {
            val indexPart = query.substringBefore(" ").trim()
            return indexPart.toIntOrNull()
        }

        fun getNameQuery(query: String): String {
            val namePart = query.substringAfter(" ").trim()
            return namePart.toLowerCase()
        }

        val indexValue = getIndexValue(query)
        val indexQuery = usersCollection
            .whereEqualTo("randomIndex", indexValue)
            .get()
            .await()

        val nameQuery = getNameQuery(query)
        val resultUsers = mutableListOf<User>()

        for (document in indexQuery.documents) {
            val user = document.toObject(User::class.java)
            if (user != null && user.name!!.toLowerCase().startsWith(nameQuery) && document.id != currentUserId) {
                user.documentId = document.id
                resultUsers.add(user)
            }
        }

        if (resultUsers.isNotEmpty()) {
            return resultUsers
        } else {
            throw Exception("No matching users found")
        }
    }

    suspend fun saveMessageProposition(messageContent: String) = withContext(Dispatchers.IO) {
        try {
            val userId = FirebaseManager.auth.uid

            if (userId != null) {
                val userDocumentRef = usersCollection.document(userId)

                userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val messagePropositions = documentSnapshot.get("messagePropositions") as? List<String>
                        val updatedMessagePropositions = messagePropositions?.toMutableList()?.apply {
                            add(messageContent)
                        } ?: mutableListOf(messageContent)

                        userDocumentRef.update("messagePropositions", updatedMessagePropositions)
                            .addOnSuccessListener {
                                Log.e(MESSAGE_PROPOSITION, "Updated existing propositions successfully")
                            }
                            .addOnFailureListener { exception ->
                                Log.e(MESSAGE_PROPOSITION, "Error updating messages propositions: $exception")
                            }
                    } else {
                        val messagePropositions = listOf(messageContent)

                        userDocumentRef.set(mapOf("messagePropositions" to messagePropositions))
                            .addOnSuccessListener {
                                Log.e(MESSAGE_PROPOSITION, "Propositions created successfully")
                            }
                            .addOnFailureListener { exception ->
                                Log.e(MESSAGE_PROPOSITION, "Error creating messages propositions: $exception")
                            }
                    }
                }
            }

            Result.Success("message sent successfully!")
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

}