import os, re, subprocess, time
import xml.etree.ElementTree as ET
from pathlib import Path

out = Path("tablet-review")
out.mkdir(exist_ok=True)
def adb(*args):
    return subprocess.check_output(["adb", *args])
def shell(*args):
    return adb("shell", *map(str,args))
def shot(name):
    time.sleep(1)
    (out / (name + ".png")).write_bytes(adb("exec-out","screencap","-p"))
def nodes():
    shell("uiautomator","dump","/sdcard/review.xml")
    return list(ET.fromstring(shell("cat","/sdcard/review.xml")).iter("node"))
def tap(text, required=True):
    for n in nodes():
        if text in (n.get("text"),n.get("content-desc"),n.get("class")):
            l,t,r,b=map(int,re.findall(r'\d+',n.get("bounds")))
            shell("input","tap",(l+r)//2,(t+b)//2)
            time.sleep(.8)
            return True
    if required:
        raise RuntimeError("Missing " + text)
    return False
def find_scroll(text):
    if tap(text,False): return
    import struct
    w,h=struct.unpack(">II",adb("exec-out","screencap","-p")[16:24])
    for _ in range(4):
        shell("input","swipe",w//2,h*8//10,w//2,h//4,350)
        if tap(text,False): return
    raise RuntimeError(text)

shell("wm","size",os.environ["TABLET_SIZE"])
shell("wm","density","160")
shell("settings","put","system","accelerometer_rotation","0")
shell("settings","put","system","user_rotation",os.environ["ROT"])
adb("install","-r","Speakivra-v1.2.9-release.apk")
shell("pm","grant","com.nghialcvn.speakivra","android.permission.POST_NOTIFICATIONS")
shell("am","start","-W","-n","com.nghialcvn.speakivra/.MainActivity")
time.sleep(2)
shot("01-language")
find_scroll("Tiếng Anh")
shot("02-profiles")
find_scroll("+ Tạo hồ sơ mới")
shot("03-create")
n = next(n for n in nodes() if n.get("text") == "Lưu và bắt đầu")
l,t,r,b=map(int,re.findall(r'\d+',n.get("bounds")))
save_point=((l+r)//2,(t+b)//2)
shell("settings","put","secure","show_ime_with_hard_keyboard","0")
tap("android.widget.EditText")
shell("input","text","Tablet")
time.sleep(1)
if b"mInputShown=true" in shell("dumpsys","input_method"):
    shell("input","keyevent","4")
time.sleep(1)
shot("03b-typed")
shell("input","tap",*save_point)
time.sleep(2)
shot("04-home")
find_scroll("Bài học")
shot("05-lessons")

(out/"last.xml").write_bytes(shell("cat","/sdcard/review.xml"))
