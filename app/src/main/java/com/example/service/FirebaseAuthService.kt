package com.example.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.util.LanguageManager
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

            val defaultClientId = app.revati.jyotish.BuildConfig.GOOGLE_WEB_CLIENT_ID
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
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            // No Google account is set up on this device, or the user has none that
            // matches. This is not an error to log as a failure — it needs a
            // different message than "sign-in failed", which is what it used to get.
            Log.i("FirebaseAuthService", "No Google credential available on this device")
            Result.failure(
                Exception(
                    LanguageManager.getString(
                        "इस डिवाइस पर कोई Google खाता नहीं मिला। कृपया Settings में Google खाता जोड़ें और दोबारा प्रयास करें।",
                        "No Google account found on this device. Add one in Settings and try again."
                    )
                )
            )
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Log.i("FirebaseAuthService", "Sign-in cancelled by user")
            Result.failure(Exception(LanguageManager.getString("साइन-इन रद्द किया गया।", "Sign-in cancelled.")))
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    /**
     * Email sign-up. Google is not an option for everyone — a phone without a
     * Google account, or a user who simply does not want to link one, still has
     * to get past the sign-in gate.
     */
    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user
        if (user == null) {
            Result.failure(Exception(LanguageManager.getString(
                "खाता नहीं बन सका। कृपया पुनः प्रयास करें।",
                "Could not create the account. Please try again."
            )))
        } else {
            if (name.isNotBlank()) {
                runCatching {
                    user.updateProfile(
                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim())
                            .build()
                    ).await()
                }
            }
            Result.success(user)
        }
    } catch (e: Exception) {
        Log.e("FirebaseAuthService", "Email sign-up failed", e)
        Result.failure(Exception(readableAuthError(e)))
    }

    /** Email sign-in for an account created with [signUpWithEmail]. */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = result.user
        if (user == null) {
            Result.failure(Exception(LanguageManager.getString(
                "साइन-इन नहीं हो सका। कृपया पुनः प्रयास करें।",
                "Could not sign in. Please try again."
            )))
        } else {
            Result.success(user)
        }
    } catch (e: Exception) {
        Log.e("FirebaseAuthService", "Email sign-in failed", e)
        Result.failure(Exception(readableAuthError(e)))
    }

    /** Sends a password-reset mail; the address may or may not have an account. */
    suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email.trim()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseAuthService", "Password reset failed", e)
        Result.failure(Exception(readableAuthError(e)))
    }

    /**
     * Firebase's own messages are English-only and written for developers
     * ("The password is invalid or the user does not have a password"). These
     * say what went wrong and what to do about it, in the reader's language.
     */
    private fun readableAuthError(e: Exception): String = when (e) {
        is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> LanguageManager.getString(
            "पासवर्ड कम से कम 6 अक्षर का होना चाहिए।",
            "Use at least 6 characters for the password."
        )
        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> LanguageManager.getString(
            "ईमेल या पासवर्ड सही नहीं है।",
            "That email or password is not right."
        )
        is com.google.firebase.auth.FirebaseAuthUserCollisionException -> LanguageManager.getString(
            "इस ईमेल से खाता पहले से मौजूद है। साइन-इन करें।",
            "An account already exists for this email. Sign in instead."
        )
        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> LanguageManager.getString(
            "इस ईमेल से कोई खाता नहीं मिला। पहले साइन-अप करें।",
            "No account found for this email. Sign up first."
        )
        is com.google.firebase.FirebaseNetworkException -> LanguageManager.getString(
            "इंटरनेट कनेक्शन नहीं है। जुड़ने के बाद पुनः प्रयास करें।",
            "No internet connection. Try again once you are back online."
        )
        else -> e.message ?: LanguageManager.getString(
            "कुछ गड़बड़ हो गई। कृपया पुनः प्रयास करें।",
            "Something went wrong. Please try again."
        )
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
            Result.failure(Exception(LanguageManager.getString(
                "सुरक्षा के लिए, खाता हटाने से पहले कृपया पुनः साइन-इन करें।",
                "For security, please sign in again before deleting your account."
            )))
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Failed to delete user data and account", e)
            Result.failure(e)
        }
    }
}
