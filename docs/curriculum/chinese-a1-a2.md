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

This is development work, not a published APK. Device microphone accuracy, installed Mandarin voice quality and S24 Ultra visual acceptance still require real-device review.

## Local verification status (2026-11-27)

The 114 JVM tests passed locally (including four Mandarin test methods). App and instrumentation APK builds succeeded; package verification confirmed com.nghialcvn.speakivra, version 1.2.7/code 47 and intact approved icon/font/English audio assets. The final local test/lint/app-build/instrumentation-build run passed (114 JVM tests, zero failures). The existing APK metadata verification script also passed. No release APK has been signed or published.

The six Mandarin Android tests compile, but have not been executed successfully: this workspace has no hardware CPU accelerator, and software-emulated Android did not finish stable startup. No new UI screenshot or real-user Mandarin microphone result is claimed. GitHub write access returned HTTP 403, so this branch has not been pushed and CI has not run.
