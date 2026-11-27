# Rebuilding the Mandarin catalogue

The installed app reads `app/src/main/resources/curriculum/chinese.json.gz` once.
The gzip is deterministic (`mtime=0`) and contains UTF-8 JSON, not executable code.
To inspect it, use `python -m gzip -d -c ...` or Python's gzip/json modules.

1. Build debug classes, then compile/run `InspectSpeakivra.java` in package
   `com.nghialcvn.speakivra` against the app classes JAR, Kotlin stdlib and org.json.
   Pass the output path as the Java program's argument. The inspector exports the
   English runtime catalogue, readings and general/hotel translation exercises.
2. Install `requirements.txt` in a build environment (not in the app).
3. Run `python scripts/curriculum/build_mandarin.py english-reference.json`.
4. Review both the bundled gzip diff (decompressed) and `docs/curriculum/chinese-parity.json`.
5. Run JVM tests, lint, APK checks and the Android UI workflow. Inspect APK screenshots.

All translation units must have explicit reviewed Chinese content. Optional draft
cache input exists only for diagnostics; any unreviewed unit aborts generation.
The review JSON files preserve their English keys or ordered source-case mapping;
IDs derive from source IDs and never from the translated text. Starter 1.2.7 content
remains in `ChineseCurriculum.kt` and is merged without deleting learned IDs.

Do not bundle translation model weights, a paid API integration, credentials, or
speech recordings. Do not represent this content as independently native-reviewed
or represent character matching as acoustic/tone assessment.
