#!/usr/bin/env bash
# Run all existing UI checks, with each new check isolated in its own
# instrumentation process. Keep screenshot layout in dp identical to the
# original 1080x2340 / 420dpi review, but reduce software-rendered pixels.
set -Eeuo pipefail
mkdir -p ui-review ui-diagnostics
: > ui-test-output.txt
: > ui-diagnostics/counts.txt
app=com.nghialcvn.speakivra
runner="$app.test/androidx.test.runner.AndroidJUnitRunner"
remote="/sdcard/Android/data/$app/files/ui-screenshots"
logcat_pid=""
collect() {
    local status=$?
    trap - EXIT
    set +e
    if [[ -n "$logcat_pid" ]]; then kill "$logcat_pid" 2>/dev/null; wait "$logcat_pid" 2>/dev/null; fi
    adb devices -l > ui-diagnostics/adb-devices-final.txt 2>&1
    free -m > ui-diagnostics/host-memory-final.txt
    sudo dmesg -T > ui-diagnostics/host-kernel.txt 2>&1
    if [[ "$(adb get-state 2>/dev/null)" == device ]]; then
        adb logcat -d -v threadtime > ui-diagnostics/logcat-final.txt 2>&1
        adb shell dumpsys meminfo > ui-diagnostics/device-memory-final.txt 2>&1
        adb pull "$remote" ui-review/ >> ui-diagnostics/screenshots.txt 2>&1
    fi
    exit "$status"
}
trap collect EXIT
free -m > ui-diagnostics/host-memory-start.txt
adb wait-for-device
adb shell wm size 864x1872
adb shell wm density 336
adb shell wm size | tee ui-diagnostics/display.txt
adb shell wm density | tee -a ui-diagnostics/display.txt
adb install -r dist/*.apk
adb shell pm grant "$app" android.permission.POST_NOTIFICATIONS
adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell settings get secure voice_recognition_service | tee ui-recognition-provider.txt
adb logcat -v threadtime > ui-diagnostics/logcat-live.txt 2>&1 &
logcat_pid=$!

run_check() {
    local spec="$1" expected_min="$2"
    local label
    label="$(printf '%s' "$spec" | tr '#' '-')"
    echo "=== $spec ===" | tee -a ui-test-output.txt
    adb shell am force-stop "$app"
    # Raw output reports the method name and status before every individual test.
    timeout 8m adb shell am instrument -w -r -e class "$app.$spec" "$runner" \
        | tee "ui-diagnostics/$label.txt" | tee -a ui-test-output.txt
    local count
    count="$(python3 - "ui-diagnostics/$label.txt" "$expected_min" <<'PY'
import re, sys
from pathlib import Path
s = Path(sys.argv[1]).read_text()
m = re.search(r"OK \((\d+) tests?\)", s)
if not m or int(m.group(1)) < int(sys.argv[2]) or "FAILURES!!!" in s or "INSTRUMENTATION_FAILED" in s or "Process crashed" in s:
    raise SystemExit("Instrumentation did not finish successfully:\n" + s[-8000:])
print(m.group(1))
PY
)"
    printf '%s %s\n' "$spec" "$count" >> ui-diagnostics/counts.txt
    # Preserve the screenshots before later groups can fail.
    if adb shell test -d "$remote"; then
        adb pull "$remote" ui-review/ >> ui-diagnostics/screenshots.txt 2>&1
    fi
    free -m >> ui-diagnostics/host-memory-by-test.txt
}

run_check 'CheckInRefinementUiTest#pickerUsesTwoToOneLockupAndShineOnlyChangesLetterPixels' 1
run_check 'CheckInRefinementUiTest#grayStripPersistsCheckInAndAvatarHandsOverlapItsTop' 1
run_check 'CheckInRefinementUiTest#allSixSelectedAvatarsKeepTheirHandsOnTheDarkStrip' 1
run_check 'CheckInRefinementUiTest#compactRowStaysAlignedAtNarrowWidthsAndLargeFont' 1
run_check ChineseCourseFlowTest 6
run_check LanguageAreaFlowTest 3
run_check BrandRefreshUiTest 1
run_check ProperNameFeedbackTest 1
run_check DailyCheckInFlowTest 1
run_check AdvancedCurriculumFlowTest 1
run_check GuestLinkReminderUiTest 1
run_check AccountDraftFlowTest 1
run_check FreshInstallLearnerTest 1
run_check 'ApprovedUiTest#approvedDiamondRewardsAndResume' 1
run_check SparkAccountIntegrationTest 1
python3 - <<'PY'
from pathlib import Path
rows = [line.split() for line in Path("ui-diagnostics/counts.txt").read_text().splitlines()]
assert len(rows) == 15, rows
assert all(int(count) == 1 for _, count in rows[:4]), rows
assert rows[4][0] == "ChineseCourseFlowTest" and int(rows[4][1]) >= 6, rows
assert rows[5][0] == "LanguageAreaFlowTest" and int(rows[5][1]) >= 3, rows
assert sum(int(count) for _, count in rows[6:]) >= 27, rows
files = list(Path("ui-review").rglob("*.png"))
missing = [n for n in list(range(140, 153)) + list(range(160, 167)) + list(range(170, 177)) if not any(f.name.startswith(f"{n}-") for f in files)]
assert not missing, f"Missing required new screenshots: {missing}"
print(f"Verified {sum(int(count) for _, count in rows)} Android tests and all twenty-seven review screenshots")
PY
