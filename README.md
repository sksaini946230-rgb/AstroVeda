# Revati — Kundli & Panchang

An Android Vedic astrology app for India: daily Panchang, Rashifal, birth charts
(Kundali), Guna Milan, Muhurat and numerology — in Hindi and English, with the
astronomy computed on the device rather than fetched from a server.

**On Google Play:** [Revati : Kundli & Panchang](https://play.google.com/store/apps/details?id=com.aistudio.astroveda.kpvqzm)

---

## What it does

| | |
|---|---|
| **Panchang** | Tithi, Nakshatra, Yoga, Karana, Vara, sunrise/sunset, moonrise/moonset, Rahu Kaal, Gulika, Yamaganda, Abhijit, Brahma Muhurta, Choghadiya |
| **Rashifal** | Daily, weekly and monthly for all twelve signs |
| **Kundali** | Lagna, planetary positions, North and South Indian charts, Vimshottari Dasha with antardashas |
| **Guna Milan** | Ashtakoot 36-guna matching with Mangal, Nadi and Bhakoot dosha |
| **Muhurat** | Choghadiya and event-specific auspicious windows |
| **Calendar** | Month grid with festivals, fasts and their puja vidhi |
| **Numerology** | Moolank, Bhagyank and name number |

Panchang and Kundali work with no internet connection.

## How the astronomy works

Solar and lunar positions come from Meeus, *Astronomical Algorithms* (2nd ed.) —
chapter 25 for the Sun, chapter 47 with tables 47.A/47.B for the Moon, chapter 22
for nutation and obliquity — converted to sidereal with the Lahiri
(Chitrapaksha) ayanamsa. Planets use Schlyter's elements with Jupiter and Saturn
perturbations. Rise and set times are solved by scan plus bisection against the
true altitude, including parallax and semi-diameter.

Accuracy is pinned by `EphemerisAccuracyTest`, which checks eight golden
positions to 0.02°. The whole engine lives in `app/src/main/java/com/example/astro/`
and has no Android dependencies, so it is unit-testable in isolation.

## Building

Requires Android Studio and a JDK 17+.

1. Clone the repository and open it in Android Studio.
2. Copy `.env.example` to `.env` and fill in the values. The AdMob and Google
   client IDs are needed for ads and sign-in; the app builds without them, using
   the placeholders.
3. `./gradlew installDebug` with a device connected, or run from the IDE.

`google-services.json` is committed and points at the `astroveda-7126b` Firebase
project.

### Checks

```bash
./gradlew testDebugUnitTest lintDebug
```

Both must pass. `lintDebug` is not optional here — it is what catches `java.time`
usage, which would crash on Android 7 (minSdk 24, no core library desugaring).

## Stack

Kotlin · Jetpack Compose · Material 3 · Room · WorkManager · Firebase (Auth,
Firestore, Crashlytics, App Check, AI Logic) · Google Play Billing · AdMob with
UMP consent.

## Contributing notes

`CLAUDE.md` in the repository root is the working handover document: identity
quirks (the package name and the app name deliberately disagree), the bugs that
have shipped before, and what is still unfinished. Read it before changing
anything.

## Licence and data

Astrological readings are presented as traditional interpretation, not
prediction. See `docs/PRIVACY_POLICY.md` for what the app collects and
`docs/ACCOUNT_DELETION.md` for how to delete it.
