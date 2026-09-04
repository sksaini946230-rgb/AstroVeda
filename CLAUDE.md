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
`MigrationTest` runs every migration against a real database of the previous
version — write one alongside the migration, not after.

**A saved profile's identity is its `uuid`, never its Room `id`.** `id` is
`autoGenerate`, so it restarts at 1 on every device. The cloud backup used to key
its Firestore document on it, and the sync used to treat a matching `id` as "we
already have this" — so two phones on one account each skipped restoring the
other's first profile and then overwrote it on the next upload. One user's saved
birth chart, gone, unrecoverably. The merge also fell back to matching on name +
date of birth, which silently dropped one of a pair of twins.

Firestore documents are keyed on `uuid`, and `ProfileMerge.profilesToRestore`
matches on `uuid` alone. `ProfileMergeTest` covers both failure modes. Do not
reintroduce an id comparison or a name-based one.

**Both languages, always.** Nothing user-facing may be a bare string.
`LanguageManager.getString(hi, en)` or a `*Local` extension
(`PanchangLocalized.kt`, `FestivalLocalized.kt`). A sweep in Aug 2026 fixed 170
hardcoded Hindi strings that English users were seeing — notifications, the
widget, share text, the PDF report, chart glyphs, error messages. Do not
reintroduce them. Chart planet glyphs in particular are stored as
language-neutral tokens (`"Sun"`, `"Mars|R"`) and localised at draw time by
`AstroNames.houseGlyph()`; baking the Hindi in froze charts into whatever
language drew them.

That sweep missed two whole categories, both found in the Sep 2026 audit and both
fixed:

- **`LanguageManager.init` has to run for every process entry, not just
  MainActivity.** It was called only from `MainActivity.onCreate`, but the process
  also starts from two `AppWidgetProvider`s and three WorkManager workers, which
  never touch an Activity. In those processes `currentLanguage` sat at its default
  of Hindi, so an English user's 6:30 AM notification and both widgets came out in
  Hindi after every reboot. **`RevatiApp`** (`android:name` on `<application>`)
  does it now, along with App Check. `LanguageInitTest` fails if that manifest
  registration is ever removed.
- **XML layouts count as user-facing.** Five label `TextView`s in
  `panchang_widget.xml` and two in `tithi_nakshatra_widget.xml` had no `android:id`
  and were never set at runtime, so they sat at their layout defaults —
  `"SUNRISE / सूर्योदय"`, `"TITHI / तिथि"` — showing both languages at once
  forever. They have ids and are set from `LanguageManager` now. `lintDebug`
  reports these as `HardcodedText`; a widget label with a hardcoded string is a
  real bug, not noise.

**A value derived from both the rashi and the transit house may not vary at
all.** `RashifalProvider` set the rating, the lucky colour and the lucky stone
from `(rashiIdx + house) % n`. But

    house = ((planetRashiIdx - rashiIdx + 12) % 12) + 1

so `rashiIdx` cancels out of that sum and what is left is the transiting
planet's own sign — the same for all twelve readers. Every rashi was shown five
stars out of five on the daily view, four on the weekly, and the lucky colour
took two distinct values across the whole zodiac. It survived because the
generated *text* was correctly per-rashi, so the screen looked varied, and
because any single rashi looks perfectly right on its own — only comparing all
twelve exposes it. The rating now comes from classical gochar phala for the
driver planet, and colour and stone from the rashi lord.
`RashifalVariesByRashiTest` compares all twelve. Any new per-rashi quantity
wants the same treatment: check it across the zodiac, not on one sign.

**Narrow phones are 320dp, and Hindi is longer than English.** Checked with
`ScreenSizeScreenshotTest`, which renders the riskiest screens at 320dp and
360dp (plus dark and 1.3x text) into `app/src/test/screenshots/`. It asserts
nothing on purpose — an automated overflow walker was built first, over the
semantics tree, and it could not be trusted: `TextLayoutResult` reached that way
returns whichever layout pass ran last, and anything measured speculatively
(Rows with weights, `IntrinsicSize.Min`) leaves a result describing a width
nobody ever saw. It reported the sub-tab headers and the PRO upgrade banner as
broken when rendering proved both fit. Look at the pictures instead. Three real
bugs it did find, now fixed: the dashboard shortcut tiles clipped every label at
320dp (the grid drops to one column below 300dp of *content* width — the
threshold is on the width the grid is handed, not the screen), the kundali
date/time fields clipped their labels (shortened to "तिथि"/"समय"), and the PRO
badge in Settings was laid out zero pixels wide and simply never drawn, because
the Row beside it had no `weight(1f)`.

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

**Firestore rules live in the console; `firebase/firestore.rules` is a copy.**
The client only ever touches `users/{uid}/kundali_profiles/{id}`, but that
scoping is worth nothing unless the server enforces it. Checked in the console
on 3 Sep 2026: the deployed rules require `request.auth != null &&
request.auth.uid == userId` over `users/{userId}/{document=**}`, and everything
else is denied by default. That is correct. Nothing here deploys the file, so
the two can drift — if you change the data model, change both.

Firebase Storage is not used by the app, so its rules do not matter.

---

## Build and verify

```bash
./gradlew testDebugUnitTest lintDebug   # must both pass before any commit
./gradlew installDebug                  # onto a connected device
```

`.github/workflows/ci.yml` runs those same two on every push and pull request.
`gradle.properties` no longer pins `org.gradle.java.home` — it used to hold an
absolute path to one Mac's Temurin 17, so the repo could not build anywhere else,
CI included. The toolchain is provisioned by the foojay resolver in
`settings.gradle.kts`. Do not put that line back.

Screens are best checked on a real device in **both languages and both themes** —
that is how most of the UI bugs in this app were found, not by reading code.

---

## Where it stands

Live on Play, production. `versionCode 5` / `1.2` was uploaded 3 Sep 2026 and
confirmed installed on a real device. The tree is now on **`versionCode 7` /
`versionName 1.4`** — the September 2026 full-codebase audit (which was
versionCode 6), plus profile export/import, the narrow-phone layout fixes and
the rashifal variation fix below. **Not yet uploaded and not yet checked on a
device.**

### What versionCode 6 changed

A line-by-line audit found one data-loss defect and a set of things that were
written, tested and never actually wired up. The individually interesting ones
have their own notes above ("Things that will bite you"); the shape of it:

- **Saved profiles were being destroyed by cloud sync** — the Firestore document
  was keyed on the Room autoGenerate id. See the `uuid` note above. Schema is
  now **version 6**, with `MIGRATION_5_6` backfilling a uuid for every existing
  row, plus the indexes the database had never had (there were none, on any
  table). `MigrationTest` covers it.
- **Notifications and widgets spoke Hindi to English users** — `LanguageManager`
  was only ever initialised from `MainActivity`. `RevatiApp` fixes it; see above.
- **`SecurityUtils` had zero call sites** while advertising OWASP and DPDP
  compliance, with seven passing tests. Root detection and a permanently broken
  SQL-injection regex (`"\b"` is backspace in Kotlin, not a word boundary) are
  deleted; the sanitiser is wired into `BirthData.parse` and both profile-save
  paths, which is where free text actually enters the app.
- **EEA/UK users could not withdraw ad consent.** `AdConsentManager` had
  `showPrivacyOptions` and `isPrivacyOptionsRequired` and nothing called either,
  so consent was collected once on first launch and locked in — a UMP policy
  violation. Settings → Legal & Privacy now shows the row, gated on the UMP
  requirement status, so it stays invisible in India.
- **"Delete my account" could destroy the cloud copy and then fail**, because
  `user.delete()` throws for a stale session *after* the Firestore wipe had
  already committed. It forces a token refresh first now, so a stale session
  fails with everything intact.
- **The panchang cache cost more than it saved** — a cache hit re-ran the entire
  ephemeris to recover the planet list, then threw the rest away. `planetsJson`
  holds them now. `deleteExpiredCache` existed in both DAOs and was never called
  by anything, so cache rows accumulated forever; the daily worker prunes them.
- **Settings said 7:00 AM and the notification arrived at 6:30**, because the
  worker and the ViewModel defaulted the same preference key differently.
- **The festival notification would never have fired on a Devanagari-numeral
  device** — it built an ISO date with `String.format` and no `Locale`, then
  compared it against real ISO dates.
- Removed: Retrofit, OkHttp, Moshi and Coil (zero imports, all four), three
  never-instantiated ViewModels, `tsconfig.json`, a stale root
  `google-services.json` that disagreed with the real one, and a
  `triggerTestCrash()` that shipped in release.
- Added `.github/workflows/ci.yml`, and dropped the `org.gradle.java.home` line
  that made the repo unbuildable on any machine but one.

**Before uploading this one:** check the app on a real device in both languages
and both themes, and specifically confirm a saved profile survives the 5 → 6
migration on a device that already has data.

`versionCode 4` fixed AdMob serving no ads in production at all: both AdMob
apps from the rename (old AstroVeda, current Revati) still exist, and `.env`'s
`ADMOB_APP_ID_ANDROID`, `ADMOB_BANNER_ID` and `ADMOB_INTERSTITIAL_ID` were all
three pointed at the old app's IDs — reference memory has the correct ones, and
where to find them again if `.env` is ever rebuilt. `versionCode 5` fixed a
second bug in the same area: `AdBanner` still gated its first request on
`AdsInitState.ready`, which the fix's own doc comment had already argued
against — a signal that never arrives should never be able to hold the banner
back forever. It doesn't gate any more; it asks immediately and retries.

Also in this pass: the festival calendar (`FestivalCalculator` /
`FestivalProvider`) now computes dates from each festival's tithi rule instead
of a table hardcoded to one year, verified against published panchang for
2025-2031 including an Adhika month and a nine-minute Raksha Bandhan window.
The old table ran out in November 2026 and had three dates wrong besides.

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
  in the same pass as `PLAY_LICENSE_KEY`, not before. `PurchaseVerifier` returns
  **true** with no key configured — deliberate, because refusing every genuine
  purchase would be worse while nothing is purchasable at all. It logs at error
  level now (the device this is tested on records nothing below `E`), and
  `PurchaseVerifierTest` pins the behaviour so the switch has to be conscious.

**Ads: the serving block was a console problem, and it is cleared.** For weeks
no ad of any kind was served. It was never the code. AdMob's Verify app page
said it outright: *"We didn't find a developer website in your app listing on
Google Play."* The chain was

    no website on the Play listing
      -> AdMob cannot fetch app-ads.txt
        -> App verification: Not verified
          -> Approval status: Requires review
            -> no ads served

Fixed 4 Sep 2026 by publishing `https://sksaini946230-rgb.github.io/` (a GitHub
Pages user site serving `app-ads.txt` with
`google.com, pub-5513456541171739, DIRECT, f08c47fec0942fa0`), setting it as the
developer website on the Play listing, and running AdMob -> app -> Verify app ->
Check for updates. AdMob now reads **App verification: Verified**. Do not go
looking for this in the code again.

Two code faults were found afterwards and fixed, both the same mistake:

- **The banner was gated on `isStartupComplete`.** `AdBanner`'s own doc comment
  argues that a signal which never arrives must never be able to hold the banner
  back — the gate had been removed from inside `AdBanner` once already, and was
  then reintroduced one layer up, in `MainActivity`'s `bottomBar`. That flag is
  set at the end of a coroutine that first runs `recalculatePanchang()`, so one
  throw out of the ephemeris meant no banner for the entire session. The gate is
  gone, and `MainViewModel`'s startup block now sets the flag in a `finally` so
  it arrives whether startup succeeded or not.
- Startup *finishing* and startup *working* are different facts. Nothing should
  gate on the second one.

**Interstitials are wired to tab switches and are meant to be sparse.**
`onBottomNavTabSelected` triggers one on every real tab change, but
`MainActivity.showInterstitialAd` will not show one until the session is 90
seconds old, keeps 3 minutes between them, and stops after 3 in a session. This
is deliberate — a Panchang app is opened for ten seconds to read a tithi — so
"I switched tabs and got no ad" is the design working, not a fault. To see one,
use the app for over 90 seconds first.

**The "native debug symbols" warning on upload cannot be fixed here.** Play warns
that the bundle has native code with no symbols. This app has no native code of
its own; the eight `.so` files come transitively from `androidx.graphics.path`
and `androidx.datastore`, and both ship **already stripped** — verified on the
versionCode 6 bundle, 0 symtab entries, ELF header says `stripped`.
`debugSymbolLevel` extracts symbols, it cannot invent them. The setting is left
on in `app/build.gradle.kts` because it is correct in principle, but the warning
will stay. It is advisory and does not block a release.

Deliberately left alone by the September 2026 audit, with reasons:

- **The four screen composables are still one function each** — `PanchangScreen`
  is 896 lines, `KundaliScreen` 875, `MatchingScreen` 621, `RashifalScreen` 568.
  Splitting them is the single biggest recomposition win available, and it is
  also a large blind refactor of screens whose bugs, by this file's own account,
  are found on a device rather than by reading code. Worth doing deliberately,
  with a device in hand, not in a sweep.
- **Debug still shares the production Firebase project.** There is no
  `applicationIdSuffix`, so debug builds read and write live user Firestore data.
  The fix is `debug { applicationIdSuffix = ".debug" }` plus a second Firebase
  Android app — but adding the suffix *before* that app exists in the console
  breaks Google Sign-In and App Check on every debug build. Console step first.
- **The Room database is unencrypted**, and holds names, exact birth times and
  coordinates. Backup and device transfer already exclude it, so this needs
  physical device access. SQLCipher with the key in the Android Keystore is the
  real answer; it is a migration of its own and wants its own pass.

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
