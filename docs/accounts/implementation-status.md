# Speakivra Spark account integration

This is a release candidate under review, not a published account release. Current public version remains 1.1.7 (code 37). Do not change the update feed until Android checks and provider configuration are complete.

## Provisioned project

- Owner-authorized dedicated project: `speakivra-nghialcvn`, number `941786313291`.
- Android package `com.nghialcvn.speakivra`, app `1:941786313291:android:58accdfe5fc0ae870b13ce`.
- Release SHA-1/SHA-256 checked against the pinned APK certificate and registered.
- Google and email/password providers deployed; public Android SDK config has Android and web OAuth clients.
- Standard Firestore `(default)` created in approved `asia-southeast1` (Singapore). CLI confirmed `databaseEdition: STANDARD` and `freeTier: true`.
- Billing was verified disabled before this work; no billing account, card, Cloud Functions or paid service was enabled.
- The user approved the Spark-only redesign. The earlier P-256 draft was never released or claimed online. Its unowned prototype identities may migrate to v2; owned prototype identities are never silently replaced. Existing released learners retain local IDs, names, avatars and progress.

## One-time ownership without Functions

`GuestIdentityStore` generates a random 256-bit one-time capability, encoded as 64 hex characters and encrypted with AES-GCM/Android Keystore. Full profile ID is SHA-256 of its UTF-8 encoding. Public serial is an 80-bit display prefix, not a password or recovery code.

A verified Firebase UID submits an atomic batch: immutable `claims/{profileId}` plus a transient proof at `accounts/{uid}/claimProofs/{profileId}`. Security Rules hash the proof, require the hash to match the full profile ID, and cross-check both post-write documents with `getAfter()`. Proof documents cannot be read and are deleted after a successful claim. A retained proof after interruption grants no ability to reassign ownership. Claims cannot be updated or deleted, including by their owner. Same-owner retries read the existing receipt; a different owner is denied, even if it knows the old capability. Deleting an account or a progress snapshot never makes its serial claimable again.

Cloud restores write already-owned local identity metadata without generating or downloading a claim capability. Restoring someone else's UID does not make a profile visible. The cloud account UID, not matching email text, defines ownership. Linking Google/Microsoft login methods must retain that UID; collisions with another UID are reported instead of automatically merging accounts.

## Data and sync

- Private snapshots live under `accounts/{uid}/profiles/{profileId}`; account-bound list queries are limited to 200 profiles. All other paths default-deny.
- Each snapshot validates fixed fields, bounded names/avatar/revision, server timestamps and a private progress JSON string capped at 524288 characters (well below a 1 MiB document for the ASCII curriculum payload). The opaque progress field is exempted from indexing.
- Both export and import whitelist known curriculum IDs and bound numeric fields. Passwords, auth tokens, claim capabilities, recordings, paths, TTS IDs and appearance settings are never part of a learning snapshot.
- Stable learned IDs and completed extras merge by union, best scores and historical counters by maximum. Concurrent repetitions are not added together; this avoids double-credit on retries.
- Transactions enforce revision increments and preserve local learning/metadata changes made during network I/O. Imports suppress preference listeners on the Main thread to prevent self-triggering sync loops.
- WorkManager debounces uploads and requires network connectivity. Jobs carry the intended UID and stop on account changes. Offline learning remains local; quota failures report pending sync rather than false success. Failed jobs can retry, and opening the app or manual sync can schedule another attempt.
- Local guest profiles remain available offline. Linked profiles are shown only for their authenticated UID; signing out hides them. Firebase's persisted sign-in permits offline access for the same signed-in account.

## Verification and release gates

- Firestore emulator: 7 adversarial groups cover anonymous/unverified access, serial-only forgery, missing proof/claim pairs, concurrent claims, second-owner/replay/deletion attacks, foreign scopes, bounded lists, extra fields, type/length/range/timestamp/revision validation, omission of required fields, and snapshot erasure without unlocking a claim. All passed locally after rules changes.
- Rules were initially deployed after emulator success. The expanded snapshot-size limit and index exemption must be deployed before release.
- JVM tests cover merge retry/idempotence, union/best-score behavior, import filtering, and the full current catalogue fitting in a snapshot.
- Android candidate CI includes real Firebase Android SDKs against isolated Auth/Firestore emulators: email registration/verification, one-time link, local offline progress, cross-account isolation, and simulated second-installation restoration. Never creates test users or data in the live project.
- Latest Auth/Google ID artifacts have Kotlin metadata 2.4, incompatible with the existing compiler. Maven-verified compatible pins use BoM 34.12.0 (Auth 24.0.1 / Firestore 26.2.0) and Google ID 1.1.1; current compiler accepts metadata through 2.1. Metadata checks are not suppressed. Android CI must pass with these pins.
- Google native account chooser and actual Microsoft OAuth still require device/provider verification. Microsoft Client ID/Secret registration is outstanding; no Microsoft secret belongs in source or the APK.
- Security Rules are a prototype, not a claim of exhaustive security. Review and verify before broad distribution. No privileged roles or public user data are supported.
