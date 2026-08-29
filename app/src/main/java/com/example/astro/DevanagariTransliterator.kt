package com.example.astro

/**
 * Devanagari to Latin, for numerology.
 *
 * The Chaldean letter table only covers A-Z, and every other character fell to
 * `else -> 0`. So a name typed in Devanagari — which is what a Hindi-first app
 * whose own placeholder reads "उदा. राहुल शर्मा" invites — summed to zero and
 * came out as Name Number 1 every single time, for everyone.
 *
 * Rather than invent a second numerology table for Devanagari, names are
 * transliterated and run through the same verified Chaldean map. That gives the
 * property you actually want: "राहुल" and "Rahul" produce the same number.
 *
 * Schwa deletion is applied — a consonant with no following vowel sign gets an
 * implicit 'a' unless it ends the word, so कमल reads "kamal" and not "kamala",
 * matching how the name is spelled in Latin.
 */
object DevanagariTransliterator {

    private val CONSONANTS = mapOf(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "n",
        'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ञ' to "n",
        'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n",
        'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m",
        'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v", 'ळ' to "l",
        'श' to "sh", 'ष' to "sh", 'स' to "s", 'ह' to "h"
    )

    /**
     * Base consonant + nukta, for names of Persian and Arabic origin.
     * These are two code points, not one character, so they cannot live in the
     * map above.
     */
    private val NUKTA_FORMS = mapOf(
        'क' to "q", 'ख' to "kh", 'ग' to "g", 'ज' to "z",
        'ड' to "r", 'ढ' to "rh", 'फ' to "f"
    )

    /** Independent vowels. */
    private val VOWELS = mapOf(
        'अ' to "a", 'आ' to "a", 'इ' to "i", 'ई' to "i", 'उ' to "u", 'ऊ' to "u",
        'ऋ' to "ri", 'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au"
    )

    /** Dependent vowel signs (matras). */
    private val MATRAS = mapOf(
        'ा' to "a", 'ि' to "i", 'ी' to "i", 'ु' to "u", 'ू' to "u",
        'ृ' to "ri", 'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au"
    )

    private const val VIRAMA = '्'      // halant — suppresses the implicit vowel
    private const val ANUSVARA = 'ं'    // ं
    private const val CHANDRABINDU = 'ँ'
    private const val VISARGA = 'ः'     // ः
    private const val NUKTA = '़'

    /** True if [text] contains any Devanagari at all. */
    fun containsDevanagari(text: String): Boolean =
        text.any { it in 'ऀ'..'ॿ' }

    /**
     * Transliterates Devanagari in [text] to Latin, leaving other characters
     * (including Latin) untouched so mixed names work.
     */
    fun transliterate(text: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < text.length) {
            val ch = text[i]

            // A nukta follows its base consonant and changes the sound.
            val hasNukta = i + 1 < text.length && text[i + 1] == NUKTA
            val consonant = if (hasNukta) {
                NUKTA_FORMS[ch] ?: CONSONANTS[ch]
            } else {
                CONSONANTS[ch]
            }

            when {
                consonant != null -> {
                    out.append(consonant)
                    if (hasNukta) i++   // consume the nukta
                    val next = text.getOrNull(i + 1)
                    when {
                        next == VIRAMA -> i++                       // no vowel, skip the halant
                        next != null && MATRAS.containsKey(next) -> {
                            out.append(MATRAS[next])
                            i++
                        }
                        else -> {
                            // Implicit 'a', unless this consonant ends the word —
                            // Hindi drops the final schwa, so कमल is "kamal".
                            val after = text.getOrNull(i + 1)
                            val endsWord = after == null || !isDevanagari(after)
                            if (!endsWord) out.append('a')
                        }
                    }
                }
                VOWELS.containsKey(ch) -> out.append(VOWELS[ch])
                MATRAS.containsKey(ch) -> out.append(MATRAS[ch])
                ch == ANUSVARA || ch == CHANDRABINDU -> out.append('n')
                ch == VISARGA -> out.append('h')
                ch == VIRAMA || ch == NUKTA -> Unit
                ch in 'ऀ'..'ॿ' -> Unit   // any remaining Devanagari mark
                else -> out.append(ch)
            }
            i++
        }
        return out.toString()
    }

    private fun isDevanagari(c: Char) = c in 'ऀ'..'ॿ'
}
