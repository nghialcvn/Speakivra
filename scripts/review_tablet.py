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
tap("android.widget.EditText")
shell("input","text","Tablet")
shell("input","keyevent","66")
find_scroll("Lưu và bắt đầu")
tap("Bắt đầu học",False)
tap("Tiếp tục học",False)
shot("04-home")
find_scroll("Bài học")
shot("05-lessons")
