"""Append offline IPA from the actual Kotlin-exported advanced curriculum.
Input is the Base64 TSV exported by AdvancedCurriculumTest or the pure Kotlin exporter.
Build-time only: espeakng-loader==0.2.4, phonemizer==3.6.0. Existing keys are preserved.
"""
import argparse,base64,gzip,json,os,re
from pathlib import Path
import espeakng_loader
from phonemizer import phonemize
from phonemizer.backend.espeak.wrapper import EspeakWrapper
parser=argparse.ArgumentParser();parser.add_argument('export');args=parser.parse_args()
root=Path(__file__).resolve().parents[1]
texts={base64.b64decode(line.split('\t')[3]).decode() for line in Path(args.export).read_text().splitlines()}
key=lambda s:re.sub(r'\s+',' ',s.strip().replace('’',"'").replace('‘',"'")).lower()
asset=root/'app/src/main/assets/advanced-ipa.bin'
data=json.loads(gzip.decompress(asset.read_bytes())) if asset.exists() else {'US':{},'GB':{}}
EspeakWrapper.set_library(espeakng_loader.get_library_path())
os.environ['ESPEAK_DATA_PATH']=espeakng_loader.get_data_path()
for accent,language in [('US','en-us'),('GB','en-gb')]:
 missing=sorted(s for s in texts if key(s) not in data[accent])
 values=phonemize(missing,language=language,backend='espeak',strip=True,preserve_punctuation=False,with_stress=True,language_switch='remove-flags')
 assert len(missing)==len(values) and all(v.strip() for v in values)
 data[accent].update({key(s):v for s,v in zip(missing,values)})
 print(accent,len(missing),'new entries')
asset.write_bytes(gzip.compress(json.dumps(data,ensure_ascii=False,separators=(',',':')).encode(),compresslevel=9,mtime=0))
