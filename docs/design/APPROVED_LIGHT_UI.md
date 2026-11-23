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
