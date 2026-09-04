package com.example.data.local

/**
 * Deciding which cloud profiles are missing locally.
 *
 * This lived inline inside MainViewModel.syncCloudAndLocalProfiles, where it
 * could not be tested without an Application, a Room database and a Firebase
 * account — so it never was. It is the rule that silently destroyed users' saved
 * birth profiles, and a single unit test over two profiles would have caught it.
 * It is a pure function now, and ProfileMergeTest covers exactly that case.
 */
object ProfileMerge {

    /**
     * Cloud profiles that are not already present locally, matched on
     * [KundaliEntity.uuid].
     *
     * The rule used to be `it.id == cloud.id || (name matches && dateOfBirth
     * matches)`, and both halves lost data:
     *
     *  - `id` is a Room autoGenerate key, so it restarts at 1 on every device.
     *    Two phones on one account each had a profile with id 1, so each read the
     *    other's cloud copy as "already here", skipped restoring it, and then
     *    overwrote it on the next upload. The overwritten profile was gone.
     *  - name + date of birth matched for twins, who share both, so one of the
     *    pair was silently dropped on every sync.
     */
    fun profilesToRestore(
        cloudProfiles: List<KundaliEntity>,
        localProfiles: List<KundaliEntity>
    ): List<KundaliEntity> {
        val localUuids = localProfiles.mapTo(HashSet()) { it.uuid }
        return cloudProfiles.filter { it.uuid !in localUuids }
    }
}
