# Speakivra 🎧

Android app for English listening, repetition and speaking practice.

## Current source

**0.1.4 beta development** based on verified V0.1.2 commit `09b73aa16e8e9fa0d7d944b757ce430bc1050f55`.
The source version is not a claim that an APK has passed device testing or been released.

Existing learning UI, approved logo and colors are preserved. The adaptive launcher foreground now uses 24dp insets (60dp artwork on the standard 108dp layer) to fit inside the 66dp safe zone. A1 sample lesson, US English TTS, Android speech recognition, Vietnamese translations and local progress are implemented. Conversation is a fixed script, not real AI. Text recognition is not pronunciation scoring. Automatic listen-record-feedback progression, Light Mode, placement assessment and full curriculum remain incomplete.

## Updates and signing

- Automatic startup check and manual check share update state.
- Public beta releases are supported. The update dialog shows release notes and opens the official APK directly in the browser; Android requires installation confirmation.
- Metadata checks package, versionCode and certificate before offering an update. Different signing keys block the install suggestion so users are not told to uninstall and lose data.
- No install-packages permission, API keys or paid services.
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
