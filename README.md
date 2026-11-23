# Speakivra 🎧

Android app for English listening, repetition and speaking practice, with a future AI conversation tutor.

## Status
**0.1.0 beta source**. A1 lesson, US English text-to-speech, Android speech recognition intent, demo-only scripted conversation, locally saved lesson progress.

**Important:** No actual AI API or phoneme-level pronunciation scoring is integrated. Speech recognition may require a provider and network connection. Do not interpret recognized text as a pronunciation score.

## Build
- JDK 17
- Android SDK 35
- Gradle 8.9
- Run `gradle :app:assembleDebug` or the GitHub Actions workflow.
- Download the unsigned development debug APK artifact from Actions after a successful run. It is debug-signed by Android tooling; never use it as a production release.

## Security
- No API keys in the repository or APK.
- No AI backend enabled.
- Recording permission requested at use time.
- Local progress stored using SharedPreferences, app backup disabled.
- Android speech recognition providers may handle audio remotely; see the provider privacy policy.

## Roadmap
- Validate device TTS / speech recognition behavior
- Accessible onboarding and placement assessment
- More lessons and adaptive review
- Genuine pronunciation assessment service
- Secure AI gateway and real conversations
- Reproducible signed releases and in-app updates

Repository is public. Never commit signing keys, secrets or user data.
