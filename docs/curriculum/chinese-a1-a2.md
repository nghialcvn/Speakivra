# Mandarin A1/A2 candidate — Speakivra 1.2.7 (47)

The approved addition is a Tiếng Anh / Tiếng Trung selector at the top of Bài học. The existing English flow and approved home/check-in layout remain intact. Chinese levels are an app learning pathway, not an HSK certification claim.

## Content

| Level | Topics | Speaking lines | Scripted exchanges | Short readings | Reading sentences | Comprehension questions | Vietnamese-to-Chinese exercises |
|---|---:|---:|---:|---:|---:|---:|---:|
| A1 | 10 | 80 | 40 | 10 | 40 | 30 | 40 |
| A2 | 15 | 120 | 60 | 15 | 60 | 45 | 60 |

Topic names mirror the current English A1/A2 catalogue. Content is authored for Mandarin, with simplified Hanzi, tone-marked Pinyin, Vietnamese meanings and contextual tips. Each topic's first four speaking lines also form a coherent short reading; its final four lines are reused as translation practice. Each reading has three comprehension questions derived from its scripted exchanges, with Vietnamese answer choices and explicit checking. These are not additional unique sentences. All 25 topics can be opened in their respective level. A1 includes an optional Pinyin/four-tones introduction.

The learner's name is substituted only in the introduction slot. Its spelling is preserved in Pinyin rather than inventing a Mandarin transliteration. No English proper-name changes are included.

## Speech

Recognition requests `zh-CN` through the existing foreground SpeechCapture lifecycle, pipe fallback and temporary RAM audio. Recognition service availability, network needs and actual accuracy depend on the installed service. Playback uses an installed offline `zh-CN` TTS voice; no paid service, new cloud audio upload, bundled Mandarin clips or automatic voice download is added. When unavailable, the app explains how to install Chinese voice data. The learner can audition/select installed voices. Their gender is not inferred from Android voice IDs. Speeds are 0.75, 1, 1.25 with 0.75 as default.

Hanzi aligns by character, Latin names by whole token. Spacing and punctuation are ignored, Latin accents are folded, and bounded Arabic integers normalize to Chinese numerals. Extra recognized content lowers the displayed match score. Added/omitted Chinese negations block continuation. Spoken repetition/chat/readings preserve the existing strict less-than-60%-mismatch threshold; translations require full bidirectional content equality. Scores measure recognition text matching, never tone/acoustic pronunciation accuracy.

## State and migration

Chinese has `zh_a1_` / `zh_a2_` lesson IDs and stable individual content IDs. It uses the existing per-profile preferences and cloud ownership boundary. Learned phrase/turn IDs and best diamond scores use the existing keys and merge semantics; reading/exercise IDs use the existing `extra.completed_ids` set. Export/import now whitelist known Chinese IDs. Device voice/language preferences and all audio stay out of cloud payloads. English IDs, existing progress, achievement thresholds and update index are unchanged.

Chinese progress/diamonds are displayed within the Chinese course. The home Continue action, existing global English progress screen and profile English totals retain their current English behaviour; selecting Chinese affects Bài học. Reopening a section resumes its first unfinished item; completed sections can be replayed. Future appended phrase IDs do not erase previously earned progress or completion history.

## Verification

New JVM tests cover content/ID integrity, English catalogue isolation, negations, insertion penalties, numeric formatting, learner names and cloud round trips. Android tests cover language selection, both levels, typed translations, per-profile isolation, continued learning, large-font dark reading/conversation and screenshots 160–166. A separate recognizer protocol fixture verifies the `zh-CN` request, final result gating and result cleanup, without claiming real Mandarin accuracy. Existing Android check groups and screenshots remain required. The established SwiftShader/Vulkan-disabled emulator setup is retained.

V1.2.7 Beta is approved for release so the user can test on the S24 Ultra. Device microphone accuracy, installed Mandarin voice quality and final S24 Ultra acceptance remain real-device checks.

## Verified candidate and approved release (2026-11-27)

Candidate: `a7867e8099d7c09c9aa31912d7614fde298f490a`.

[CI run 46030698099](https://github.com/nghialcvn/Speakivra/actions/runs/46030698099) passed 114 JVM tests, lint, APK identity/asset verification, Firebase ownership checks and 37 Android tests (all 31 earlier checks plus six Chinese tests). All 20 required screenshots were captured. Actual APK captures were inspected for A1/A2, typed translation, conversation/reading at fontScale 1.5 in dark mode, recognition feedback, comprehension and the compact check-in card. Artifact ZIP SHA-256: `d3de00d6a76745c1a56469284287d4cc62af86dcab629546a65a5fd173b0f07b`.

The user explicitly requested release to test on their phone. The V1.2.7 release workflow pins the verified candidate and CI run, permits only this workflow/release-documentation change, uses the original signing secrets, verifies the signer against the current published update index, rejects an existing release tag and updates the index only after publication.

Recognition protocol tests use a synthetic test provider and do not establish real Mandarin accuracy. Real S24 Ultra microphone and installed TTS voice review remain pending. No claim is made that Play Protect warnings have been resolved.
