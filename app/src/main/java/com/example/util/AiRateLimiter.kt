package com.example.util

/**
 * How often one person may ask the model something.
 *
 * There was no limit of any kind. The question box is free text with a send
 * button, the Rashifal screen has a fetch button per sign, and every press went
 * straight to Firebase AI Logic. Nothing stopped a held finger, a stuck retry
 * loop, or simple curiosity from issuing hundreds of calls — and there is no
 * backend here, so the bill and the quota are the developer's directly. A quota
 * exhausted by one enthusiastic user takes the feature down for everyone else.
 *
 * Two limits, because they catch different things:
 *
 *  - a minimum gap, which stops double-taps and hammering;
 *  - a rolling hourly cap, which bounds what a single session can cost.
 *
 * Deliberately in-memory and per-process. Persisting it would mean a user could
 * be told to wait by an app they just opened, which is a worse experience than
 * the abuse it would prevent; a process restart is not the attack this is for.
 *
 * The clock is injected so the behaviour can be tested without sleeping.
 */
class AiRateLimiter(
    private val minGapMs: Long = DEFAULT_MIN_GAP_MS,
    private val maxPerHour: Int = DEFAULT_MAX_PER_HOUR,
    private val now: () -> Long = System::currentTimeMillis
) {

    companion object {
        /** Enough to stop a double-tap without being noticeable when reading. */
        const val DEFAULT_MIN_GAP_MS = 3_000L

        /** A real session of questions fits well inside this. */
        const val DEFAULT_MAX_PER_HOUR = 20

        const val ONE_HOUR_MS = 60L * 60L * 1000L
    }

    sealed interface Decision {
        /** Go ahead; the call has been counted. */
        object Allow : Decision

        /** Asked again too quickly. [waitMs] is how long is left. */
        data class TooSoon(val waitMs: Long) : Decision

        /** The hourly allowance is spent. [retryAfterMs] is until the oldest call ages out. */
        data class HourlyCapReached(val retryAfterMs: Long) : Decision
    }

    private val calls = ArrayDeque<Long>()

    /**
     * Asks for permission and, when granted, counts the call. Callers that do
     * not go on to make the request will simply have spent one of the hour's
     * allowance, which is the safe direction to be wrong in.
     */
    @Synchronized
    fun tryAcquire(): Decision {
        val t = now()

        while (calls.isNotEmpty() && t - calls.first() >= ONE_HOUR_MS) {
            calls.removeFirst()
        }

        val last = calls.lastOrNull()
        if (last != null && t - last < minGapMs) {
            return Decision.TooSoon(minGapMs - (t - last))
        }

        if (calls.size >= maxPerHour) {
            val oldest = calls.first()
            return Decision.HourlyCapReached(ONE_HOUR_MS - (t - oldest))
        }

        calls.addLast(t)
        return Decision.Allow
    }

    /** For a message to the user: whole minutes, rounded up, never zero. */
    fun minutesFrom(ms: Long): Int = ((ms + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
}
