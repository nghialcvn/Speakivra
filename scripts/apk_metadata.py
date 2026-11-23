"""Verify built APK identity, permissions, icon and signature before publishing."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument("apk", type=Path)
parser.add_argument("output", type=Path)
parser.add_argument("--release", action="store_true")
args = parser.parse_args()
build_tools = Path(os.environ["ANDROID_HOME"]) / "build-tools" / "35.0.0"

def run(name, *arguments):
    return subprocess.check_output([str(build_tools / name), *map(str, arguments)], text=True)

badging = run("aapt", "dump", "badging", args.apk)
identity = re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", badging)
assert identity, "Cannot read APK identity"
package, code, version = identity.groups()
assert package == "com.nghialcvn.speakivra", "applicationId changed"
assert "android.permission.REQUEST_INSTALL_PACKAGES" not in badging, "Unexpected installation permission"
assert "android.permission.RECORD_AUDIO" in badging and "android.permission.INTERNET" in badging
with zipfile.ZipFile(args.apk) as apk:
    for resource in ["res/mipmap-anydpi-v26/ic_launcher.xml", "res/drawable/ic_launcher_foreground.xml",
                     "res/drawable-nodpi-v4/speakivra_icon.webp", "res/mipmap-nodpi-v4/ic_launcher.webp"]:
        assert resource in apk.namelist(), f"Missing branded icon: {resource}"
    root = Path(__file__).resolve().parents[1]
    for path, resource in [("drawable-nodpi/speakivra_icon.webp", "res/drawable-nodpi-v4/speakivra_icon.webp"),
                           ("mipmap-nodpi/ic_launcher.webp", "res/mipmap-nodpi-v4/ic_launcher.webp")]:
        assert apk.read(resource) == (root / "app/src/main/res" / path).read_bytes(), "Icon bytes changed"
manifest = run("aapt", "dump", "xmltree", args.apk, "AndroidManifest.xml")
assert 'android:icon' in manifest and 'android:roundIcon' in manifest, "Launcher icons not registered"
verification = run("apksigner", "verify", "--verbose", "--print-certs", args.apk)
certificates = re.findall(r"Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]+)", verification)
assert len(certificates) == 1, "Expected one APK signer"
certificate = certificates[0].lower()
if args.release:
    assert "application-debuggable" not in badging, "Release is debuggable"
    expected = os.environ.get("SPEAKIVRA_SIGNING_SHA256", "").lower().replace(":", "")
    assert re.fullmatch(r"[0-9a-f]{64}", expected), "Missing pinned signing fingerprint"
    assert certificate == expected, "Release signing key differs from pinned certificate"
metadata = dict(applicationId=package, versionCode=int(code), versionName=version,
                apkName=args.apk.name, apkSha256=hashlib.sha256(args.apk.read_bytes()).hexdigest(),
                signingCertificateSha256=certificate)
args.output.mkdir(parents=True, exist_ok=True)
(args.output / "speakivra-update.json").write_text(json.dumps(metadata, indent=2) + "\n")
print(json.dumps(metadata, indent=2))
