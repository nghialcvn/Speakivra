# Play Protect report — 23 November 2026

The user reports another Google warning after V0.1.8. Exact wording, affected APK/version and whether the warning appeared at installation or after a scan are not yet supplied. No cause or false-positive classification is confirmed. This is a source review, not a Google verdict or a malware certification.

Reviewed manifest, Gradle dependencies, update download/installation code and FileProvider configuration:

- Declared permissions are microphone, Internet and user-requested package installation. The native updater introduced REQUEST_INSTALL_PACKAGES in V0.1.6. It is used for the explicitly requested self-update flow. Its presence alone does not establish the reason for Google's warning.
- No SMS, contacts, accessibility-service, notification-listener, all-packages or broad storage permission is declared in the source manifest. CI verifies release identity, non-debuggable build, signer and branded resources. The actual published V0.1.8 APK was also inspected: its merged manifest requests these same three Android permissions plus AndroidX’s app-specific signature-protected DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION. The AndroidX ProfileInstallReceiver is exported but guarded by android.permission.DUMP; DUMP is not requested by the app. The APK targets API 35, uses standard ZIP/APK headers and has no duplicate ZIP entries. Its SHA-256 is 6ea3fd4c93d550bc9ef9ee6a3b29446fdcd6bf4bbd0fe0126f136f5390f43264. This does not establish which file Google flagged.
- The updater accepts official Speakivra release assets over HTTPS, constrains redirects, verifies size/SHA-256, package, increasing version and installed signing certificate. Its provider is not exported and shares only cache/updates files with read access for the Android installer. Installation requires Android user confirmation and source permission. No silent install is implemented.
- Generated speech is packaged audio; no synthesis model, downloaded executable code, ad SDK or new permissions were introduced with the A1 lesson pack.
- Signing and package identity remain unchanged. Do not rotate the key, rename the package or disable Play Protect as a response to this report.

Next evidence needed: screenshot of the full warning including details, affected installed/file version and whether the warning occurs in Speakivra's update flow, browser install or a later scan. Match the warning against Google's categories before changing behavior. If a harmful-app classification remains after a complete review and the app complies with Google's policies, use Google's official Play Protect appeal process for the exact APK. No appeal has been submitted and no external scanning service has received the APK in this review.

Official developer guidance: https://developers.google.com/android/play-protect/warning-dev-guidance
