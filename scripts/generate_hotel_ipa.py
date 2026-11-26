"""Append hotel IPA only, preserving every existing curriculum entry.
Requires espeakng-loader==0.2.4 and phonemizer==3.6.0 (build-time only).
"""
from pathlib import Path
import json, os, re
import espeakng_loader
from phonemizer.backend.espeak.wrapper import EspeakWrapper
from phonemizer import phonemize
root = Path(__file__).resolve().parents[1]
source = '\n'.join((root / 'app/src/main/java/com/nghialcvn/speakivra' / name).read_text() for name in ('HotelCurriculum.kt', 'HotelExpansion.kt'))
quoted = r'"(?:[^"\\]|\\.)*"'
texts = sorted({json.loads(m[1]) for m in re.finditer(r'\b(?:Phrase|ConversationTurn)\(\s*(' + quoted + ')', source)})
def key(text):
    return re.sub(r'\s+', ' ', text.strip().replace('’', "'").replace('‘', "'")).lower()
EspeakWrapper.set_library(espeakng_loader.get_library_path())
os.environ['ESPEAK_DATA_PATH'] = espeakng_loader.get_data_path()
asset = root / 'app/src/main/assets/ipa.json'
base = json.loads(asset.read_text())
prior = json.loads((root / 'app/src/main/assets/hotel-ipa.json').read_text())
data = {lang: dict(prior[lang]) for lang in ('US', 'GB')}
for lang in data:
    data[lang].update({key(text): value for text in texts if (value := base[lang].get(key(text)))})
asset = root / 'app/src/main/assets/hotel-ipa.json'
for accent, language in [('US', 'en-us'), ('GB', 'en-gb')]:
    missing = [text for text in texts if key(text) not in data[accent]]
    if not missing: continue
    values = phonemize(missing, language=language, backend='espeak', strip=True,
        preserve_punctuation=False, with_stress=True, language_switch='remove-flags')
    assert len(values) == len(missing) and all(value.strip() for value in values)
    data[accent].update({key(text): value for text, value in zip(missing, values)})
asset.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
print(f'Hotel IPA coverage: {len(texts)} English strings; prior entries preserved.')
