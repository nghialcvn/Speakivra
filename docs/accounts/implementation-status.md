# Account and guest-profile integration — draft

This branch is not a cloud-account release. Do not publish a version claiming live sign-in or sync.

Implemented independently of a Firebase project:

- Existing learners keep their local IDs, names, avatars and all learning preferences.
- A public serial and a P-256 signing identity are stored separately. The private key stays in Android Keystore.
- Changing a name does not create a new identity. Restoring metadata without its original key cannot silently reissue the identity.
- Claim proofs bind the complete profile ID to the exact intended account UID. Serial possession alone is not ownership proof.
- Receipt validation refuses a second owner. This local check is not a substitute for an immutable server-side ownership record.
- Portable learning export uses a whitelist. No credentials, voice recordings, device paths or device voice IDs are exported.
- Approved sign-in, register, verification, link confirmation and account screens are prepared. The default gateway explicitly reports unavailable; it never creates fake accounts, verification messages or successful sync states.

Configuration still required before implementing/deploying the Firebase gateway and ownership transaction:

1. Select a dedicated Speakivra Firebase project or provide an existing Project ID.
2. Authorise administration of that project. The CLI in this environment is not signed in.
3. Register the Android package and stable signing certificate; obtain public Android SDK configuration with the CLI.
4. Confirm the database edition/location and deployment plan; do not create a paid resource or enable billing automatically.
5. Enable Google and email/password, and register Microsoft OAuth with the agreed supported account types and redirect URI. Keep any Microsoft client secret on the provider/backend side, never in source or the APK.
6. Implement the server claim transaction, idempotent learning merge, account-partitioned local access, account linking and background sync. Test concurrent claims, retries, offline use, account switching, restore, deletion and rule denial before release.

Required claim invariant: one immutable owner UID per full profile ID, across reinstalls and copied local data. Authenticate the UID, verify the P-256 signature and profile/public-key hash, and atomically create/verify the ownership record. A repeat claim from the same UID is idempotent; a different UID is rejected. No client may update or delete the ownership record to make an old serial claimable again.

Provider linking (Google/Microsoft/password) attaches another login method to the same Speakivra account UID. It never transfers guest-profile ownership to a different UID. Do not automatically merge two different UIDs merely because their email addresses match.
