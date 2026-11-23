"""Build bundled US-English lesson audio locally. Never runs during Android builds.

pip install kokoro-onnx==0.6.1 soundfile==0.14.0
Model files: upstream thewh1teagle/kokoro-onnx, model-files-v1.1 release.
Only generated audio is shipped; the synthesis model is not bundled in the APK.
"""
import argparse
import hashlib
import importlib.metadata
import json
from pathlib import Path
import subprocess
import tempfile
import numpy as np
import soundfile as sf
from kokoro_onnx import Kokoro

parser = argparse.ArgumentParser()
parser.add_argument("model_directory", type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
profiles = json.loads((root / "scripts/speech_profiles.json").read_text())
model_path = args.model_directory / "kokoro-v1.0.int8.onnx"
voices_path = args.model_directory / "voices-v1.0.bin"
model = Kokoro(str(model_path), str(voices_path))
old_manifest = json.loads((root / "app/src/main/assets/speech/manifest.json").read_text())
cached = {clip["resource"]: clip for clip in old_manifest["clips"]}
clips = []
for name, profile in profiles.items():
    path = root / "app/src/main/res/raw" / (name + ".ogg")
    profile_hash = hashlib.sha256(json.dumps(profile, sort_keys=True).encode()).hexdigest()
    previous = cached.get(name)
    if previous and path.exists() and previous.get("profile_sha256") == profile_hash and hashlib.sha256(path.read_bytes()).hexdigest() == previous["sha256"]:
        clips.append(previous)
        continue
    chunks = []
    for part in profile["parts"]:
        samples, rate = model.create(part["text"], voice="af_heart", speed=part["speed"], lang="en-us")
        chunks.append(samples)
        if part.get("pause_ms", 0):
            chunks.append(np.zeros(int(rate * part["pause_ms"] / 1000), dtype=np.float32))
    audio = np.concatenate(chunks)
    assert np.isfinite(audio).all() and len(audio) > rate * 0.5
    assert " ".join(part["text"] for part in profile["parts"]) == profile["text"]
    path = root / "app/src/main/res/raw" / (name + ".ogg")
    with tempfile.TemporaryDirectory() as temporary:
        wav = Path(temporary) / "voice.wav"
        sf.write(wav, audio, rate, subtype="PCM_16")
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(wav),
                        "-af", "loudnorm=I=-18:TP=-1.5:LRA=7", "-ar", "24000", "-ac", "1",
                        "-c:a", "libvorbis", "-q:a", "5", str(path)], check=True)
    decoded, decoded_rate = sf.read(path)
    assert decoded_rate == 24000 and np.isfinite(decoded).all() and np.max(np.abs(decoded)) < 1
    duration = len(decoded) / decoded_rate
    assert 0.5 < duration < 15
    clips.append(dict(resource=name, text=profile["text"], context=profile["context"], profile_sha256=profile_hash,
                      duration_seconds=round(duration, 3), size=path.stat().st_size,
                      sha256=hashlib.sha256(path.read_bytes()).hexdigest()))
    print(name, round(duration, 2), path.stat().st_size, flush=True)
manifest = dict(generator="kokoro-onnx " + importlib.metadata.version("kokoro-onnx"),
                model="Kokoro-82M v1.0 INT8", voice="af_heart", language="en-us",
                model_source="https://github.com/thewh1teagle/kokoro-onnx/releases/tag/model-files-v1.1",
                model_license="Apache-2.0", runtime_license="MIT",
                model_sha256=hashlib.sha256(model_path.read_bytes()).hexdigest(),
                voices_sha256=hashlib.sha256(voices_path.read_bytes()).hexdigest(), clips=clips)
(root / "app/src/main/assets/speech/manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
