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
| Local checkout | `~/Revati` — renamed from `~/AstroVeda` on 8 Sep 2026 |
| GitHub repo | `sksaini946230-rgb/Revati` — renamed from `AstroVeda`; the old URL redirects |

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

The checkout and the GitHub repo both say **Revati** now. The folder was
renamed from `~/AstroVeda` on 8 Sep 2026 and the repo from
`sksaini946230-rgb/AstroVeda` shortly after, by the owner from GitHub's settings
page. Nothing in the build refers to either name: paths are relative, and the two
absolute ones that existed — `goldie.config.ts` and `.env`'s `KEYSTORE_PATH` —
were rewritten with the folder.

**Never create a repository called `AstroVeda` on this account.** The old URL
still works only because GitHub redirects it, and it has to keep working: every
copy of the privacy policy handed out before the rename points at
`github.com/sksaini946230-rgb/AstroVeda`. That redirect lasts until the name is
taken again, and a new repo under it breaks every old link at once. The ads site,
`sksaini946230-rgb.github.io`, is a separate repository and was not affected;
this repo has no Pages site of its own, which matters because Pages URLs are the
one kind a rename does not redirect.

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

**"Everyone sees the same data" has had four separate causes.** They are worth
listing together because each looked different and none was visible from one
user's screen:

1. `RashifalProvider` derived values from `(rashiIdx + house)`, in which rashiIdx
   cancels — see the note above.
2. The horoscope **cache** kept serving those values for seven days after the
   fix. `MIGRATION_6_7` clears it.
3. The Rashifal **AI insight** was one shared `String` for all twelve signs, so
   fetching for one sign left its text under every other sign's heading. It is
   keyed by sign now.
4. When the model cannot be reached, `getOfflineVedicResponse` picks text by
   keyword — and the insight question carries none of them, so all twelve signs
   got one identical paragraph under a heading promising a personalised reading.
   That is treated as a failure now, with a retry, rather than shown as an
   insight. `AiInsightFallbackTest` pins the premise.

Also in that family: **Numerology shipped pre-filled with the developer's own
name and date of birth**, already calculated, so every first-time visitor was
shown a stranger's Moolank and reading. The fields start empty and the results
stay hidden until the user asks.

The general lesson: a per-user or per-item derived value cannot be judged from
one instance. Dump the whole set and count the distinct values.

**AI calls are rate limited, because nothing was limiting them.** `AiRateLimiter`
— a 3 second minimum gap and a rolling 20 per hour — sits in front of both
`askAiAstrologer` and `fetchPersonalizedInsight`. There is no server between this
app and the bill, so an unbounded question box is a direct cost and a shared
quota one user can exhaust for everyone.

**Analytics were almost entirely unwired.** `AstroAnalytics` has eighteen logging
methods; `init` and `logAppOpen` were the only two ever called, so the live app
recorded app opens and nothing else. Fifteen of them are wired now — screen
views, onboarding, horoscope views, kundali, matching, numerology, AI queries,
login on all four paths, the three purchase outcomes, and sharing. Three are not:
`logFirstOpen`, `logOnboardingStep` and `logPanchangView`. The last is not a
free wiring job. The city it sends is usually the one derived from the phone's
GPS, which would make it the only place a user's location leaves the device —
against what the privacy policy says and what the Data safety form declares. Its
doc comment says what has to change with it.

`recordNonFatal` in particular now sits on every data path that can lose or fail
to save a profile: background backup, cloud backup, sync, local wipe, export,
import, account deletion. They were calling `Log.e` and stopping, which means
nobody ever learned it happened — the test device records nothing below `E`, and
no one reads a stranger's logcat.

`AstroFeatureFlags` was the same dead pattern and is **deleted**. Its own doc
called it a "Remote Kill-Switch & Soft Launch Feature Flag Engine"; it had zero
call sites, no remote source at all (every flag a hardcoded default), and a
support address, `support@astroveda.app`, that is not the one the app or the Play
listing uses. A real kill switch needs a real remote source and is its own piece
of work.

Not every silent `catch` is a bug, and the count is misleading. The ones in
`AstroAnalytics` are correct — analytics must never crash the app, and an
analytics failure cannot be reported through analytics. The ones in `onCleared`,
in ads/consent init, and around an external browser that may not exist are
correct too. The user-facing data paths already reported failures in both
languages before this pass; what they lacked was telling *the developer*.

**R8 was keeping three quarters of the app.** `proguard-rules.pro` carried
blanket rules — `-keep class com.google.firebase.** { *; }` and
`-keep class com.google.android.gms.** { *; }` chief among them — which held the
two largest dependency trees completely immune to the optimiser. Play measured
it and said so on the release dashboard for versionCode 7: **optimization 27%,
obfuscation 28%, shrinking 28%**. Firebase and Play Services ship their own
consumer ProGuard rules inside their AARs and R8 applies those automatically; a
blanket keep on top only defeats it. Removing them took the APK from 10.4 MB to
8.8 MB and obfuscated all 15,482 classes instead of a quarter of them.

What must stay is this app's own classes that are reached by reflection —
`com.example.data.model.**`, `com.example.data.local.**`, `com.example.worker.**`,
the Room `@Entity` keep and the Firestore `PropertyName` members — plus the
Firebase AI serializer rules. Those are kept by name.

`com.android.billingclient.api.**` also keeps its blanket rule, deliberately and
alone: PRO is not purchasable until the merchant account exists, so a purchase
cannot be walked through a minified build to see it work. The library is small,
and a purchase that silently fails in production is not worth the bytes.
Revisit when a real purchase can be tested.

Verified on a device against the minified build: no crash on any screen, Google
Sign-In's account picker opens (that is `androidx.credentials` and
`com.google.android.libraries.identity`, both of which lost their blanket keeps),
and real banner and interstitial ads serve. The Firebase AI call was not
re-exercised live — the device was stuck in landscape, where the content area is
too short to reach the question box — but its serializer keeps were left intact
and 710 Firebase AI classes with 124 `serializer()` methods survive in the
mapping.

**Landscape is cramped and nobody has designed it.** On a 720x1600 phone held
sideways the sub-tab header and bottom nav leave roughly 250dp of content, the
banner lands mid-screen, and some screens cannot be scrolled to their end. It is
usable but not good, and it has never been a design pass of its own.

**The Dependabot count is about the build, not the app.** GitHub reports 50
vulnerabilities on the default branch and the number is alarming until you look
at where they are. Checking every dependency by name against OSV:

    277 libraries that ship inside the APK   ->  0 vulnerable
    147 build-time libraries (Gradle plugins) ->  6 vulnerable, 7 advisories

The six are bcprov-jdk18on, bcpkix-jdk18on, commons-lang3, jose4j, jdom2 and the
Kotlin Gradle plugin — all of them pulled in by Gradle plugins, all of them
running on the build machine and on the CI runner, none of them present in the
APK. The threat they describe is someone compromising a build, not a user's
phone.

The count does not reconcile: this scan finds 7 advisories where GitHub counts
50. The likely reason is that Automatic dependency submission reports a graph
per build variant, so one advisory is counted several times — but that is a
guess and has not been confirmed against GitHub's own list. What *is* confirmed
is the half that matters: nothing that reaches a user is vulnerable.

Keeping AGP, Kotlin and KSP current pulls newer versions of all six in time.
Reproduce with `./gradlew :app:dependencies --configuration releaseRuntimeClasspath`
and `./gradlew buildEnvironment`, then post the resolved coordinates to
`https://api.osv.dev/v1/querybatch`.

**Sunday's Choghadiya was wrong, and a test that counted slots let it
through.** Each Choghadiya belongs to a graha — Udveg/Sun, Char/Venus,
Labh/Mercury, Amrit/Moon, Kaal/Saturn, Shubh/Jupiter, Rog/Mars — and the daytime
sequence for a weekday is that Chaldean cycle rotated to begin with the
weekday lord's own Choghadiya. Six of the seven rows in `ChoghadiyaCalculator`
obeyed that. Sunday stepped through the cycle three at a time, so on a Sunday
07:34-09:09 was labelled **Amrit**, the most auspicious slot there is, when it
is Char and merely neutral; and 09:09-10:43 was labelled **Rog** when it is
**Labh**. That is the screen people read to choose when to begin something.

The only test touching Choghadiya asserted that eight slots came back. Eight
always came back. `ChoghadiyaSequenceTest` checks the sequence itself now, and
was confirmed to fail against the old table before the fix went in.

**Open question, deliberately not changed: the night table uses a different
progression from the day table.** All seven night rows are consistent rotations
of one list, and every one starts on the lord of the fifth weekday, which is the
classical rule and is now covered by a test — and, since the toggle was fixed,
is finally visible on the device (Monday opens on Char, Shukra's Choghadiya, and
Friday is the fifth weekday from Monday). But that list steps the Chaldean
order five at a time where the daytime list steps it one at a time. That may be
a real convention, or it may be the same class of slip as Sunday's. It was left
alone because, unlike Sunday, nothing about it is internally inconsistent —
deciding it needs someone who knows the shastra, not someone reading the table.

**The night Choghadiya could not be reached at all, and that hid the open
question above.** Tapping "रात का चौघड़िया" moved the pill and changed nothing
else — all eight tiles kept the daytime sequence and the daytime hours under a
control that said night. `choghadiyaSlots` was a plain `get()` reading four
MutableStateFlows through `.value`, which is not a snapshot read, so Compose
never learned the list depended on them; the only scope invalidated was the one
drawing the pill. The same silence covered the city, the date and the 12/24-hour
setting. It is a `combine` of all four now. Half a screen had never worked, and
nobody had noticed because the screen looked like it responded.

**Bhakoot had two of the three doshas.** The classical set is 2/12
Dwirdwadasha, 5/9 Nav-Pancham and 6/8 Shadashtaka; `calculateBhakoot` listed 5/9
among the *scoring* distances instead, so one couple in six was handed the full
seven points and told "भकूट दोष नहीं है" underneath. Seven of thirty-six is
enough to carry a match across the 18-point line the conclusion text treats as
the verdict. The dosha label had no branch for Nav-Pancham either, so a fix to
the score alone would have called it Shadashtaka. The three distances now live
in one named list so the score and the label cannot disagree again, and
`BhakootDoshaTest` asserts all 144 rashi pairs against the rule rather than
restating the table.

**Guna Milan never asked for a birth time.** The calculator has always taken
one and `MainViewModel` has always held the field, defaulted to "12:00" — but
nothing on screen set it, so every match was computed for noon. The Moon changes
nakshatra about once a day and Tara, Yoni, Gana and Nadi are all read from the
nakshatra: 21 of the 36 points. The field is there now, still optional, and the
card under the form says what leaving it blank costs.

**The lucky time reached three of its six values.** `(rashiIdx * 5 + house +
dayOfMonth) % 6`, and six divides twelve, so the modulo inside `house` survived
the outer one and the expression reduced to `4 * rashiIdx` plus terms every
rashi shares — and 4 is not coprime with 6. Half the list was unreachable on any
given day and four rashis shared each of the rest. The fix is a shape that
cannot cancel rather than a better multiplier: the lucky time no longer involves
the transit house at all. This is the fifth separate cause in the "everyone sees
the same data" family, and the first one found by *counting a field nobody had
counted* — `RashifalVariesByRashiTest` covered the rating, the colour, the stone
and the six readings, and this was not among them.

**The recent-search chip on Guna Milan had never worked.** It assigned the four
local `remember` variables, which is what the fields render, while
`calculateGunaMatching()` reads the ViewModel — so the form filled in and the
calculation then refused it with "कृपया नाम दर्ज करें" over a form that plainly
had a name in it. No test could have caught it: the calculator was right, the
screen was right, only the wiring between them was wrong.

**Generated text does not follow a language switch on its own.**
`onLanguageChanged` recomputes the Panchang and the horoscopes for exactly this
reason, and the astro news was missed: switching to English left the Hindi
bulletins on the More tab verbatim. It was the only Devanagari string a sweep of
the whole app in English still found. The offline copy is bilingual so re-reading
it is free; model-fetched news costs one call. The AI answer and the per-rashi
insights are cleared rather than re-asked — re-asking spends a call to say the
same thing, and leaving them puts a Hindi paragraph under an English heading.

**A clipped word at the top of a scrolling list is not a font bug.** Devanagari
loses its matras first, so a half-scrolled heading reads "भकूट दाष ावचार" and
looks like broken shaping. It is the scroll viewport. The tell is that the
warning icon beside it is clipped at exactly the same y — a font problem cannot
crop a vector. Check that before reaching for `includeFontPadding`.

**What the security review found, and what it did not.** Checked before the
public launch, all of it against the release build rather than the source alone:

    manifest        not debuggable; no app component exported without a
                    launcher intent, a widget, or a permission behind it
    backup          Room database and both preference files excluded from
                    cloud backup and device transfer, on both API levels
    network         cleartext blocked, system trust anchors only
    WebView         JavaScript off, DOM storage off, file-URL access off, and
                    only the two bundled legal pages load in it
    APK strings     nothing extractable that is not public by design — the
                    AdMob unit ids and the Firebase key, both of which ship
                    in every APK on Play
    input           sanitised at all four entry points: both profile-save
                    paths, BirthData.parse, and the profile import
    Firestore       users/{uid}/** behind request.auth.uid == userId
    code            no world-readable modes, no addJavascriptInterface, no
                    custom TrustManager or hostname verifier, no Runtime.exec,
                    no PII in any log

**App Check is the control that matters, and it is provably working.** Play
Integrity is installed in `RevatiApp.onCreate`, so worker and widget processes
are attested too, and the debug provider lives in a variant source set that
never reaches release. A release APK signed with the upload key and side-loaded
was refused by the server:

    Firebase AI Logic call failed: Firebase App Check token is invalid

So extracting the API key from the APK does not buy an attacker Firebase AI —
which is the thing that costs money. The API key restriction in Cloud Console
is still worth adding as defence in depth; it is not the only thing standing
there.

**PRO cannot be defended against a patched APK, and does not need to be yet.**
Entitlement is a boolean in SharedPreferences, so anyone with root can set it.
What stops that mattering is `queryPurchases()`: Play is asked on every launch
and an absent subscription revokes Pro, so a hand-set flag survives until the
next start. A patched binary that skips the revocation cannot be stopped
without a server, and there is no server. The financial exposure today is zero
because PRO is not purchasable.

Left as they are, with reasons: `USE_BIOMETRIC` and `USE_FINGERPRINT` arrive
from `androidx.credentials`, which Google Sign-In needs. Nothing in this app
uses biometrics, but both are normal-level permissions with no prompt and no
Play declaration, and removing a permission a sign-in library asks for is not
a change to make the day before a launch — Google Sign-In has broken in
production here once already.

**minSdk is 24 and there is no core library desugaring.** `java.time` is off
limits in `app/`. Lint catches it; it once got as far as a crash-on-Android-7
before that. Use `java.util.Calendar` or parse strings.

**Devanagari clips without help.** Compose defaults `includeFontPadding = false`,
which eats i-matras ("पिछला दिन" → "ापछला ादन"). `Type.kt` sets
`PlatformTextStyle(includeFontPadding = true)` plus `LineHeightStyle(trim =
None)` and 1.35× line heights. Leave it alone.

**Themes come from `LocalAstroColors`.** Light and dark both real, following the
system. Never hardcode a colour that only works in one.

**The bottom navigation bar is a floating capsule and every tab carries its
label under its icon**, with the selected one inside a tinted pill. It follows a
design the owner supplied. An earlier version showed the label on the selected
tab only, on the reasoning that five labels cannot share a 320dp screen — which
was true of the way it was measuring, and is not true of the way it measures
now. Nothing in this bar is a constant that was chosen by eye; four separate
things are computed, and each of them was got wrong first:

- **Measure every label, not the longest one.** "कुण्डली" has more characters
  than "राशिफल" and is 5px narrower, so picking by `length` measures the wrong
  string.
- **Measure in the style that is drawn.** The labels render SemiBold and were
  being measured at the default weight, which is narrower — an under-measurement
  in the one direction that clips.
- **Measure against the pill, not against the slot.** Only the selected tab
  draws a pill, so only its label is bounded by one — and near the bottom of a
  capsule there is far less width than the shape's own width. A capsule 60dp
  wide and 47dp tall has 11dp of straight side; the label sits at the bottom,
  deep in the curve, and "Panchang" ran out past both sides of its own pill on
  the device. `cornerInsetAt` computes that loss and the size ladder is checked
  against what is left.
- **The fix for that is a shorter pill, not a squarer one.** The first attempt
  dropped the corner radius to 32% of the height, which is a rounded box and was
  rejected on sight — the design being followed has a capsule. The radius is
  half the height again, and what changed instead is everything that makes the
  pill tall: an 18dp icon, a 2dp gap under it, 4dp of ring around the bar. The
  bar went from 61dp to 51dp and the label fits. The arithmetic is worth
  keeping: the width lost to the curve is `R - sqrt(R² - (C/2)²)` for a stack of
  height C, so a *deeper* vertical padding makes the end cap flatter and buys
  width back — 5dp to 6dp there is the difference between a label and no label
  at 320dp in English.
- **The pill has to stay wider than it is tall, and that is a constraint on the
  label.** `MIN_PILL_RATIO` is checked before the width, with a second pass that
  drops the requirement rather than the label. The design's own ratio is 1.6 and
  is not reachable on a phone: five tabs on a 320dp screen give a 57dp pill, so
  1.6 would need a 36dp pill around a 32dp stack. The limit is the word
  "Horoscope" — the reference's longest label is "Publish".
- **Solve for both languages at once, not for the one on screen.** The bar used
  to change height when the language was switched — 53.5dp in Hindi against
  51dp in English, measured on the device, growing upward from a fixed bottom
  edge. Devanagari's line box is about 1dp taller than the Latin one at the same
  size, and, the larger part, the two were choosing different sizes: "राशिफल" is
  33px at 11sp and fits, "Horoscope" is 53px at 11sp and does not. Solving them
  separately and taking the taller pill fixed the height and broke the shape —
  at 320dp Hindi has no size that satisfies the ratio, so it fell through to the
  pass that gives up the ratio, took the largest size that merely fits, and
  handed English a 53x49 disc. One measurement over both label sets is what
  holds: identical in either language by construction, and sized by "Horoscope",
  which is the widest string in the app in either script anyway.
- **`BAR_MAX_WIDTH` is 420dp and no portrait phone reaches it.** The widest
  common phone is 412dp, which leaves a 388dp bar, so this changes nothing where
  the app is used. It is there for everything wider: the same phone turned
  sideways is 800dp, and five tabs sharing that gave a pill 155dp wide against
  47dp tall — 3.3, against 1.4 in portrait — a stretched band with the labels
  marooned. Capped and centred it comes out at 1.7.
- **A font scale that does not fit is not a reason to drop the label.** The size
  ladder runs 12 down to 8 twice: first in `sp`, which honours the user's font
  setting, then — only for a user who enlarged the text — in `dp`, which ignores
  it. So a 320dp phone at 1.6x shows the label at the size a 320dp phone always
  shows it, instead of showing no label at all. Icons alone remain the last
  resort and no configuration in the screenshot set now reaches it.

Two standing traps: **the English labels are the long ones**, so a bar checked
only in Hindi proves nothing; and **the shot must have the widest label
selected**. `NavBarOnly` used to select "Kundali", the shortest of the five, so
every screenshot showed a comfortable fit while the device did not — it selects
"Horoscope" now. The bar's height follows the measured label, so it grows a
little with the font scale instead of cropping the Devanagari matras.
`navbar_*` and `navbar_en_*` screenshots cover 320/360/412/600/800, dark, 1.3x
and 1.6x; confirmed on the device in both languages, both themes, portrait and
landscape, and at 1.0x/1.3x/1.6x.

**An AdView reserves its height whether or not it has an ad.** Roughly 50dp of
empty strip, and it used to hide itself by accident: the banner gave up after
three failures and rendered nothing. Making it retry indefinitely left that
strip sitting above the tab bar for the whole session on a phone getting no
fill, which reads as a rendering fault. It is `height(0.dp)` until `loaded`.

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
`ADMOB_APP_OPEN_ID`, `ADMOB_REWARDED_ID`, `GOOGLE_WEB_CLIENT_ID`,
`PLAY_LICENSE_KEY`, `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`,
`KEY_PASSWORD`.

`ADMOB_APP_OPEN_ID` and `ADMOB_REWARDED_ID` are still `NOT_CONFIGURED`. The
units exist in AdMob; the ids have not been pasted in. Until they are, those two
placements are absent — no crash, no test ad, nothing on screen. See
`AdIds.resolve`.

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
| Play app signing (what users present) | see below — **the value that used to be here is stale** |
| Upload key | `77:D9:C2:35:B4:EB:2D:47:8E:89:DB:27:41:A8:D7:E2:06:9A:40:81` |
| Debug | `D3:46:B8:A4:61:5B:85:6A:4F:AF:5B:F2:64:D2:01:47:2B:F2:31:E1` |

The upload and debug values above were re-derived from the keystores
themselves (`keytool -list -v`) and are correct.

**The Play app signing key was upgraded on 25 Aug 2026**, and this file went on
recording the old one. Play Console → App signing now shows a *Classical key*, a
*Post-quantum cryptography key*, and a **Previous app signing keys** section
first used 25 Aug 2026 — which is what the old
`B7:D1:DE:10:DD:F3:5E:FD:56:27:99:FA:F6:C4:D1:D0:38:B4:5C:C5` was. Pulling the
APK Play actually serves and reading its certificate gave

    SHA-1    FB:3F:D2:6B:05:B8:71:4E:8B:2D:DB:E5:DB:67:AF:FE:B5:96:52:2F
    SHA-256  9A:42:6C:10:AD:96:C7:69:0E:A3:FC:4B:62:DC:99:34:BE:E5:DF:C6:D0:95:8D:93:B4:55:E7:EF:7D:9F:49:B9

and the SHA-256 there matches what the console shows. Before registering these
anywhere, copy them from Play Console → App signing rather than from here: there
are now several certificates (classical, post-quantum, previous) and a user's
install may present any of them.

**The Firebase API key has no application restriction.** GitHub secret scanning
flags `app/google-services.json`, and the honest reading is that the key in it
is not a password — it ships inside every APK and anyone can read it out of a
Play download. What matters is what the key is allowed to do, and Google Cloud
Console → Credentials → *Android key (auto created by Firebase)* currently says

    Action recommended: This key can currently be used with any application.
    Application restrictions: None

with API restrictions set to 25 APIs, among them Firebase AI Logic and Identity
Toolkit. So anyone holding the key can call those from anywhere, billed here.
The fix is Application restrictions → Android apps with the package name and
**every** SHA-1 above, and it wants a device in hand: get the list wrong and the
app's AI and Google Sign-In both stop working. Reverting to None is instant if
they do.

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

The layout screenshots are Roborazzi over Robolectric, and **`testDebugUnitTest`
does not write them** — it runs `ScreenSizeScreenshotTest` and every capture in
it is a no-op without the record flag. The task that produces PNGs is:

```bash
./gradlew recordRoborazziDebug          # writes app/src/test/screenshots/
```

They assert nothing on purpose; the point is to open them. And a screenshot
proves something only when the worst case is the one inside it — see the
narrow-phone and navigation-bar notes above.

`.github/workflows/ci.yml` runs those same two on every push and pull request.
`gradle.properties` no longer pins `org.gradle.java.home` — it used to hold an
absolute path to one Mac's Temurin 17, so the repo could not build anywhere else,
CI included. The toolchain is provisioned by the foojay resolver in
`settings.gradle.kts`. Do not put that line back.

Screens are best checked on a real device in **both languages and both themes** —
that is how most of the UI bugs in this app were found, not by reading code.

---

## The Play listing

The store screenshots are generated, not taken by hand. `goldie/goldie.config.ts`
holds all eight scenes, their headlines, the background and the typeface, and
`goldie frame` renders them into `goldie/out/screenshots/pixel-10-pro/en-US/` as
eight 1080x1920 tiles. The config is the source; `goldie/out/` is gitignored.
`goldie/README.md` has the commands.

The raw screens behind the tiles were captured from the real device rather than
an emulator — there is no AVD on this machine — and four things about that
capture show up in the finished tiles when they are got wrong:

- **Render bigger than the phone.** The device is 720x1600 and goldie draws into
  1280x2856, so a straight capture comes out soft. `wm size 1080x2400` with
  `wm density 420` gives 411dp, an ordinary modern phone. Reset both after.
- **Cut the network.** `AdBanner` is `height(0.dp)` until an ad loads, so with
  wifi and data off there is no banner in any capture and no interstitial
  landing mid-walk.
- **Put SystemUI in demo mode**, or a notification badge turns up in a tile.
- **Sample data only.** The device's saved profiles carry the owner's real name
  and birth time, and the Kundali form's Recent Searches chips display them.
  The tiles use "Aarav Sharma", "Rahul & Priya" and Jaipur.

Only `en-US` is rendered so far. A Hindi set needs the raw screens re-captured
with the app in Hindi — goldie renders every locale from the same captures, and
only the copy changes.

**The privacy policy is one text in two places.** `docs/PRIVACY_POLICY.md` is
the URL on the Play listing (*App content → Privacy policy*) and is what a
reviewer reads; `app/src/main/assets/privacy_policy.html` is what Settings
opens. They used to be written separately, and by Sep 2026 they disagreed on
almost everything. The published one opened with "An Account Is Required",
after the sign-in gate had been removed and against the Data safety form, which
marks name and email optional, and it called a precise location "approximate".
The in-app one described push-notification tokens for an app with no push
messaging, and said the AI receives the time and place of birth, which it does
not. Neither mentioned Firebase Analytics.

Edit the markdown, then run `python3 docs/render_privacy_policy.py`.
`PrivacyPolicyTest` fails if the two copies differ by a word, if a Google
library ships in the app without being named in the policy (or the policy names
one that does not ship), if `ACCESS_FINE_LOCATION` and "precise location"
disagree, or if the Tele-MANAS line is dropped. It was confirmed to fail, all
four ways, against the policy it replaced.

What the policy says about location rests on one fact worth protecting: the
device's location never leaves it — not to the AI (which gets a name, a date of
birth and a lagna), not to Firestore, not to Analytics. Only Android's own
geocoder sees the coordinates, to name the city. See `logPanchangView`.

Release notes, and the rules for writing them, live in `docs/RELEASE_NOTES.md`.
Play takes 500 characters per language and will not publish with only one of the
two filled in.

---

## Where it stands

Live on Play, production. **`versionCode 156` / `2.0` was uploaded by the owner
on 8 Sep 2026**, along with a new set of eight English store screenshots. It
supersedes `versionCode 11`, which the owner had put up earlier the same day,
and the `versionCode 5` / `1.2` that production had been on since 3 Sep. That 10
and 11 both went up inside a day is how the versionCode note below came to be
written.

It carries the navigation bar rebuilt to a design the owner supplied, verified
on a real device in both languages, both themes, portrait and landscape, and at
1.0x/1.3x/1.6x font scale.

Nothing is built and waiting to go up. The tree's own number is just its commit
count and climbs with every commit, so a number higher than 156 here does not
mean a release is pending.

**`versionCode` is derived, never typed.** It is `git rev-list --count HEAD` —
the number of commits on the branch — resolved in `app/build.gradle.kts`. That
number only ever goes up, it goes up on every commit, and every release is
committed before it is built, so a code cannot repeat.

It is derived because typing it failed twice in a row. A release was built on
10 after 10 had gone up; the fix was "bump to 11", and 11 had gone up too. Both
times the only symptom was *"Version code N has already been used"* at the top
of the Play upload page, after a ten-minute build — and both times the mistake
was the same one: this machine cannot know what has been uploaded, because the
owner uploads, not the build. Guessing was the defect, not the guess.

`VERSION_CODE_FLOOR` is 20, above everything uploaded under the old scheme. If
git cannot answer — a source zip, a shallow CI checkout — the floor is used so
that tests and lint still run, and `assembleRelease` and `bundleRelease` refuse
outright, because the floor is by definition a number Play has already taken.
CI checks out with `fetch-depth: 0` for the same reason.

### What versionCode 156 changed

Two things. **`versionCode` is now derived from the commit count**, for the
reason above — which is why this release is 156 and not 12.

The other is **the bottom navigation bar**. Every tab now
carries its label under its icon, the selected one inside a tinted capsule, on
a floating capsule bar — the layout the owner asked for, matched from a
screenshot. The full account of how the label is fitted, and of the four
separate ways it was got wrong first, is in the navigation-bar note under
"Things that will bite you". The short version, because each is a general
lesson:

- A screenshot test that selects the *shortest* label proves nothing about a
  bar where only the selected tab draws a pill.
- Measuring text at a different weight than you draw it under-measures, in the
  direction that clips.
- A label inside a capsule is bounded by the curve, not by the width.
- Solving for the language on screen makes the bar change size when the
  language is switched. Solve for both.

The bar is also capped at 420dp and centres beyond that. No portrait phone
reaches that, but the same phone in landscape is 800dp, where it had never been
rendered and looked like a stretched band.

### What versionCode 10 changed

The release the September work adds up to: the September 2026 full-codebase
audit (versionCode 6), profile export/import, the narrow-phone layout fixes and
the rashifal variation fix, plus what a full pass on a real phone found —
Guna Milan missing one of the three Bhakoot doshas and never asking for a birth
time, the night Choghadiya being unreachable, the lucky time repeating across
four rashis at once, and the recent-search chip on Guna Milan never having
worked. All four ad formats were confirmed serving on a device in the same pass.

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

**That release shipped inside versionCode 10.** The 5 → 6 migration it carries
has since run on a device that already had data, with saved profiles intact —
so the warning that used to stand here is discharged. The habit it asked for is
not: check a release on a real device in both languages and both themes before
uploading it.

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

- **The Firebase API key has no application restriction — do this with a device
  in hand.** Google Cloud Console → Credentials → *Android key (auto created by
  Firebase)* says "This key can currently be used with any application", while
  its 25 permitted APIs include Firebase AI Logic and Identity Toolkit. Anyone
  holding the key — and it is inside every APK — can call those from anywhere,
  billed here. The steps, in order:

  1. Play Console → **App signing**: copy *every* SHA-1 — classical,
     post-quantum, and the previous key under "Previous app signing keys".
     Installs from before the 25 Aug 2026 key upgrade still present the old one.
  2. Cloud Console → Android key → **Application restrictions → Android apps**.
  3. Add package `com.aistudio.astroveda.kpvqzm` with each SHA-1, and the debug
     SHA-1 too if debug builds should keep working.
  4. Wait five minutes, then on a device check **the AI answer and Google
     Sign-In**. Those are the two that break first.
  5. Anything broken: set Application restrictions back to **None**. It takes
     effect in minutes.

  Deliberately not done in the same pass that found it: an incomplete SHA-1 list
  takes down AI and sign-in, and the failure does not say why.


- **Two Play Console items go with the privacy policy.** The listing's
  privacy-policy URL is still the pre-rename
  `github.com/sksaini946230-rgb/AstroVeda/blob/main/docs/PRIVACY_POLICY.md`,
  which works only through GitHub's redirect; it should be the `Revati` path.
  And the Data safety form does not declare the text of AI questions, which is
  sent to Firebase AI Logic. *App activity → Other user-generated content*,
  optional, for app functionality, is the category that fits. That one is a
  judgement on the owner's attestation to Google, not a fix to make from here.
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

**Four ad formats now, and one gate between the two that cover the screen.**
Banner and interstitial were the only ones; App Open and Rewarded were added on
the owner's instruction after he created the units.

    banner        anchored adaptive, refreshes every 60s while on screen
    interstitial  tab change, 45s / 120s / 5 per session
    app open      return to the foreground, never on a cold start
    rewarded      opt-in, in front of the Guna Milan PDF

All four were confirmed serving on a real device on 7 Sep 2026: the banner
filled in landscape and refreshed on the minute, the interstitial appeared on a
tab change, the app-open ad appeared on a return to the foreground and *not* on
a cold start, and the rewarded ad played on the PDF button and produced the
report — as did the "just make it" button beside it.

Both new units answered `code=0 Internal error` for the first couple of hours
after being created and then began serving. That is propagation, not wiring: the
other formats were loading from the same app id throughout. Do not go looking
for a bug in the first hours of a new ad unit.

`FullScreenAdGate` is what stops the interstitial and the app-open ad arriving
together — they fire on unrelated events (a tab change, a return to the
foreground), so "come back to the app and change tab" was two full-screen ads
back to back. One at a time, and a 60s floor between any two of them.

`AdIds.resolve` decides what may be asked for. Debug always uses Google's test
units — development traffic on a live unit is what invalid-traffic enforcement
looks for, and the account is what is at risk. Release refuses a test unit, the
`NOT_CONFIGURED` sentinel, and anything not shaped like `ca-app-pub-…/…`,
because `.env.example` holds test ids and the secrets plugin falls back to it
for any key `.env` does not define — so a release built without `.env` would
otherwise serve test ads to real users, earn nothing, and look like it worked.
An unresolvable id means the placement is simply absent. `AdIdsTest` pins it.

**The rewarded ad must never cost the user the report.** The PDF has always been
free and is the output of a calculation they already ran. `RewardedAdManager`
reports the reward as earned whenever there is no ad to show, so a no-fill is
invisible: only an ad that actually played and was actually abandoned counts as
a refusal. The dialog offers "Watch ad" and "Just make it", and both produce the
report; PRO users never see it.

**An app-open ad cannot be shown from `onStart`, and barely from `onResume`.**
The SDK refuses with *"The ad can not be shown when app is not in foreground"*
and, as first written, that refusal also threw the loaded ad away. `onStart` is
where the background-to-foreground transition can be *detected*, but the process
has not reached foreground importance yet; `onResume` is closer and still too
early. It is posted 600ms after resume now, with one retry at 1.2s, and a show
failure keeps the ad — the response is valid for four hours, so discarding it
meant paying for a load and binning it.

Two false alarms are worth recording alongside that, because both looked like
the same bug. Bringing the app back with `adb shell am start` while the screen
is **asleep** produces exactly that message and is correct behaviour: check
`dumpsys power | grep mWakefulness` before believing it. And the message only
became visible at all because these callbacks log at error level.

**The app-open ad never shows on a cold start.** Google's guidance is that it
belongs over a loading screen someone is already waiting through, not in front
of an app they just launched — that is how these get reported as disruptive. It
also needs the process's foreground state, which an Activity cannot see, so it
is registered from `RevatiApp` and counts started Activities rather than using
ProcessLifecycleOwner: a rotation never drops the count to zero, so returning
from a rotation cannot be mistaken for returning from the launcher. Four-hour
expiry, because that is Google's documented validity window for the response.

**Every ad callback logs at error level, and that is deliberate.** The test
device keeps nothing below E — a dump of its buffer holds thousands of E lines
and not one W. `AdBanner` logged its failure at warning, so the message its own
comment called worth keeping was invisible in the only place anyone reads it;
the interstitial logged nothing at all. Both say `loaded` or `load failed:
code=… msg=…` now, and that is how `code=3 No fill` was identified in one run
instead of being guessed at.

**No fill is a fact about the minute, not the session.** The banner used to give
up after three retries and render nothing until the app restarted; the
interstitial retried only when a tab change happened to pass all three gates,
which during the first 45 seconds never happens. Both retry on a bounded
schedule now. On 7 Sep 2026 the banner took `No fill` for twenty minutes
straight in portrait while the interstitial filled immediately and the banner
itself had filled in landscape — demand is thin and uneven for a new app, which
is exactly why giving up is the wrong response.

**A failed `uploadCrashlyticsMappingFileRelease` does not mean a failed build.**
That task runs *after* `packageRelease` and `bundleRelease`, so the APK and the
AAB are already on disk when it fails. It failed repeatedly on 7 Sep 2026 with
`SocketException: Connection reset by peer` and `Broken pipe` while the machine
was on a tethered connection — the host answers, the upload does not complete.
The consequence is limited and worth knowing: Play deobfuscates its own crash
reports from the mapping inside the bundle
(`BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`), so
Play Console is unaffected; only **Firebase Crashlytics** lacks the mapping for
that build until the task is re-run on a working network. Check the artifact's
timestamp before treating a red build as a broken one.

**Ads cannot be tested on the iPhone hotspot.** It resolves
`googleads.g.doubleclick.net` and `pagead2.googlesyndication.com` to 127.0.0.1,
so every request fails and it looks like the integration is broken. `adb shell
ping pagead2.googlesyndication.com` answers in 0.1ms when this is happening.
Switch the phone to normal WiFi before concluding anything about ads.

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
