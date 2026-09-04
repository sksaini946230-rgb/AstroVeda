package com.example.data.local

import com.example.data.model.PlanetPosition

/**
 * Serialises the planet list into the panchang cache row.
 *
 * Deliberately dependency-free. The app carried Moshi and Retrofit purely as
 * leftovers from an old REST path and they are gone now, and kotlinx.serialization
 * was never a dependency — so this uses two ASCII separator characters that
 * cannot occur in planet names, rashi names or a formatted degree. Unit tests can
 * exercise it without Robolectric, which org.json would have required.
 */
internal object PlanetsCodec {

    private const val FIELD = '' // ASCII unit separator
    private const val RECORD = '' // ASCII record separator

    fun encode(planets: List<PlanetPosition>): String =
        planets.joinToString(RECORD.toString()) { p ->
            listOf(
                p.planetNameEn,
                p.planetNameHi,
                p.rashiNumber.toString(),
                p.rashiNameHi,
                p.rashiNameEn,
                p.degree.toString(),
                p.houseNumber.toString(),
                if (p.isRetrograde) "1" else "0",
                p.nakshatraHi,
                p.nakshatraEn
            ).joinToString(FIELD.toString())
        }

    /** Returns an empty list for anything it cannot read, so a bad row just misses the cache. */
    fun decode(encoded: String): List<PlanetPosition> {
        if (encoded.isBlank()) return emptyList()
        return try {
            encoded.split(RECORD).mapNotNull { record ->
                val f = record.split(FIELD)
                if (f.size != 10) return@mapNotNull null
                PlanetPosition(
                    planetNameEn = f[0],
                    planetNameHi = f[1],
                    rashiNumber = f[2].toIntOrNull() ?: return@mapNotNull null,
                    rashiNameHi = f[3],
                    rashiNameEn = f[4],
                    degree = f[5].toDoubleOrNull() ?: return@mapNotNull null,
                    houseNumber = f[6].toIntOrNull() ?: return@mapNotNull null,
                    isRetrograde = f[7] == "1",
                    nakshatraHi = f[8],
                    nakshatraEn = f[9]
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// PlanetsCodec is internal to keep it out of the app's public surface; these two
// give the unit test a way in without widening it.
internal fun testEncode(planets: List<PlanetPosition>): String = PlanetsCodec.encode(planets)
internal fun testDecode(encoded: String): List<PlanetPosition> = PlanetsCodec.decode(encoded)
