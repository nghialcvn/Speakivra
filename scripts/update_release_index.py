"""Maintain a small validated update index after a public beta is published.

GitHub's unauthenticated releases API is rate-limited per public IP. Android
clients use this index only as fallback and still verify package, version,
SHA-256 and installed signing certificate before accepting an update.
"""
import base64
import json
import os
from pathlib import Path
import subprocess
import sys


def api(path, method=None, payload=None):
    command = ["gh", "api", "repos/" + os.environ["GITHUB_REPOSITORY"] + "/" + path]
    if method:
        command += ["--method", method]
    if payload is not None:
        command += ["--input", "-"]
    result = subprocess.run(
        command, input=json.dumps(payload) if payload is not None else None,
        text=True, capture_output=True
    )
    if result.returncode:
        raise RuntimeError("GitHub release index API failed: " + result.stderr[:800])
    return json.loads(result.stdout)


def main():
    meta = json.loads(Path("dist/speakivra-update.json").read_text())
    tag = "v" + meta["versionName"] + "-beta"
    assert meta["applicationId"] == "com.nghialcvn.speakivra"
    assert int(meta["versionCode"]) > 0
    assert len(meta["apkSha256"]) == 64 and len(meta["signingCertificateSha256"]) == 64
    release = api("releases/tags/" + tag)
    assert release["tag_name"] == tag and release["draft"] is False
    assert release["target_commitish"] == os.environ["GITHUB_SHA"]
    asset = next(a for a in release["assets"] if a["name"] == meta["apkName"])
    assert int(asset["size"]) == Path("dist", meta["apkName"]).stat().st_size
    assert asset["digest"] == "sha256:" + meta["apkSha256"]
    assert asset["browser_download_url"] == (
        "https://github.com/" + os.environ["GITHUB_REPOSITORY"] +
        "/releases/download/" + tag + "/" + meta["apkName"]
    )
    index = {
        "release": {
            "tag_name": tag,
            "draft": False,
            "body": release.get("body", ""),
            "assets": [{
                "name": asset["name"],
                "browser_download_url": asset["browser_download_url"],
                "size": asset["size"],
                "digest": asset["digest"],
            }],
        },
        "metadata": meta,
    }
    old = api("contents/updates/latest.json")
    prior = json.loads(base64.b64decode(old["content"]).decode())
    old_code = int(prior["metadata"]["versionCode"])
    new_code = int(meta["versionCode"])
    if new_code < old_code:
        raise RuntimeError("Refusing to replace a newer release index")
    if new_code == old_code:
        assert prior == index, "Published index conflicts with the same version code"
        print("Release index already current")
        return
    payload = {
        "message": "Update signed public release index " + tag + " [skip ci]",
        "branch": "main",
        "sha": old["sha"],
        "content": base64.b64encode(
            (json.dumps(index, ensure_ascii=False, indent=2) + "\n").encode()
        ).decode(),
    }
    api("contents/updates/latest.json", "PUT", payload)
    print("Updated verified fallback index to", tag)


if __name__ == "__main__":
    main()
