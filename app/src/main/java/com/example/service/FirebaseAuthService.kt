package com.example.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.data.local.KundaliEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUser: FirebaseUser?
        get() = try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }

    suspend fun signInWithGoogle(context: Context, webClientId: String = ""): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val defaultClientId = com.example.BuildConfig.GOOGLE_WEB_CLIENT_ID
            val clientId = if (webClientId.isNotEmpty()) webClientId else defaultClientId

            if (clientId.isEmpty()) {
                return Result.failure(Exception("Google Web Client ID is not configured. Please add GOOGLE_WEB_CLIENT_ID to .env or strings.xml"))
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Firebase user is null after sign in"))
                }
            } else {
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign out error", e)
        }
    }

    suspend fun backupProfilesToCloud(profiles: List<KundaliEntity>): Result<Int> {
        val user = currentUser ?: return Result.failure(Exception("User not authenticated with Firebase"))
        return try {
            val batch = firestore.batch()
            val userProfilesRef = firestore.collection("users")
                .document(user.uid)
                .collection("kundali_profiles")

            profiles.forEach { profile ->
                val docRef = userProfilesRef.document(profile.id.toString())
                val data = mapOf(
                    "id" to profile.id,
                    "name" to profile.name,
                    "gender" to profile.gender,
                    "dateOfBirth" to profile.dateOfBirth,
                    "timeOfBirth" to profile.timeOfBirth,
                    "placeOfBirth" to profile.placeOfBirth,
                    "latitude" to profile.latitude,
                    "longitude" to profile.longitude,
                    "notes" to profile.notes,
                    "createdAt" to profile.createdAt
                )
                batch.set(docRef, data)
            }
            batch.commit().await()
            Result.success(profiles.size)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Failed to backup profiles to cloud", e)
            Result.failure(e)
        }
    }

    suspend fun restoreProfilesFromCloud(): Result<List<KundaliEntity>> {
        val user = currentUser ?: return Result.failure(Exception("User not authenticated with Firebase"))
        return try {
            val snapshot = firestore.collection("users")
                .document(user.uid)
                .collection("kundali_profiles")
                .get()
                .await()

            val profiles = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                KundaliEntity(
                    id = doc.getLong("id") ?: 0L,
                    name = name,
                    gender = doc.getString("gender") ?: "MALE",
                    dateOfBirth = doc.getString("dateOfBirth") ?: "",
                    timeOfBirth = doc.getString("timeOfBirth") ?: "",
                    placeOfBirth = doc.getString("placeOfBirth") ?: "",
                    latitude = doc.getDouble("latitude") ?: 28.6139,
                    longitude = doc.getDouble("longitude") ?: 77.2090,
                    notes = doc.getString("notes") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
            Result.success(profiles)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Failed to restore profiles from cloud", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUserDataAndAccount(): Result<Unit> {
        val user = currentUser ?: return Result.failure(Exception("User not authenticated with Firebase"))
        return try {
            // 1. Delete all Firestore cloud backup data for this user
            val userProfilesRef = firestore.collection("users")
                .document(user.uid)
                .collection("kundali_profiles")
                .get()
                .await()

            val batch = firestore.batch()
            userProfilesRef.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.delete(firestore.collection("users").document(user.uid))
            batch.commit().await()

            // 2. Delete the Firebase Auth user account
            user.delete().await()
            Result.success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            Log.e("FirebaseAuthService", "Re-authentication required before deleting account", e)
            Result.failure(Exception("सुरक्षा के लिए, खाता हटाने से पहले कृपया पुनः साइन-इन करें। (For security, please sign in again before deleting your account.)"))
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Failed to delete user data and account", e)
            Result.failure(e)
        }
    }
}
