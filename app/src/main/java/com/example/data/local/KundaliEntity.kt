package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A saved birth profile.
 *
 * [uuid] is the identity that travels. [id] is a Room autoGenerate key, so it
 * starts at 1 on every device — two phones signed into the same account both
 * produce a profile with id 1, and the cloud backup used to key its Firestore
 * document on exactly that. Phone B's "Shyam" then overwrote phone A's "Ram" at
 * users/{uid}/kundali_profiles/1, and Ram was gone for good. The sync also read
 * the collision as "already present locally" and skipped the restore, so nothing
 * ever brought Ram back.
 *
 * Firestore documents are keyed on [uuid] now, and the merge in
 * MainViewModel.syncCloudAndLocalProfiles matches on [uuid] alone. The old
 * fallback of matching on name + date of birth is gone too: it silently dropped
 * one of a pair of twins.
 */
@Entity(
    tableName = "saved_kundali_profiles",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["createdAt"])
    ]
)
data class KundaliEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Stable across devices and reinstalls; the key the cloud copy is stored under. */
    val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val gender: String, // "MALE" or "FEMALE"
    val dateOfBirth: String, // YYYY-MM-DD
    val timeOfBirth: String, // HH:MM
    val placeOfBirth: String,
    val latitude: Double,
    val longitude: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
