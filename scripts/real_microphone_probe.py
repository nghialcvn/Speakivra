"""CI emulator only: drive the actual APK through its Compose UI tests and feed
real bundled English speech through EmulatorController.injectAudio. No transcript
or recognition fixture is supplied to the production app or its recognizer.
"""
import base64
import gzip
import hashlib
import json
import os
import pathlib
import subprocess
import time
import grpc

PACKAGE = 'com.nghialcvn.speakivra'
DEVICE_DIR = '/sdcard/Android/data/' + PACKAGE + '/files/'
OUT = pathlib.Path('ui-review/real-microphone')
OUT.mkdir(parents=True, exist_ok=True)

def adb(*args):
    return subprocess.check_output(['adb', *args], timeout=15)

def varint(value):
    data = bytearray()
    while value > 127:
        data.append((value & 127) | 128)
        value >>= 7
    data.append(value)
    return bytes(data)

def scalar(number, value):
    return varint(number << 3) + varint(value)

def blob(number, value):
    return varint((number << 3) | 2) + varint(len(value)) + value

report = {'passed': False, 'fixture': 'bundled voice_greeting.ogg'}
process = None
try:
    assert os.environ.get('GITHUB_ACTIONS') == 'true', 'CI emulator only'
    assert adb('shell', 'getprop', 'ro.kernel.qemu').strip() == b'1', 'Never change a physical device'
    fixture = pathlib.Path('scripts/fixtures/voice_greeting_pcm16k.b64')
    metadata = json.loads(fixture.with_suffix('.json').read_text())
    pcm = gzip.decompress(base64.b64decode(fixture.read_text()))
    assert hashlib.sha256(pcm).hexdigest() == metadata['sha256'], 'Speech fixture integrity mismatch'
    adb('shell', 'rm', '-f', DEVICE_DIR + 'mic-probe-ready.txt', DEVICE_DIR + 'mic-probe-result.json')
    log = (OUT / 'instrumentation.txt').open('wb')
    process = subprocess.Popen(['adb', 'shell', 'am', 'instrument', '-w', '-e', 'class',
        PACKAGE + '.RealMicrophoneUiProbeTest', PACKAGE + '.test/androidx.test.runner.AndroidJUnitRunner'], stdout=log, stderr=subprocess.STDOUT)
    deadline = time.monotonic() + 45
    ready = False
    while time.monotonic() < deadline and process.poll() is None:
        probe = subprocess.run(['adb', 'shell', 'cat', DEVICE_DIR + 'mic-probe-ready.txt'], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, timeout=10)
        if probe.returncode == 0 and b'READY' in probe.stdout:
            ready = True
            break
        time.sleep(.25)
    assert ready, 'Actual APK UI did not reach the microphone; inspect instrumentation.txt'
    audio = b'\0' * 8000 + pcm + b'\0' * 16000 + pcm + b'\0' * 96000
    fmt = scalar(1, 16000) + scalar(3, 1) + scalar(4, 1)
    def packets():
        start = time.monotonic()
        for offset in range(0, len(audio), 1024):
            yield blob(1, fmt) + scalar(2, time.time_ns() // 1000) + blob(3, audio[offset:offset + 1024])
            delay = start + (offset + 1024) / 32000 - time.monotonic()
            if delay > 0: time.sleep(delay)
    with grpc.insecure_channel('127.0.0.1:8554') as channel:
        call = channel.stream_unary('/android.emulation.control.EmulatorController/injectAudio', request_serializer=lambda x: x, response_deserializer=lambda x: x)
        call(packets(), timeout=30)
    process.wait(timeout=45)
    log.close()
    adb('pull', DEVICE_DIR + 'mic-probe-result.json', str(OUT / 'device-result.json'))
    report.update(json.loads((OUT / 'device-result.json').read_text()))
    output = (OUT / 'instrumentation.txt').read_text()
    report['passed'] = report['passed'] and 'OK (1 test)' in output and 'FAILURES!!!' not in output
except Exception as error:
    report['error'] = str(error)
finally:
    if process is not None and process.poll() is None:
        try: process.wait(timeout=40)
        except subprocess.TimeoutExpired: process.terminate()
    try:
        adb('pull', DEVICE_DIR + 'ui-screenshots', str(OUT))
        (OUT / 'logcat.txt').write_bytes(adb('logcat', '-d', '-t', '500'))
    except Exception:
        pass
    (OUT / 'result.json').write_text(json.dumps(report, ensure_ascii=False, indent=2))
    print(json.dumps(report, ensure_ascii=False))
