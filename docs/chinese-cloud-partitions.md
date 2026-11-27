# Chinese progress storage (1.2.8)

Approved by the user after review of PR #15. This changes storage only; no approved
screen, profile identity, attendance behaviour or achievement formula changes.

The original `accounts/{uid}/profiles/{profileId}` document retains schema 1,
English progress, shared attendance, both resume keys and a compatibility copy of
the original 1.2.7 Chinese A1/A2 subset. Full Chinese progress is divided into eight
private `languageProgress/zh_<level>` documents: A1, A2, B1, B2, C1, C2, FO and HK.
Each has schema 1, profileId, language=`zh`, level, progressJson, revision and
server timestamps. Both client and rules cap each JSON string at 131,072 UTF-8
bytes. Empty levels are not created until they contain progress.

One Firestore transaction reads the parent and the eight fixed document paths
before writing. It unions stable completion IDs, keeps best scores/rewards and
earned rounds, and sanitizes all imported fields. The root compatibility copy is
trimmed only in the same atomic commit that persists the full level snapshots.
Any rejected write aborts the entire migration. An old client may still update the
root; later sync unions its legacy progress with the level documents, preserving
new content. The shared Chinese resume key stays in the root so stale resumes in
different levels cannot compete.

Fresh restores fetch the eight exact level paths from the server before importing
the profile. Offline failures leave local progress pending. Study changes made
during a sync are merged back and scheduled for the next sync. Every document
requires the existing verified account and immutable profile claim. There are no
collection-list permissions for languageProgress; writes require the same parent
profile, including a parent created in the atomic transaction. Voice IDs, audio,
device settings and auth credentials remain outside progress payloads.

Tests cover complete English + Chinese payloads, legacy starter migration, old
client compatibility, UTF-8 limits, failed atomic writes, independent Android SDK
clients with isolated preferences, offline retry, concurrent merge and foreign
account denial. Existing UI tests and screenshot requirements remain mandatory.

## Deployment

The new Firestore rules must be deployed to the existing Firebase project before
this client is released. The additive rules preserve the current parent profile
schema and old-client access. Deploying the APK without the matching rules will
cause level reads/writes to be denied and leave progress pending; it does not
permit an unsafe fallback that discards Chinese progress. Rule deployment and APK
publication have not been performed by this development change.
