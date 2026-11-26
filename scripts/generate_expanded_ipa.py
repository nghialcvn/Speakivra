"""Generate a separate offline IPA asset without changing the approved base IPA."""
from pathlib import Path
import json,re,os
import espeakng_loader
from phonemizer import phonemize
from phonemizer.backend.espeak.wrapper import EspeakWrapper
root=Path(__file__).resolve().parents[1]
source=(root/'app/src/main/java/com/nghialcvn/speakivra/ExtraPracticeExpansion.kt').read_text()
quoted=r'"(?:[^"\\]|\\.)*"'
texts={json.loads(m[1]) for m in re.finditer(r'\bReadingSentence\('+quoted+r',\s*('+quoted+')',source)}
texts.update(json.loads(m[1]) for m in re.finditer(r'\bPracticeVocabulary\(('+quoted+')',source))
key=lambda s:re.sub(r'\s+',' ',s.strip().replace('’',"'").replace('‘',"'")).lower()
base=json.loads((root/'app/src/main/assets/ipa.json').read_text())
EspeakWrapper.set_library(espeakng_loader.get_library_path());os.environ['ESPEAK_DATA_PATH']=espeakng_loader.get_data_path()
data={}
for accent,lang in [('US','en-us'),('GB','en-gb')]:
 data[accent]={key(s):base[accent][key(s)] for s in texts if key(s) in base[accent]}
 missing=sorted(s for s in texts if key(s) not in data[accent])
 values=phonemize(missing,language=lang,backend='espeak',strip=True,preserve_punctuation=False,with_stress=True,language_switch='remove-flags')
 assert len(values)==len(missing) and all(x.strip() for x in values)
 data[accent].update({key(s):v for s,v in zip(missing,values)})
(root/'app/src/main/assets/expanded-ipa.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
print('Expanded IPA:',len(texts),'strings in both accents; base unchanged.')
