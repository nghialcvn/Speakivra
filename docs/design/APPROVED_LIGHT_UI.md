# Approved light interface — V0.1.10

The user approved the six-screen proposal on 23 November 2026 and asked to preserve its layout, voice and formatting. The source board is bundled unchanged as `res/drawable-nodpi/approved_light_demo.png`. It is the visual reference for subsequent changes. No replacement logo, avatar or headphone artwork was generated for implementation.

## Screen mapping

- Home: original mark/wordmark/tagline; Chào Nghĩa!; peach daily-goal card with original headphone pixels; lavender current lesson and progress; mint topic and lavender scripted-conversation shortcuts.
- A1 lessons: the approved numbered rows, outline topic icons, Vietnamese/English names, all ten actual topics; selected lesson and real saved position.
- Speaking Studio: back header, lesson progress, automatic/manual selector, centered English/Vietnamese phrase, speaker/speed/translation controls, large coral microphone, decorative static waves, mint pronunciation tip and pinned next action. Recognition text is shown only after actual recognition. Automatic mode retains its existing sample-playback behavior.
- Scripted conversation: Emma artwork taken directly from the approved board, explicit simulation pill, lavender prompt, mint suggested response, microphone CTA, sample-answer audio and real turn counter. Existing next/translation controls remain available as content below the approved conversation composition.
- Review: progress, speaker/question card, three meaning choices, feedback only after answering, real score in the existing summary. The existing optional spoken repetition remains available in feedback. No fabricated pronunciation metric.
- Personal/settings: approved initial/avatar/name, light/dark selector, US-English voice information, saved speech-rate selection, saved daily-goal selection and actual version/update card. The bottom Cập nhật destination opens this page, as shown in the proposal; Kiểm tra cập nhật opens the original download/verify/install screen and starts a check.

## Preserved behavior

Application ID, signing configuration, icon/approved logo, 110 audio clips, ten-lesson content, progress keys and update security checks are retained. Normal speed is still the V0.1.9 calibration: 1x equals the former 0.75x. New preferences are `dark_theme` (default false), `daily_goal` (default 20) and `automatic_mode` (default true). They do not clear or rewrite lesson progress.

Light mode uses warm ivory, coral, lavender and mint. The approved dark choice uses the same hierarchy with warm charcoal surfaces, not the old navy palette. System bar foregrounds follow the selected mode. Nunito variable font, bundled under its OFL license from the official google/fonts repository, supports Vietnamese text without a font download at runtime. Only small illustrative portions of the approved board are painted; all interface text, icons, controls and progress are native Compose, interactive and backed by actual state.

Insets, scroll areas and minimum touch targets accommodate Samsung system bars and longer lesson content. Controls wrap only when available width or large accessibility text requires it. The next action stays visible during speaking/review. Back navigation returns without resetting progress.

Build/lint/unit checks are necessary, but they do not constitute a pixel comparison or actual Samsung device test. Play Protect status remains unresolved pending the reported warning screenshot; this visual update is not represented as a Play Protect fix.

Font source: https://github.com/google/fonts/tree/main/ofl/nunito

The optional `ui_check` manual workflow input runs instrumented UI checks on a disposable API 35 emulator before publishing. It captures real rendered screens, checks theme/speed controls and verifies that navigation preserves lesson position. It is off by default to avoid adding emulator cost to unrelated releases. Test fixture state is confined to the CI emulator; app update logic does not seed or clear learner data.

A build with `ui_check` enabled intentionally stops at verified artifacts, so the rendered screens can be inspected before release. The separate manual Publish reviewed APK workflow reuses that exact APK without rebuilding; it requires a successful UI run at the current source commit, re-verifies the bytes and signing certificate, and publishes the direct APK. Ordinary small releases can still use the existing single build/publish path with `ui_check` off.

## Approved extension — V0.1.12

The revised four-screen demo, approved by the user, is bundled unchanged as approved_profiles_demo.png. Its exact six face regions are drawn by native Compose. The startup picker and name/avatar form use real profiles and saved progress; the sample people/progress from the demo are not seeded as fictitious data. Only the original learner Nghĩa is initialized to retain prior progress. The profile page provides Đổi người học.

Speaking retains the existing composition, sample translation/controls, large mic, mint tip, result row and pinned Next. The color legend and recognized transcript card are removed per the revision. Reference words become green or red only after final recognition. The compact percentage/action row and honest word-recognition footer remain. Next is disabled at >=60% unmatched, until a final result exists, and while a capture is active. All other approved screens and branding remain unchanged.

## Approved correction — V0.1.13

The user approved the profile-editing and spoken-result demo. Replace the picker wordmark with the existing logo, render avatars from the separate high-resolution learner_avatars atlas, and expose a pencil at the lower right of the profile avatar. The edit form preserves the creation form layout and changes only its title, prefilled values and save label. Show actual final spoken text in italic beside a square avatar in a compact 6dp-corner row directly below the mic. Learner introduction templates use the current profile name in English/Vietnamese; other lesson content stays intact. Matching colors and the 60% gate are retained. No other screen or brand changes are authorized.

## Approved compact speaking controls and A1 expansion — V0.1.14

The user's approved demo retains the original speaking composition, typography, background, micro, word-feedback row, tip and fixed bottom action. Inside the practice phrase card only, “Nghe mẫu”, one speed-selector displaying the current value and “Ẩn dịch/Hiện dịch” share one horizontal row. The three speed values 0.75x, 1x and 1.25x open from the selector; its selected underlying calibration remains unchanged. For narrow devices or high accessibility font scale, controls may wrap rather than clip. All other approved screens remain unchanged.

Content is additive: keep the first eight phrases and first three scripted turns for each original A1 lesson in their original positions, then append four practice phrases and two scripted turns per lesson. This brings practice to 120 phrases and 50 turns across the same 10 IDs. Existing bundled neural clips are unchanged; appended lines use on-device US-English TTS. The 30 meaning review questions are retained. Do not reset any learner preferences/progress or imply real AI conversation/acoustic pronunciation scoring.

## Approved neon voice extension — V0.1.15

On 23 November 2026 the user approved the shared dark-neon voice/settings mockups as an **additional UI style for the new voice-selection experience**. Preserve the accepted light/dark study screens, existing logo, palette, lesson flow, score/feedback behavior, spacing, profile data, and installed APK update logic. The new voice configuration screen uses the mockup's deep-navy background, bright blue selected borders, rounded voice cards, and discrete listening controls. Study/conversation screens add only compact accent/voice chips directing to this configuration screen; do not replace existing lesson screens wholesale.

Accent options refer to pronunciation and recognition locale, **not** to the display/translation language: English US (en-US) and English UK (en-GB), with male/female voice-selection labels. Android TextToSpeech Voice has no authoritative gender attribute. Hence a user must listen to installed offline US/UK voice candidates and explicitly assign one to the chosen label. Never advertise an inferred gender, synthesize a clip using the wrong accent, require paid APIs, or silently substitute another voice. When unavailable, show a setup/error message without fabricating real speech.

The original US female neural clip remains the default. Every other selection uses the exact installed TTS voice the learner explicitly assigned; newly personalized sentences also use TTS. Save locale, gender label, and chosen TTS voice in each learner's existing preference file without renaming old lesson progress keys. Existing 1x playback calibration, 120 phrases, 50 scripted conversation turns and 30 review questions are unchanged. These changes still require emulator and Samsung verification.
