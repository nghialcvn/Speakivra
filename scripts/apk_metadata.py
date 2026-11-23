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
assert "android.permission.REQUEST_INSTALL_PACKAGES" in badging, "Native updater installation permission missing"
assert "android.permission.RECORD_AUDIO" in badging and "android.permission.INTERNET" in badging
resources = run("aapt", "dump", "resources", args.apk)
# AAPT2 shortens release ZIP entry paths, while resource IDs and names stay stable.
for name in ["mipmap/ic_launcher", "drawable/ic_launcher_foreground", "drawable/speakivra_icon"]:
    assert ":" + name in resources, f"Missing branded resource: {name}"
launcher_ids = set(re.findall(r"resource\s+(0x[0-9a-fA-F]+)\s+[^\s]*:mipmap/ic_launcher\b", resources))
assert len(launcher_ids) == 1, "Cannot resolve launcher icon resource ID"
with zipfile.ZipFile(args.apk) as apk:
    image_digests = {hashlib.sha256(apk.read(name)).digest() for name in apk.namelist()
                     if name.startswith("res/") and name.endswith(".webp")}
    root = Path(__file__).resolve().parents[1]
    for path in ["drawable-nodpi/speakivra_icon.webp", "mipmap-nodpi/ic_launcher.webp"]:
        expected_image = hashlib.sha256((root / "app/src/main/res" / path).read_bytes()).digest()
        assert expected_image in image_digests, f"Approved icon bytes missing from APK: {path}"
manifest = run("aapt", "dump", "xmltree", args.apk, "AndroidManifest.xml")
for attribute in ["icon", "roundIcon"]:
    reference = re.search(r"android:" + attribute + r"\([^\n]*?\)=@(0x[0-9a-fA-F]+)", manifest)
    assert reference and reference[1] in launcher_ids, f"Manifest {attribute} does not reference branded launcher"
assert "androidx.core.content.FileProvider" in manifest, "Update FileProvider missing"
assert ":xml/update_paths" in resources, "Update-only FileProvider path configuration missing"
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
