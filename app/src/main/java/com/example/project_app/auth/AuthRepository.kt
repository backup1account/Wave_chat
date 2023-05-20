package com.example.project_app.auth

import androidx.core.net.toUri
import com.example.project_app.FirebaseManager
import com.example.project_app.auth.data_classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.project_app.utils.Result
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlin.Exception


class AuthRepository(private val auth: FirebaseAuth) {
    private val usersCollection = FirebaseManager.firestore.collection("users")
    private val storageRef = FirebaseManager.firebaseStorage.reference

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)
        } catch(e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun addUserToDatabase(user: User): Result<String> {
        return try {
            val userId = auth.currentUser?.uid?: return Result.Error(Exception("User ID is null"))

            val userDocRef = usersCollection.document(userId)

            userDocRef.set(user, SetOptions.merge()).await()

            val profilePicFileName = "profile_pictures/$userId.jpg"
            val profilePicRef = storageRef.child(profilePicFileName)
            val downloadTask = profilePicRef.putFile(user.profPictureUrl!!.toUri()).await()

            if (downloadTask.task.isSuccessful) {
                val downloadUrl = profilePicRef.downloadUrl.await()

                userDocRef.update("profilePicUrl", downloadUrl.toString()).await()
                Result.Success("Profile picture successfully added to Firestore")
            } else {
                Result.Error(downloadTask.task.exception ?: Exception(
                    "Error uploading profile picture to Firebase Storage"
                ))
            }
        } catch(e: Exception) {
            Result.Error(e)
        }
    }

}