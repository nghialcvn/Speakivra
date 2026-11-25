# Offline conversational speech — V0.1.9

The 110 distinct lesson/conversation lines across ten A1 topics have bundled US-English neural speech clips. They use Kokoro-82M v1.0, voice `af_heart`, synthesized locally with `kokoro-onnx` 0.6.1. No model, paid TTS API, synthesis backend or API key is shipped or required at runtime.

`app/src/main/assets/speech/manifest.json` records exact text, intended context, duration, byte size, SHA-256, model/voice hashes and generation provenance. `scripts/speech_profiles.json` contains complete-clause delivery and short sentence-boundary pauses. The text is unchanged; no filler words or emotion labels are spoken. Neural punctuation and the selected voice supply intonation; this is not a runtime emotion-recognition model.

The APK contains 110 Ogg/Vorbis 24kHz mono files, about 1.63MB in total. The 114MB generation model and 28MB voice pack are build-time scratch inputs, never APK content or Git repository content. Generated audio is normalized to -18 LUFS, with a -1.5dB true-peak target. The generation script checks finite samples, valid durations and clipping.

MediaPlayer keeps the displayed 0.75x / 1x / 1.25x options, with pitch fixed at 1. Starting in V0.1.9 their underlying multipliers are 0.5625 / 0.75 / 0.9375: the previous 0.75x becomes the new normal 1x. On first upgrade only, the saved selection is set to this new 1x baseline; later speed choices persist. Learning progress is not reset. Android TTS fallback retains the earlier calibrated speed relation, uses complete clauses with small context pitch/pace differences, and prefers the highest-quality installed offline US-English voice. TTS is configured only after initialization succeeds and `Locale.US` is applied once. No automatic voice-data download is performed.

Playback requests transient audio focus, flushes/release previous audio on replay, stops before recognition and on app backgrounding, and discards stale callbacks. Unsupported/missing clip playback falls back explicitly to device TTS; synthesis failures are shown in the existing status area. Existing speaking controls and visual theme are retained.

## Rebuild audio (local only)

Install `kokoro-onnx==0.6.1` and `soundfile==0.14.0` in a Python environment with FFmpeg. Obtain `kokoro-v1.0.int8.onnx` and `voices-v1.0.bin` from the upstream `model-files-v1.1` release, then run:

```sh
python scripts/generate_speech.py /absolute/path/to/model-directory
```

This script is intentionally not part of GitHub Actions. Model downloads and inference are not repeated during APK builds. Release verification checks every clip hash and Android resource against the generated manifest.

## Attribution

- Kokoro model: https://huggingface.co/hexgrad/Kokoro-82M — Apache-2.0.
- Kokoro ONNX conversion/runtime: https://github.com/thewh1teagle/kokoro-onnx — MIT.
- Generation assets: https://github.com/thewh1teagle/kokoro-onnx/releases/tag/model-files-v1.1.

Only newly generated speech audio is distributed. The model weights and runtime are not redistributed inside Speakivra. These are synthetic voices. Perceived expression and audible quality still require listening on the target device; compilation and file-integrity checks do not prove human-level emotion.

## V1.0.9: passages, spoken translation and IPA

The two extra catalogues contain 15 original bilingual passages and 15 six-question exercise packs (five per CEFR level). They use the same topic gates as the main curriculum. Their stable completion IDs live in `extra.completed_ids` in each learner's existing preferences and never change main lesson rounds or the V1.0.8 reset marker.

Passages support selected-sentence and full-paragraph recognition with the existing ordered word matcher and continuation threshold. Whole-paragraph capture allows 2.4 seconds of silence on the PCM path (sentence default remains 1.4 seconds, total limit remains 60 seconds). Providers using their own microphone retain their own endpoint policy. Replay remains RAM-only, explicit speaker press, cleared on sentence/mode/question changes, leaving, background or replacement.

Translation exercises require complete bidirectional canonical word alignment against curated alternative translations. Numbers, punctuation and contractions use the existing normalizer. Extra/omitted wrong words, including negations, fail. Unknown alternative phrasing is labelled “Chưa khớp đáp án mẫu”, not semantic or acoustic pronunciation scoring. Hints and listening to answers never complete a question.

Reference IPA is a static US/GB asset generated offline with `scripts/generate_ipa.py` (pinned espeakng-loader and phonemizer); the demo's three IPA lines are explicitly reviewed overrides. No synthesis engine is added to the APK. IPA is reading guidance, not a phoneme assessment of recorded speech. Personal names remain in their original spelling outside IPA brackets. The two accent lookups and complete active curriculum coverage are tested.

## V1.1.0: stable IPA typography

The IPA line now explicitly uses the packaged Tinos Italic face, normal weight and no synthetic font styles. Generic Android serif resolution is no longer used; Samsung system font settings cannot select another family for this text. The packaged face covers every character in both IPA dictionaries and Vietnamese personal names. The original design image specifies the visual serif italic style, not an identifiable named font; no claim is made that Tinos is Times New Roman. Font licensing is included in `assets/font-licenses`.

All six existing home study destinations are inside one initially collapsed “Học tập” section. Their original callbacks, content, colours, topic gates and progress storage are unchanged. Profile reset V1.0.8 is not repeated. Version rolls from 1.0.9 to 1.1.0, code 30.
