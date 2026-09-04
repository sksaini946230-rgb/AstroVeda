package com.example.data

import com.example.data.local.KundaliEntity
import com.example.data.local.ProfileMerge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The regression tests for the cloud-sync data loss.
 *
 * Each of these fails against the rule this replaced
 * (`it.id == cloud.id || (name matches && dateOfBirth matches)`), which is the
 * point: the defect destroyed users' saved birth profiles permanently and would
 * have been caught by any one of them.
 */
class ProfileMergeTest {

    private fun profile(
        uuid: String,
        id: Long = 0,
        name: String = "Test",
        dob: String = "1994-08-25",
        tob: String = "14:15"
    ) = KundaliEntity(
        id = id,
        uuid = uuid,
        name = name,
        gender = "MALE",
        dateOfBirth = dob,
        timeOfBirth = tob,
        placeOfBirth = "Jaipur",
        latitude = 26.9124,
        longitude = 75.7873
    )

    @Test
    fun `two devices whose local ids collide both keep their profiles`() {
        // Phone A saved "राम" and got Room id 1. Phone B saved "श्याम", also id 1.
        // The old rule compared ids, saw a match, skipped the restore — and the
        // next upload overwrote राम in the cloud, permanently.
        val cloud = listOf(profile(uuid = "uuid-ram", id = 1, name = "राम"))
        val local = listOf(profile(uuid = "uuid-shyam", id = 1, name = "श्याम"))

        val restore = ProfileMerge.profilesToRestore(cloud, local)

        assertEquals(1, restore.size)
        assertEquals("राम", restore.first().name)
    }

    @Test
    fun `twins sharing a name and a date of birth are both kept`() {
        // The old rule's second half matched on name + dateOfBirth, so one twin
        // was silently dropped on every sync. They differ only by birth time.
        val cloud = listOf(
            profile(uuid = "uuid-twin-a", name = "आरव", dob = "2015-03-04", tob = "04:10"),
            profile(uuid = "uuid-twin-b", name = "आरव", dob = "2015-03-04", tob = "04:35")
        )
        val local = emptyList<KundaliEntity>()

        val restore = ProfileMerge.profilesToRestore(cloud, local)

        assertEquals(2, restore.size)
    }

    @Test
    fun `a profile already held locally is not restored twice`() {
        val shared = profile(uuid = "uuid-1", name = "सीता")
        // Same identity, different local Room id — an ordinary post-restore state.
        val local = listOf(shared.copy(id = 42))

        val restore = ProfileMerge.profilesToRestore(listOf(shared), local)

        assertTrue(restore.isEmpty())
    }

    @Test
    fun `an empty cloud restores nothing`() {
        val local = listOf(profile(uuid = "uuid-1"))
        assertTrue(ProfileMerge.profilesToRestore(emptyList(), local).isEmpty())
    }

    @Test
    fun `a first sign-in on a fresh device restores everything`() {
        val cloud = listOf(profile(uuid = "a"), profile(uuid = "b"), profile(uuid = "c"))
        assertEquals(3, ProfileMerge.profilesToRestore(cloud, emptyList()).size)
    }

    @Test
    fun `every profile gets a distinct uuid by default`() {
        val uuids = (1..500).map {
            KundaliEntity(
                name = "Same", gender = "MALE", dateOfBirth = "1994-08-25",
                timeOfBirth = "14:15", placeOfBirth = "Jaipur",
                latitude = 26.9124, longitude = 75.7873
            ).uuid
        }
        assertEquals(500, uuids.toSet().size)
    }
}
