"""Disposable CI emulator ONLY: speak a bundled lesson through its virtual microphone.
Uses the public EmulatorController injectAudio protocol from the SDK's
emulator/lib/emulator_controller.proto. No fixture RecognitionService or transcript.
"""
import json
import base64
import gzip
import hashlib
import os
import pathlib
import re
import subprocess
import threading
import time
import xml.etree.ElementTree as ET
import grpc

PACKAGE = 'com.nghialcvn.speakivra'
OUT = pathlib.Path('ui-review/real-microphone')
OUT.mkdir(parents=True, exist_ok=True)

def adb(*args):
    return subprocess.check_output(['adb', *args], timeout=15)

def nodes():
    adb('shell', 'uiautomator', 'dump', '/sdcard/speakivra-ui.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/speakivra-ui.xml'))

def tap(predicate, timeout=20):
    end = time.monotonic() + timeout
    while time.monotonic() < end:
        root = nodes()
        for node in root.iter('node'):
            if predicate(node.attrib) and node.get('enabled') == 'true':
                values = list(map(int, re.findall(r'\d+', node.get('bounds'))))
                x, y = str((values[0] + values[2]) // 2), str((values[1] + values[3]) // 2)
                adb('shell', 'input', 'swipe', x, y, x, y, '120')
                return
        time.sleep(.2)
    (OUT / 'failed-ui.xml').write_bytes(ET.tostring(root))
    raise AssertionError('Requested production UI control was not available')

def text(value):
    report['stage'] = value
    tap(lambda n: n.get('text') == value)

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

report = {'passed': False, 'fixture': 'bundled voice_greeting.ogg', 'expected': 'Hello! How are you?'}
try:
    assert os.environ.get('GITHUB_ACTIONS') == 'true', 'CI-only probe'
    assert adb('shell', 'getprop', 'ro.kernel.qemu').strip() == b'1', 'Never reset a physical device'
    # This resets only the throwaway CI emulator after its other tests. Never
    # invoked on a user's phone or with their profile/progress data.
    adb('shell', 'pm', 'clear', PACKAGE)
    adb('shell', 'pm', 'grant', PACKAGE, 'android.permission.RECORD_AUDIO')
    adb('shell', 'am', 'start', '-n', PACKAGE + '/.MainActivity')
    text('+ Tạo hồ sơ mới')
    # The first injected touch after launching a fresh activity can only focus
    # its window. Read back the screen and retry this idempotent Open action.
    deadline = time.monotonic() + 10
    while time.monotonic() < deadline:
        root = nodes()
        if any(n.get('class') == 'android.widget.EditText' for n in root.iter('node')): break
        text('+ Tạo hồ sơ mới')
    report['stage'] = 'Enter learner name'
    tap(lambda n: n.get('class') == 'android.widget.EditText')
    adb('shell', 'input', 'text', 'Probe')
    adb('shell', 'input', 'keyevent', '66')
    text('Lưu và bắt đầu')
    text('Cài đặt')
    text('Tùy chỉnh')
    text('Neon')
    text('Trang chủ')
    text('Tiếp tục học')
    root = nodes()
    assert any(n.get('text') == 'Hello! How are you?' for n in root.iter('node')), 'Must test the real first sentence'
    adb('shell', 'screencap', '-p', '/sdcard/probe-before.png')
    adb('pull', '/sdcard/probe-before.png', str(OUT / 'before.png'))
    fixture = pathlib.Path('scripts/fixtures/voice_greeting_pcm16k.b64')
    metadata = json.loads(fixture.with_suffix('.json').read_text())
    pcm = gzip.decompress(base64.b64decode(fixture.read_text()))
    assert hashlib.sha256(pcm).hexdigest() == metadata['sha256'], 'Speech fixture integrity mismatch'
    # A little silence lets recognition bind; repeat the real clip so the
    # unsupported-source fallback still receives speech after its capability probe.
    stream = b'\0' * 8000 + pcm + b'\0' * 16000 + pcm + b'\0' * 96000
    fmt = scalar(1, 16000) + scalar(3, 1) + scalar(4, 1)
    injection_error = []
    def inject():
        try:
            channel = grpc.insecure_channel('127.0.0.1:8554')
            method = channel.stream_unary('/android.emulation.control.EmulatorController/injectAudio', request_serializer=lambda x: x, response_deserializer=lambda x: x)
            def packets():
                start = time.monotonic()
                for offset in range(0, len(stream), 1024):
                    yield blob(1, fmt) + scalar(2, time.time_ns() // 1000) + blob(3, stream[offset:offset + 1024])
                    delay = start + (offset + 1024) / 32000 - time.monotonic()
                    if delay > 0: time.sleep(delay)
            method(packets(), timeout=30)
            channel.close()
        except Exception as error:
            injection_error.append(str(error))
    tap(lambda n: n.get('content-desc') == 'Nói và nhận dạng')
    worker = threading.Thread(target=inject)
    worker.start()
    deadline = time.monotonic() + 25
    passed = False
    while time.monotonic() < deadline:
        root = nodes()
        texts = [n.get('text', '') for n in root.iter('node')]
        # The reference alone is insufficient. Require a separate recognized
        # reply AND the actual enabled Next gate, derived by the app itself.
        next_nodes = [n for n in root.iter('node') if n.get('text') == 'Câu tiếp theo' and n.get('enabled') == 'true']
        replies = [n for n in root.iter('node') if n.get('text') and 'hello' in n.get('text').lower()]
        if next_nodes and len(replies) >= 2:
            passed = True
            break
        if injection_error: break
        time.sleep(.2)
    worker.join(35)
    report.update(passed=passed and not injection_error, visible_text=texts, injection_error=injection_error)
    (OUT / 'result-ui.xml').write_bytes(ET.tostring(root))
    adb('shell', 'screencap', '-p', '/sdcard/probe-result.png')
    adb('pull', '/sdcard/probe-result.png', str(OUT / 'result.png'))
    assert report['passed'], 'Real microphone recognition did not return words and enable Next'
except Exception as error:
    report['error'] = str(error)
    try:
        (OUT / 'logcat.txt').write_bytes(adb('logcat', '-d', '-t', '500'))
    except Exception:
        pass
finally:
    (OUT / 'result.json').write_text(json.dumps(report, ensure_ascii=False, indent=2))
    print(json.dumps(report, ensure_ascii=False))
