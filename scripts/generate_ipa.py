"""Generate reference IPA assets offline; no speech engine or dependency is bundled.
Run: pip install espeakng-loader==0.2.4 phonemizer==3.6.0
     python scripts/generate_ipa.py
Review overrides and sample every curriculum level after changing English content.
"""
from pathlib import Path
import json, re, os
import espeakng_loader
from phonemizer.backend.espeak.wrapper import EspeakWrapper
from phonemizer import phonemize
ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/nghialcvn/speakivra'
EspeakWrapper.set_library(espeakng_loader.get_library_path())
os.environ['ESPEAK_DATA_PATH'] = espeakng_loader.get_data_path()
texts = {"I'm", "Hello there."}
quoted = r'"(?:[^"\\]|\\.)*"'
for file in SOURCE.glob('*.kt'):
    source = file.read_text()
    for name in ('Phrase', 'ConversationTurn', 'PracticeVocabulary'):
        for found in re.finditer(r'\b' + name + r'\(\s*(' + quoted + ')', source):
            texts.add(json.loads(found[1]))
    for found in re.finditer(r'ReadingSentence\(\s*' + quoted + r',\s*(' + quoted + ')', source):
        texts.add(json.loads(found[1]))
    for found in re.finditer(r'ExerciseQuestion\([^\n]*listOf\((.*?)\)\)', source):
        texts.update(json.loads(x) for x in re.findall(quoted, found[1]))
# Texts are authored literal English, never Kotlin interpolation or translated names.
texts = sorted(x for x in texts if '$' not in x and x.strip())
def key(text):
    return re.sub(r'\s+', ' ', text.strip().replace('’', "'").replace('‘', "'")).lower()
overrides = {"I'm": 'aɪm', "I'm fine, thank you.": 'aɪm faɪn θæŋk juː',
             'I make breakfast.': 'aɪ meɪk ˈbrekfəst',
             'Could I have the bill, please?': 'kʊd aɪ hæv ðə bɪl pliːz'}
result = {}
for accent, language in [('US', 'en-us'), ('GB', 'en-gb')]:
    ipa = phonemize(texts, language=language, backend='espeak', strip=True,
                    preserve_punctuation=False, with_stress=True, language_switch='remove-flags')
    assert len(ipa) == len(texts) and all(x.strip() for x in ipa)
    result[accent] = {key(t): p for t, p in zip(texts, ipa)}
    result[accent].update({key(t): p for t, p in overrides.items()})
asset = ROOT / 'app/src/main/assets/ipa.json'
asset.parent.mkdir(parents=True, exist_ok=True)
asset.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
print(f'{len(texts)} English entries, US + GB, {asset.stat().st_size} bytes')
