# Speakivra 🎧

Android app for English listening, repetition and speaking practice.

## Current source

**1.0.3 beta candidate**, continuing the published V1.0.2 release. This candidate adds current-sentence replay, stable content progress IDs and Neon artwork matching the approved reference. Publication requires signed release verification, emulator checks and screenshot review.
The source version is not a claim that an APK has passed device testing or been released.

The user-approved warm light interface is implemented; the approved logo is preserved. See [approved design and behavior mapping](docs/design/APPROVED_LIGHT_UI.md). The adaptive launcher foreground now uses 24dp insets (60dp artwork on the standard 108dp layer) to fit inside the 66dp safe zone. Ten A1 practice lessons, bundled offline US-English neural speech for current prompts, installed US-English TTS fallback, Android speech recognition, Vietnamese translations and local progress are implemented. See [speech provenance and generation](docs/SPEECH.md). Conversation is a fixed script, not real AI. Text recognition is not pronunciation scoring. Automatic listen-record-feedback progression and placement assessment and curricula beyond this initial A1 pack remain incomplete.

## Updates and signing

- Automatic startup check and manual check share update state.
- Public beta releases are supported. The update dialog shows release notes, downloads the official APK inside the app with progress/cancel/retry, verifies its size, hash, package/version and signer, then opens the Android installer. Android requires source permission and installation confirmation.
- Metadata checks package, versionCode and certificate before offering an update; downloaded bytes and actual APK identity are checked again before installation. Different signing keys block the install suggestion so users are not told to uninstall and lose data.
- `REQUEST_INSTALL_PACKAGES` is used only for user-requested self-updates. A non-exported FileProvider grants read access only to the staged APK in `cache/updates/`. No storage permissions, API keys or paid services.
- Fixed release signing uses GitHub Secrets. Releases fail closed when signing is absent or the certificate differs from the pinned fingerprint.
- V0.1.2 used a runner debug key. Seamless migration to a different release key is **not verified or guaranteed**.

See [signing setup and Samsung verification](docs/SIGNING.md).

## Build

JDK 17, Android SDK 35/build-tools 35.0.0, Gradle 8.9.

```sh
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Main source changes trigger one debug verification workflow. Manual release builds use the fixed key and publish a direct APK plus update metadata to GitHub Releases after verification. Gradle dependency/build caching is enabled. Release publishing is manual to avoid duplicate builds. Never use debug artifacts as production releases.

## Data and privacy

Application ID remains `com.nghialcvn.speakivra`. Progress remains in `speakivra_progress` SharedPreferences (`phrase`, `completed`); updates do not reset it. Android backup remains disabled. Do not uninstall or clear app storage during an update.

Microphone permission is requested when practicing speech. Speech recognition providers may need a network connection and handle audio remotely. No AI backend is enabled. Update checks contact GitHub; the current installation certificate and progress are compared locally and are not uploaded.

Repository stays public. Never commit signing keys, passwords, user data or API keys.

## A1 lesson pack

10 topics, 80 practice phrases, 30 scripted conversation turns, 30 meaning checks and 110 bundled voice clips. Each lesson saves its own progress. See [lesson content and migration](docs/LESSONS.md). Conversations remain scripted; review results are not pronunciation scores.

Light mode is the default. The personal/settings page provides a saved warm dark option, speech rate and daily goal. Font and approved illustration assets are bundled; no new permissions or paid services.

Speech capture uses SpeechRecognizer directly inside the approved learning screens, with partial/final transcripts, tap-to-stop and cancellation on navigation/background. No external recognition activity is opened. The configured Android provider may require network access; offline availability depends on its installed language models. Recognition does not save an audio recording or grade pronunciation.

## Local learner profiles and word feedback (V0.1.12)

Choose a learner at startup; add a name and one of the six approved avatars without a password. Each learner has a separate progress/settings preference file. The first learner uses the original progress file in place; no existing data is cleared. Appearance and update state remain app-wide. Switch learners from Personal/settings.

Speaking Studio colors reference words using ordered matching of the final recognition result, with case/punctuation ignored. Extra recognized words cannot substitute for missing reference words. At least 60% unmatched reference words blocks Next; below 60% allows Next. No final result, an error, a new attempt, or a different phrase cannot reuse a previous pass. This is word recognition feedback, not acoustic pronunciation assessment. The approved legend and recognition transcript panel are absent.

## Approved profile editing and personalization (V0.1.13)

The avatar atlas has six equal 512px cells with centered faces, replacing tiny crops from the UI board; the app shares one decoded atlas. The approved brand mark is shown on the learner picker. Personal/settings has a pencil action opening the same name/avatar form in edit mode. Updating a registry entry retains its immutable ID and its preference file, so progress is unchanged.

Name templates are resolved from the selected learner for practice, conversations, translations and review content without changing lesson IDs or ordering. Renaming immediately updates these templates. Personalized sentences use the installed offline US-English TTS fallback rather than playing an Alex recording; other clips remain bundled. No new audio API or paid service is used. Matching folds Vietnamese diacritics and accepts I am / I'm as equivalent text for the personalized introduction.

Final recognized words appear in a compact italic transcript row with a small square avatar below the practice microphone. Partial text is not shown in this row, and a new attempt/context clears it. The 60% rule remains unchanged and is not an acoustic pronunciation assessment.

## Expanded A1 practice and compact lesson controls (0.1.14 candidate)

The existing ten topics now include 120 practice phrases and 50 scripted conversation turns (12 phrases and 5 turns per topic). The original 80 phrases and first 30 turns retain their positions, lesson IDs, and original bundled neural speech clips. All 40 added practice lines and 20 new turn prompts use the existing on-device US-English TTS fallback until separately recorded neural clips are reviewed. The 30 meaning checks remain unchanged; no curriculum claims beyond A1. Existing learner preferences, completed rounds and update/signing settings are unchanged. The approved speaking phrase card now uses a single current-speed dropdown next to Nghe mẫu and Ẩn dịch/Hiện dịch on one row. This candidate is not yet a signed or Samsung-tested APK release.

## Reviewed neon voice extension (0.1.15 candidate)

The user approved a dark-neon voice-settings experience in addition to existing study screens. The new optional selector offers US or UK pronunciation, shows user-selected male/female *labels* and lets learners listen to and explicitly assign installed offline Android TTS voices. Android does not expose authoritative voice gender; an unsupported/unassigned combination is not falsely announced as an available male/female voice. The original bundled US female neural voice remains the default, and the previously approved 110 Ogg files are unchanged. Microphone recognition requests the selected `en-US` or `en-GB` locale; provider support varies by device. The accent/voice selection is stored only inside each existing learner preferences file. The learning layout and progress data are unchanged; this is source code until a new Android build and UI review pass.

## Current-sentence replay and content migration (V1.0.3)

On Android 13+, the microphone captures up to 60 seconds of mono 16 kHz PCM in RAM. Tap again to finish. The same samples are passed to Android SpeechRecognizer through EXTRA_AUDIO_SOURCE; the configured provider may require network access. Replay uses AudioTrack only after the learner taps the speaker next to their avatar. It never uses TTS to imitate a recording. A new attempt, navigation, learner/context changes and backgrounding stop playback and discard the buffer. No replay file, media-library item or server sync is created. Android 8–12 retain the previous recognition path without replay.

Content declarations have explicit immutable IDs, retained during name personalization. `LegacyContentIds.kt` freezes V1.0.2 ordering for migration of positional preferences; do not regenerate it when adding content. Learned ID sets are saved per learner and lesson. A completed lesson with new content resumes at its first unlearned ID, retaining old rounds. Keep IDs unchanged when editing or reordering existing lines. A1 remains 15 topics / 300 phrases / 100 scripted turns; A2 content is unchanged.

The three transparent Neon raster assets were extracted from the user-approved home reference using image generation (background-extraction prompts preserving the pictured logo, headphones and silver classic microphone). No generated art implements speech, assessment or navigation behavior.
