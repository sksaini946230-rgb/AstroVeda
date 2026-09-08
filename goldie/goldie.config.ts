/**
 * Play Store screenshots for Revati.
 *
 * The raw screens under out/raw/pixel-10-pro are captured from a real phone
 * with adb, not from an emulator — there is no AVD on this machine, and the
 * device renders at 1080x2400 (wm size/density override) with the system UI in
 * demo mode so the status bar is a clean 9:30 / full battery / full signal.
 * That is why there are no `flow` fields here: `goldie capture` is skipped and
 * the manifest is written by hand. Everything from `goldie frame` onward is
 * the normal pipeline.
 *
 * Nothing in these screens is real user data. The chart is "Aarav Sharma",
 * the match is "Rahul & Priya", and the city is Jaipur.
 */
const config = {
  appRoot: "/Users/sksaini/AstroVeda",
  bundleId: "com.aistudio.astroveda.kpvqzm",
  android: {
    appPath: "/Users/sksaini/AstroVeda/app/build/outputs/apk/release/app-release.apk",
    applicationId: "com.aistudio.astroveda.kpvqzm",
  },

  // Only the Android device is built here, and it is framed with goldie's
  // bundled Pixel 10 Pro bezel — this key is still required, and its iPhone
  // art is unused.
  frame: { variant: "17-pro-silver" },

  devices: ["pixel-10-pro"],
  locales: ["en-US"],
  appearance: "dark",

  theme: {
    // The app's own night sky, so the tiles and the screens are one object
    // rather than a light frame around a dark app.
    background: "linear-gradient(165deg, #05070F 0%, #131A33 48%, #2A1B3A 100%)",
    headlineColor: "#F2D48B",
    subheadColor: "#AEB7D4",
    fontFamily: "Montserrat",
    copyHeightRatio: 0.26,
    deviceWidthRatio: 0.82,
    // Every layout here spans one tile. "editorial" was tried first and gave
    // ten images for eight scenes, because its panorama layout splits one
    // screen across two tiles — fine for a strip someone scrolls, wrong for a
    // Play listing that has to be exactly eight self-contained screenshots.
    template: ["hero", "classic", "tilt", "classic", "offset", "classic", "tilt-right", "hero"],
    layout: "classic",
  },

  store: {
    name: "Revati",
    subtitle: { "en-US": "Kundli & Panchang" },
    developer: "Msunjay Enterprises",
    category: "Lifestyle",
    rating: 4.8,
    ratingCount: "New",
    ageRating: "3+",
    price: "Free",
    description: {
      "en-US":
        "Revati is a Vedic Panchang and Kundli app that works entirely on your phone. Planetary positions come from the Meeus algorithms with the Lahiri ayanamsa, computed on the device — no internet needed for the Panchang, Choghadiya, birth chart or Guna Milan.\n\nDaily Panchang for your own city, horoscope for all twelve rashis, North and South Indian birth charts, 36-guna matching with Nadi, Bhakoot and Mangal dosha, numerology, and a Hindu festival calendar that computes dates from each festival's tithi rule. Hindi and English throughout.",
    },
  },

  scenes: [
    {
      kind: "screenshot",
      id: "panchang",
      headline: { "en-US": "Today's Panchang, on your phone" },
      subhead: { "en-US": "Tithi, Nakshatra, Yoga and Karana for your own city — no internet needed." },
    },
    {
      kind: "screenshot",
      id: "elements",
      headline: { "en-US": "Every element, at a glance" },
      subhead: { "en-US": "Sunrise, sunset, Rahu Kaal, Abhijit and Brahma Muhurta in one view." },
    },
    {
      kind: "screenshot",
      id: "choghadiya",
      headline: { "en-US": "Know the right moment" },
      subhead: { "en-US": "Day and night Choghadiya, plus muhurat for weddings and new beginnings." },
    },
    {
      kind: "screenshot",
      id: "horoscope",
      headline: { "en-US": "A reading that is actually yours" },
      subhead: { "en-US": "All 12 rashis — daily, weekly and monthly, with lucky number, colour and stone." },
    },
    {
      kind: "screenshot",
      id: "kundali",
      headline: { "en-US": "Your birth chart in seconds" },
      subhead: { "en-US": "North and South Indian charts, with lagna, moon sign, nakshatra and dasha." },
    },
    {
      kind: "screenshot",
      id: "matching",
      headline: { "en-US": "36 gunas, honestly scored" },
      subhead: { "en-US": "Full Ashtakoot matching with Nadi, Bhakoot and Mangal dosha analysis." },
    },
    {
      kind: "screenshot",
      id: "numerology",
      headline: { "en-US": "Numbers, and what they mean" },
      subhead: { "en-US": "Moolank and Bhagyank from your name and date of birth, explained simply." },
    },
    {
      kind: "screenshot",
      id: "festivals",
      headline: { "en-US": "Never miss a festival or fast" },
      subhead: { "en-US": "Festival dates computed from each tithi rule, years ahead — not a fixed table." },
    },
  ],
};

export default config;
