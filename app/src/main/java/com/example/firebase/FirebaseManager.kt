package com.example.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

sealed class LinkResult {
    object Success : LinkResult()
    data class AccountAlreadyExists(val email: String?) : LinkResult()
    data class Error(val message: String) : LinkResult()
}

class FirebaseManager {
    private val TAG = "FirebaseManager"
    
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth initialization failed: ${e.message}")
            null
        }
    }
    
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseFirestore initialization failed: ${e.message}")
            null
        }
    }

    fun getCurrentUserId(): String? = auth?.currentUser?.uid
    fun getCurrentUserEmail(): String? = auth?.currentUser?.email
    fun isUserSignedIn(): Boolean = auth?.currentUser != null && auth?.currentUser?.isAnonymous == false

    suspend fun signInAnonymously(): String? {
        val authInstance = auth ?: return null
        return try {
            if (authInstance.currentUser != null) {
                return authInstance.currentUser?.uid
            }
            val result = authInstance.signInAnonymously().await()
            result.user?.uid
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign-in error: ${e.message}")
            null
        }
    }

    suspend fun saveFavorite(id: String, name: String, type: String = "BUS", subtitle: String = "") {
        val dbInstance = db ?: return
        val userId = getCurrentUserId() ?: signInAnonymously() ?: return
        val favoriteData = mapOf(
            "id" to id,
            "name" to name,
            "type" to type,
            "subtitle" to subtitle,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            dbInstance.collection("users").document(userId)
                .collection("favorites").document(id)
                .set(favoriteData).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorite: ${e.message}")
        }
    }

    suspend fun deleteFavorite(id: String) {
        val dbInstance = db ?: return
        val userId = getCurrentUserId() ?: return
        try {
            dbInstance.collection("users").document(userId)
                .collection("favorites").document(id)
                .delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting favorite: ${e.message}")
        }
    }

    fun listenToFavorites(onUpdate: (List<Map<String, Any>>) -> Unit): ListenerRegistration? {
        val dbInstance = db ?: return null
        val userId = getCurrentUserId() ?: return null
        return try {
            dbInstance.collection("users").document(userId)
                .collection("favorites")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { it.data }
                        onUpdate(list)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "SnapshotListener exception: ${e.message}")
            null
        }
    }

    suspend fun linkWithGoogle(idToken: String): LinkResult {
        val authInstance = auth ?: return LinkResult.Error("Firebase no inicializado")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            val currentUser = authInstance.currentUser
            if (currentUser == null) {
                authInstance.signInWithCredential(credential).await()
                LinkResult.Success
            } else {
                currentUser.linkWithCredential(credential).await()
                LinkResult.Success
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e(TAG, "Collision: ${e.message}")
            LinkResult.AccountAlreadyExists(e.email)
        } catch (e: Exception) {
            Log.e(TAG, "Link error: ${e.message}")
            LinkResult.Error(e.localizedMessage ?: "Error al vincular cuenta de Google")
        }
    }

    suspend fun switchToExistingGoogleAccount(idToken: String): Boolean {
        val authInstance = auth ?: return false
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            authInstance.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Switch error: ${e.message}")
            false
        }
    }

    fun signOut() {
        auth?.signOut()
    }
}
