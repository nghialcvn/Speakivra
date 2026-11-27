# Speakivra 1.2.8 review candidate

## Approved scope

The user approved the three-screen language selection proposal and requested a full
Mandarin programme matching the existing English curriculum, including Front Office
and Housekeeping. The approved home, attendance card, logo, Nunito font, colours,
Light/Dark/Neon treatments and five navigation destinations are retained.

Opening the app presents English and Chinese cards. A language choice precedes the
shared learner picker. Changing language from the home header retains the learner;
only that language's courses, practice, resume position and progress are shown.
Accounts, profile identity/avatar, attendance dates, reminders and appearance are shared.
The old English learned IDs, scoring and lesson order are untouched.

## Curriculum parity

Source: main `4c353a2c69d10a7f48dc3183c9862f704d7e9910` (released 1.2.7).
Counts were exported from the runtime catalogue, including hotel exercise packs;
they are not estimates from release notes.

| Area | Topics | Speaking sentences | Dialogue turns | Reading packs / sentences | Translation packs / questions |
| --- | ---: | ---: | ---: | ---: | ---: |
| A1 | 10 | 200 | 70 | 5 / 20 | 5 / 30 |
| A2 | 15 | 450 | 150 | 10 / 100 | 20 / 100 |
| B1 | 20 | 700 | 300 | 10 / 100 | 20 / 100 |
| B2 | 20 | 700 | 300 | 10 / 100 | 20 / 100 |
| C1 | 20 | 700 | 300 | 10 / 100 | 20 / 100 |
| C2 | 20 | 700 | 300 | 10 / 100 | 20 / 100 |
| Front Office | 20 | 500 | 120 | 0 / 0 | 20 / 500 |
| Housekeeping | 20 | 500 | 120 | 0 / 0 | 20 / 500 |
| Total | 145 | 4450 | 1660 | 55 / 520 | 145 / 1530 |

Chinese retains the original 25 A1/A2 topics' first eight sentences, first four
turns and stable completion IDs. The original 25 short readings (100 sentences),
25 short exercise sets (100 questions) and 75 comprehension checks remain as
supplemental practice rather than being deleted to force exact totals. Full course
meaning checks total 435. No lessons are duplicated merely to fill counts.

The B1 → B2 → C1 → C2 progression uses earned completion history. Replaying or adding
content does not revoke an earned level. Existing A1/A2 access is preserved. New
content resumes at the first unfinished stable ID, including a new reading pack
after a completed starter reading. Historical starter diamonds are retained; the
existing score thresholds and diamond artwork are used.

## Mandarin content and speech

Mandarin uses simplified characters, Pinyin and Vietnamese meanings. A1–C2 are
internal learning stages, not a claim of HSK certification. Hotel dialogues are
reviewed by situation; advanced levels use reviewed cases and increasingly demanding
response patterns. Chinese-specific pronunciation/learning wording replaces English
specific grammar references where needed.

Initial machine-assisted drafts were not shipped directly. Every translation unit
used by the generator has an explicit reviewed Chinese value in the committed review
files. The generator rejects missing/unreviewed units. Pinyin is generated with phrase
pronunciation overrides and common 一/不 tone changes. This review was performed by
the assistant; independent native-speaker review is not claimed.

Recognition uses zh-CN; English keeps its original recognition path. Matching covers
the whole advanced passage instead of silently truncating after 512 units. Chinese
negations remain significant. Translation alternatives use their own displayed text
and Pinyin. Audio recordings remain temporary, never autoplay, and are discarded when
changing context. Whole-passage capture uses the existing four-minute maximum.
Offline Mandarin TTS requires installed Chinese voice data, as in 1.2.7. Users can
listen and assign actual voices to male/female slots; gender is never inferred.
No model, paid translation API, API key or permanent recording is added to the APK.

## Validation and publication

The candidate workflow retains the established isolated emulator groups, SwiftShader,
Vulkan disabling and dp-equivalent display configuration. It adds real app startup,
language switching, two home screens, full catalogue/hotel and upgrade-resume checks.
The UI script requires all legacy tests and screenshots in addition to new coverage.
Build success alone does not approve the appearance. Inspect the actual APK screenshots
170–175 alongside the approved demo, and preserve the existing check-in screenshots.

This development branch does not publish a release or modify updates/latest.json.
Real microphone/accent and TTS behaviour on the user's Galaxy S24 Ultra still require
device testing; synthetic recognition fixtures are protocol/logic tests only.
