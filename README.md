# Speakivra 🎧

Android app for English listening, repetition and speaking practice.

## Current source

**0.1.11 beta development** based on verified V0.1.2 commit `09b73aa16e8e9fa0d7d944b757ce430bc1050f55`.
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
