package com.example.supply7.data

import com.example.supply7.utils.EmailValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (!EmailValidator.isValid(email)) {
                return Result.failure(Exception("Invalid email domain. Must be @std.yeditepe.edu.tr"))
            }
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Sync user data to Firestore on login to fix existing users
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to (user.email ?: ""),
                )
                if (!user.displayName.isNullOrBlank()) {
                    userData["displayName"] = user.displayName!!
                }
                
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.uid)
                    .set(userData, com.google.firebase.firestore.SetOptions.merge())
                
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed, user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            if (!EmailValidator.isValid(email)) {
                return Result.failure(Exception("Invalid email domain. Must be @std.yeditepe.edu.tr"))
            }
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Update Display Name in Auth
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                
                // Save user data to Firestore
                val userData = mapOf(
                    "uid" to user.uid,
                    "displayName" to name,
                    "email" to email
                )
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.uid)
                    .set(userData, com.google.firebase.firestore.SetOptions.merge())
                    .await()
                
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed, user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
             if (!EmailValidator.isValid(email)) {
                return Result.failure(Exception("Invalid email domain. Must be @std.yeditepe.edu.tr"))
            }
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
