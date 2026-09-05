# Release notes — how to write them, and what has been said

Play takes release notes per language, wrapped in locale tags, **500 characters
maximum each**. This app ships `en-US` and `hi-IN`, and Play will not let a
release out with only one of them filled in.

## The shape

```
<en-US>
…
</en-US>
<hi-IN>
…
</hi-IN>
```

## What belongs in them

Release notes are read by someone deciding whether to press Update. They are not
a changelog for the developer — the git history is that, and it is better at it.

- **Say what changed for them, not what changed in the code.** "Daily horoscope
  now differs for every rashi" is a reason to update. "Fixed a derived value
  that cancelled out" is not.
- **Lead with the thing they would notice.** If a reading was wrong and is now
  right, that is the first line.
- **Both languages carry the same promises.** A Hindi user reading a shorter,
  vaguer version of the English notes has been told less, and this app's whole
  point is that neither language is the afterthought.
- **Do not claim what is not there yet.** PRO is not purchasable until the
  merchant account exists; notes must not imply otherwise.
- **Keep the greeting.** "नमस्ते 🙏" costs eight characters and sets the tone the
  rest of the app keeps.

## Reusable skeleton

```
<en-US>
Namaste 🙏

<one line on the biggest user-visible change>

• <change>
• <change>
• <change>

Thank you for using Revati.
</en-US>
<hi-IN>
नमस्ते 🙏

<सबसे बड़े बदलाव की एक पंक्ति>

• <बदलाव>
• <बदलाव>
• <बदलाव>

रेवती चुनने के लिए धन्यवाद।
</hi-IN>
```

## Said so far

### versionCode 9 / 1.4

Everything since the 1.2 that production is on: the per-rashi horoscope fix,
numerology no longer pre-filled with someone else's details, profile
export/import, in-app support, narrow-screen layout fixes, and notifications and
widgets finally honouring the chosen language.

```
<en-US>
Namaste 🙏

This update makes your readings truly your own:

• Daily horoscope now differs for every rashi — rating, lucky colour and stone come from your sign's own lord
• Numerology starts blank, so the result is yours alone
• Export and import your saved kundli profiles — no account needed
• Contact support right from Settings
• Smaller, faster app, and a better fit on compact screens
• Notifications and widgets now follow your chosen language

Thank you for using Revati.
</en-US>
<hi-IN>
नमस्ते 🙏

इस अपडेट में आपका फलादेश सचमुच आपका अपना:

• दैनिक राशिफल अब हर राशि के लिए अलग — रेटिंग, शुभ रंग व रत्न आपकी राशि के स्वामी से
• अंक ज्योतिष अब खाली शुरू होता है, परिणाम केवल आपका
• सहेजी गई कुण्डलियां एक्सपोर्ट व इम्पोर्ट करें — बिना खाते के
• सेटिंग्स से सीधे सहायता संपर्क
• ऐप छोटा व तेज़, छोटी स्क्रीन पर बेहतर
• सूचनाएं व विजेट अब आपकी चुनी हुई भाषा में

रेवती चुनने के लिए धन्यवाद।
</hi-IN>
```
