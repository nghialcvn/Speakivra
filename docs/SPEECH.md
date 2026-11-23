# Offline conversational speech — V0.1.7

The seven existing lesson/conversation prompts have bundled US-English neural speech clips. They use Kokoro-82M v1.0, voice `af_heart`, synthesized locally with `kokoro-onnx` 0.6.1. No model, paid TTS API, synthesis backend or API key is shipped or required at runtime.

`app/src/main/assets/speech/manifest.json` records exact text, intended context, duration, byte size, SHA-256, model/voice hashes and generation provenance. `scripts/speech_profiles.json` contains complete-clause delivery and short sentence-boundary pauses. The text is unchanged; no filler words or emotion labels are spoken. Neural punctuation and the selected voice supply intonation; this is not a runtime emotion-recognition model.

The APK contains seven Ogg/Vorbis 24kHz mono files, about 110KB in total. The 114MB generation model and 28MB voice pack are build-time scratch inputs, never APK content or Git repository content. Generated audio is normalized to -18 LUFS, with a -1.5dB true-peak target. The generation script checks finite samples, valid durations and clipping.

MediaPlayer applies 0.75 / 1 / 1.25 speed with pitch fixed at 1. The 1x setting is the natural delivery of the new voice. Android TTS fallback retains the earlier calibrated speed relation, uses complete clauses with small context pitch/pace differences, and prefers the highest-quality installed offline US-English voice. TTS is configured only after initialization succeeds and `Locale.US` is applied once. No automatic voice-data download is performed.

Playback requests transient audio focus, flushes/release previous audio on replay, stops before recognition and on app backgrounding, and discards stale callbacks. Unsupported/missing clip playback falls back explicitly to device TTS; synthesis failures are shown in the existing status area. Lesson UI and content are unchanged.

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
