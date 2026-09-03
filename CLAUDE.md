# Revati — Kundli & Panchang

An Android Vedic astrology app: daily Panchang, Rashifal, birth charts, Guna
Milan, Muhurat, numerology. Hindi and English throughout. Live on Google Play.

**Read this whole file before changing anything.** Several of the notes below
record bugs that shipped once already.

---

## Identity — read carefully, these disagree on purpose

| | Value |
|---|---|
| App name (users see) | **Revati** |
| Play listing | Revati : Kundli & Panchang |
| `applicationId` | `com.aistudio.astroveda.kpvqzm` |
| `namespace` (R / BuildConfig) | `app.revati.jyotish` |
| Kotlin source package | `com.example.*` |
| Firebase project | `astroveda-7126b` |
| Play developer | Msunjay Enterprises |

The app was called **AstroVeda** until Aug 2026. That name had to go: five apps
on Play already use it, two with six-figure installs. The rename reached the UI
and the namespace, but **not** the `applicationId` — the first production
release went out under the old one, and an applicationId is permanent once
published.

So: `namespace ≠ source package`. Two consequences that have already bitten:

- `R` and `BuildConfig` live at **`app.revati.jyotish`**, not `com.example`.
  Import them explicitly.
- Manifest components must be **fully qualified** (`com.example.MainActivity`),
  never `.MainActivity` — a leading dot resolves against the namespace, which is
  no longer where the classes are.

Do not "tidy" any of this into agreement. Each half is pinned by something
external.

---

## Layout

```
app/src/main/java/com/example/
  astro/      the ephemeris and all Vedic calculation — no Android imports
  data/       Room entities/DAOs, models, Firebase AI service
  service/    billing, auth, ad consent, PDF report
  ui/         Compose screens, components, theme, MainViewModel
  util/       LanguageManager, analytics, feature flags, share, image export
  widget/     home-screen widgets
  worker/     WorkManager notification jobs
```

`astro/` is pure Kotlin and unit-tested. Keep it that way — it is the one part
of this app where a wrong answer is invisible until a user notices.

---

## Things that will bite you

**The ephemeris is real and was wrong once.** `AstroMath.kt` implements Meeus
*Astronomical Algorithms* 2nd ed. — ch. 25 (solar), ch. 47 + tables 47.A/47.B
(lunar), ch. 22 (nutation/obliquity), with Lahiri ayanamsa. An earlier version
was off by enough to put **92 of 365 days on the wrong Tithi and 71 on the wrong
Nakshatra**. It was caught by dumping a year of output and diffing against an
independent implementation, not by reading the code. `EphemerisAccuracyTest`
pins eight golden values to 0.02°. If you touch `astro/`, run it, and if you
change anything about how longitudes are produced, re-do the year-long diff.

**Every ephemeris entry point takes Universal Time, not IST.** `AstroTime.kt`
does the conversion, once. Passing local time silently moves the Moon ~3°.

**Room will not wipe user data any more — keep it that way.** The database was
on `fallbackToDestructiveMigration(dropAllTables = true)`, so every schema
change silently dropped saved profiles, reports and searches. It now takes real
migrations, registered in `MIGRATIONS` in `AppDatabase.kt`, and fails loudly in
development instead. Schemas are exported to `app/schemas/`. Bumping the version
without writing a migration is now a crash, which is the intended pressure.

**Both languages, always.** Nothing user-facing may be a bare string.
`LanguageManager.getString(hi, en)` or a `*Local` extension
(`PanchangLocalized.kt`, `FestivalLocalized.kt`). A sweep in Aug 2026 fixed 170
hardcoded Hindi strings that English users were seeing — notifications, the
widget, share text, the PDF report, chart glyphs, error messages. Do not
reintroduce them. Chart planet glyphs in particular are stored as
language-neutral tokens (`"Sun"`, `"Mars|R"`) and localised at draw time by
`AstroNames.houseGlyph()`; baking the Hindi in froze charts into whatever
language drew them.

**minSdk is 24 and there is no core library desugaring.** `java.time` is off
limits in `app/`. Lint catches it; it once got as far as a crash-on-Android-7
before that. Use `java.util.Calendar` or parse strings.

**Devanagari clips without help.** Compose defaults `includeFontPadding = false`,
which eats i-matras ("पिछला दिन" → "ापछला ादन"). `Type.kt` sets
`PlatformTextStyle(includeFontPadding = true)` plus `LineHeightStyle(trim =
None)` and 1.35× line heights. Leave it alone.

**Themes come from `LocalAstroColors`.** Light and dark both real, following the
system. Never hardcode a colour that only works in one.

**Compact facts use `BentoTile`** (`ui/components/BentoPanchangGrid.kt`). Rows of
tiles need `Modifier.height(IntrinsicSize.Min)` on the Row and `.fillMaxHeight()`
on each tile, or a two-line neighbour leaves the other short. Saved Profiles is
deliberately *not* bento — those are action cards, not facts.

---

## Secrets

`.env` (gitignored, mirrored by `.env.example`) feeds BuildConfig via the secrets
plugin. **The plugin cannot emit an empty string** — an unset value must be a
sentinel, which is why `PLAY_LICENSE_KEY=NOT_CONFIGURED` rather than blank.

Keys: `ADMOB_APP_ID_ANDROID`, `ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`,
`GOOGLE_WEB_CLIENT_ID`, `PLAY_LICENSE_KEY`, `KEYSTORE_PATH`, `STORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`.

Release signing uses `upload-keystore.jks` (alias `upload`). The older
`astroveda-upload-key.jks` is dead — Play no longer accepts it.

---

## Signing and Firebase — the part that broke in production

Play re-signs every upload with **its own** app signing key. A Play-installed
app therefore presents a different certificate than the one you signed with, and
Google Sign-In matches on that certificate. Getting this wrong took Google
Sign-In down for every real user of the first release, while it worked fine on
every device tested locally.

All six fingerprints are registered on `com.aistudio.astroveda.kpvqzm` now:

| Key | SHA-1 |
|---|---|
| Play app signing (what users present) | `B7:D1:DE:10:DD:F3:5E:FD:56:27:99:FA:F6:C4:D1:D0:38:B4:5C:C5` |
| Upload key | `77:D9:C2:35:B4:EB:2D:47:8E:89:DB:27:41:A8:D7:E2:06:9A:40:81` |
| Debug | `D3:46:B8:A4:61:5B:85:6A:4F:AF:5B:F2:64:D2:01:47:2B:F2:31:E1` |

Play app signing SHA-256 (App Check / Play Integrity):
`0A:5D:A6:36:17:4B:F6:4A:88:0D:64:1A:89:2C:C6:0C:B6:B9:8F:BF:51:4A:CD:41:82:32:06:E8:D5:D8:4B:6A`

Find these under Play Console → Test and release → **App signing**. Adding a
fingerprint takes effect server-side — no new release needed.

A second Firebase app, `app.revati.jyotish`, exists from the abandoned package
rename. It is unused. Leaving it costs nothing; deleting it is fine too.

**Firestore rules live in the console, and a copy is in `firebase/firestore.rules`.**
The client only ever touches `users/{uid}/kundali_profiles/{id}`, but that
scoping is worth nothing unless the server enforces it. The file in the repo is
what the console should say; it is not deployed by any build here, so the two
can drift. If you change the data model, change both.

---

## Build and verify

```bash
./gradlew testDebugUnitTest lintDebug   # must both pass before any commit
./gradlew installDebug                  # onto a connected device
```

Screens are best checked on a real device in **both languages and both themes** —
that is how most of the UI bugs in this app were found, not by reading code.

---

## Where it stands

Live on Play, production, 100% rollout — the shipped release is `versionCode 2`
/ `versionName 1.1`. The tree is now on `versionCode 3` / `versionName 1.2`,
unreleased.

Working: on-device ephemeris, Panchang, Rashifal, Kundali, Guna Milan, Muhurat,
numerology, festivals calendar, widgets, notifications, Firestore profile sync,
Google + email sign-in, AdMob with UMP consent, App Check via Play Integrity.

Not working / not finished:

- **PRO subscription cannot exist yet.** Play Console refuses the Subscriptions
  page until a Google Payments merchant account is set up. Until then the PRO
  button leads nowhere. Product id the app queries:
  `astroveda_premium_pro_subscription`.
- **`PLAY_LICENSE_KEY` is unset**, so `PurchaseVerifier` skips signature
  checking and logs a warning. Moot until the above is done.
- Purchase verification is client-side only; a server checking tokens against
  the Play Developer API is the real fix and there is no backend. Nothing is
  purchasable until the merchant account exists, so this is not urgent — do it
  in the same pass as `PLAY_LICENSE_KEY`, not before.

Decided rather than pending:

- **Sign-in is no longer a gate.** It was `MainActivity`, `currentUser == null`
  → `AuthScreen`, in front of everything, while the Play listing said most
  features work without an account. The gate is gone: `AuthScreen` now takes an
  optional `onDismiss` and opens as a full-screen dialog from
  `MainViewModel.showAuthScreen`, reached from the Saved Profiles cloud card and
  from Settings → Account & data controls. An account buys cloud backup and
  nothing else, so an account is what it now asks for. Signing out leaves the
  user inside the app rather than bouncing them to a login screen.
- **No age gate, deliberately.** The app is not child-directed and does not
  declare children in its Play target audience. `MainActivity` already sets
  `MAX_AD_CONTENT_RATING_G`, `TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE` and
  `TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE`, which is what Play and AdMob actually
  ask for here. An age gate would be a screen in the way of nothing.

---

## Ground rules

**Tezzo is a different project.** Never touch its files, Firebase project, or
tokens. If a task might reach it, ask first.

Astrology output is presented as traditional interpretation, not prediction, and
the app must never imply a human astrologer is answering — the "answers are
generated automatically" line under the AI question box in `AstroDisclaimer.kt`
stays.

The Tele-MANAS 14416 helpline was on the end of that line and was removed in Sep
2026 on the owner's instruction, given twice after the reason for it was put to
him. It survives in two places that were not part of that instruction and should
not be quietly swept up with it: the crisis-response rule in the model prompt in
`GeminiAstroService.kt`, which only fires when a user sounds hopeless, and the
privacy policy.
