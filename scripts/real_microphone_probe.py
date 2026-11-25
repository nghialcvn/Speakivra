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
import threading
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
    # Register the emulator's virtual microphone BEFORE AudioRecord opens it.
    # Opening injectAudio only after the app reports listening can leave the
    # already-open guest input bound to its previous (silent host) source.
    start_clip = threading.Event()
    first_packet = threading.Event()
    audio = b'\0' * 8000 + pcm + b'\0' * 16000 + pcm + b'\0' * 96000
    fmt = scalar(1, 16000) + scalar(3, 1) + scalar(4, 1)  # mono=0; PCM S16; real time
    sent = {'packets': 0, 'speech_bytes': 0}
    def packets():
        # Keep a live virtual input with silence until the actual APK's micro
        # is ready. No transcript or app result is ever supplied by this host.
        while not start_clip.is_set():
            yield blob(1, fmt) + scalar(2, time.time_ns() // 1000) + blob(3, b'\0' * 1024)
            first_packet.set()
            time.sleep(.032)
        for offset in range(0, len(audio), 1024):
            chunk = audio[offset:offset + 1024]
            yield blob(1, fmt) + scalar(2, time.time_ns() // 1000) + blob(3, chunk)
            sent['packets'] += 1
            sent['speech_bytes'] += len(chunk)
            time.sleep(len(chunk) / 32000)
    adb('logcat', '-c')  # Disposable CI emulator, preserve the complete probe below.
    with grpc.insecure_channel('127.0.0.1:8554') as channel:
        get_mic = channel.unary_unary('/android.emulation.control.EmulatorController/getMicrophoneState',
            request_serializer=lambda x: x, response_deserializer=lambda x: x)
        report['microphone_state_wire_hex'] = get_mic(b'', timeout=10).hex()
        call = channel.stream_unary('/android.emulation.control.EmulatorController/injectAudio',
            request_serializer=lambda x: x, response_deserializer=lambda x: x)
        injection = call.future(packets(), timeout=90)
        assert first_packet.wait(10), 'Virtual microphone did not accept its first packet'
        time.sleep(.25)  # Allow registration before the test opens Android AudioRecord.
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
            if injection.done():
                injection.result()  # Surface the real gRPC error instead of hiding it behind a UI timeout.
                raise AssertionError('Virtual microphone closed before the APK was ready')
            time.sleep(.25)
        assert ready, 'Actual APK UI did not reach the microphone; inspect instrumentation.txt'
        report['virtual_microphone_registered_before_app'] = True
        start_clip.set()
        injection.result(timeout=35)
        report.update(sent)
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
        (OUT / 'logcat.txt').write_bytes(adb('logcat', '-d', '-v', 'threadtime'))
    except Exception:
        pass
    (OUT / 'result.json').write_text(json.dumps(report, ensure_ascii=False, indent=2))
    print(json.dumps(report, ensure_ascii=False))
