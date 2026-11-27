#!/usr/bin/env python3
"""Compile reviewed translation units and explicit overrides into the bundled course.

Inputs are the runtime English export (InspectSpeakivra.java) and a build-time
translation-unit draft. The APK includes text only, never a model or an API key.
Always inspect the diff and review language quality before publishing.
"""
import argparse, json, re, hashlib, gzip
from functools import lru_cache
from pathlib import Path
from pypinyin import pinyin, Style, load_phrases_dict
from pypinyin.contrib.tone_convert import to_tone3

load_phrases_dict({'客房部': [['kè'], ['fáng'], ['bù']], '行李': [['xíng'], ['li']], '前台': [['qián'], ['tái']],
 '还款': [['huán'], ['kuǎn']], '还给': [['huán'], ['gěi']], '重新': [['chóng'], ['xīn']], '长大': [['zhǎng'], ['dà']]})
ROOT=Path(__file__).resolve().parents[2]

def main():
 ap=argparse.ArgumentParser();ap.add_argument('english',type=Path);ap.add_argument('units',type=Path,nargs='?',help='Optional draft cache for diagnostics; unreviewed units are always rejected');args=ap.parse_args()
 source=json.loads(args.english.read_text()); units=json.loads(args.units.read_text()) if args.units else {}
 overrides=json.loads((Path(__file__).with_name('mandarin_overrides.json')).read_text())
 overrides.update(json.loads((Path(__file__).with_name('supplemental_mandarin_review.json')).read_text()))
 hotel=json.loads((Path(__file__).with_name('hotel_mandarin_review.json')).read_text())
 for lesson in source['lessons']:
  if lesson['level'] not in ['FO','HK']:continue
  h=hotel[lesson['id']]; lines=h['lines'].split('|'); prompts=h['prompts'].split('|')
  assert len(lines)==16 and len(prompts)==6,lesson['id']
  noun=h['subject'];check=h['check']
  if lesson['id'] in ['hotel_fo_emergency','hotel_hk_emergency']:
   department='前台'if lesson['level']=='FO'else'客房部'
   lines += [f"这里是{department}，我正在报告一起客房紧急情况。",f"我会从{department}向应急响应小组报告您的准确位置。",
    f"如果有人无法自行疏散，请告知{department}。","请勿进入充满烟雾的走廊，请遵从应急人员的指示。",
    "我会立即报告危险，而不会等到例行服务时再报告。","如果现场情况有任何变化，请立即告知应急人员。",
    "应急响应小组会决定何时可以安全地重新进入该区域。","请勿用行李堵塞紧急出口。","我会引导应急人员前往您报告的位置。"]
  else:
   lines += [f"我可以确认一下有关{noun}的详细信息吗？", h['question'],f"让我{check}。",f"在确认安排之前，我会{check}。",
    f"我已记录有关{noun}的详细信息。",f"关于{noun}，还有什么需要我们了解的吗？",f"我可以将有关{noun}的必要信息提供给负责部门吗？",
    f"我会查询有关{noun}的最新进展。",f"您能确认我对{noun}的理解是否正确吗？"]
  assert len(lines)==len(lesson['phrases'])==25
  for phrase,text in zip(lesson['phrases'],lines):overrides[phrase['en']]=text
  for turn,text in zip(lesson['turns'],prompts):overrides[turn['prompt']]=text
  assert all(t['reply']in overrides for t in lesson['turns']),lesson['id']
 basic=json.loads((Path(__file__).with_name('basic_mandarin_review.json')).read_text())
 for lesson in source['lessons']:
  if lesson['id']not in basic:continue
  h=basic[lesson['id']];lines=h['lines'].split('|');prompts=h['prompts'].split('|')
  offset=h.get('offset',8)
  assert len(lines)==len(lesson['phrases'])-offset and len(prompts)==len(lesson['turns'])-4,lesson['id']
  overrides.update((p['en'],t)for p,t in zip(lesson['phrases'][offset:],lines))
  overrides.update((p['prompt'],t)for p,t in zip(lesson['turns'][4:],prompts))
 b1=json.loads((Path(__file__).with_name('b1_mandarin_review.json')).read_text())
 for lesson in source['lessons']:
  if lesson['level']!='B1':continue
  h=b1[lesson['id']];u=h['units'].split('|');assert len(u)==25
  base,plan,reason,obstacle,condition=[u[n:n+5]for n in range(0,25,5)]
  responses=(base+plan+[f'{base[i]}，因为{reason[i]}'for i in range(5)]+
   [f'虽然{obstacle[i]}，但{base[i]}'for i in range(5)]+
   [f'如果{condition[i]}，{plan[i]}'for i in range(5)]+
   [f'虽然{obstacle[i]}，但{base[i]}，因为{reason[i]}'for i in range(5)]+
   [f'如果{condition[i]}，{plan[i]}，因为{reason[i]}'for i in range(5)])
  assert len(responses)==len(lesson['phrases'])==35
  for phrase,text in zip(lesson['phrases'],responses):overrides[phrase['en']]=text+'。'
  for n,text in zip([0,3,6,9,12],h['questions'].split('|')):overrides[lesson['turns'][n]['prompt']]=text
 advanced=json.loads((Path(__file__).with_name('advanced_mandarin_review.json')).read_text())
 for block in re.findall(r'"""(.*?)"""',(ROOT/'app/src/main/java/com/nghialcvn/speakivra/AdvancedScenarioData.kt').read_text(),re.S):
  rows=block.strip().splitlines(); topic=rows[0].split('|')[0]
  en=[]
  for row in rows[1:]:en.extend(row.split('|')[::2])
  zh=advanced[topic].split('|');assert len(en)==len(zh)==19
  overrides.update(zip(en,zh))
 def localized(t):return t.replace('English','Chinese').replace('english','Chinese')
 overrides={localized(k):v for k,v in overrides.items()}
 def vi(t):return t.replace('tiếng Anh','tiếng Trung').replace('Tiếng Anh','Tiếng Trung').replace('Giáo viên nói tôi nên chú ý hơn đến âm cuối của từ.', 'Giáo viên nói tôi nên chú ý hơn đến thanh điệu.').replace('Bạn có thể kiểm tra xem tôi đã dùng thì quá khứ đúng chưa?', 'Bạn có thể kiểm tra xem tôi đã diễn đạt sự việc trong quá khứ đúng chưa?')
 unreviewed=set()
 @lru_cache(None)
 def translate(t):
  t=localized(t)
  if t in overrides:return overrides[t]
  chunks=re.split(r'(?<=[.!?])\s+(?=[A-Z])',t)
  def translated_chunk(p):
   if p in overrides:return overrides[p]
   match=re.fullmatch(r'When you say, "(.*?)", are you asking me to suggest a change, or simply flagging a concern\?',p)
   if match:
    issue=overrides[match.group(1)]
    return f'当您说“{issue}”时，您是希望我提出改变，还是仅仅在指出一个担忧？'
   unreviewed.add(p)
   return units.get(p,'')
  text=''.join(translated_chunk(p)for p in chunks)
  if any(not translated_chunk(p) for p in chunks):raise ValueError('Missing translation: '+t)
  text=text.replace('家居保养','客房服务').replace('家务管理','客房管理').replace('亚麻布','布草').replace('亚麻','布草')
  text=text.replace('前线办公室','前台').replace('前台办公室','前台')
  if not re.search(r'[\u3400-\u9fff]',text):raise ValueError('Missing Hanzi: '+t+' -> '+text)
  if re.search(r'[\ue000-\uf8ff\ufffd]',text):raise ValueError('Invalid Unicode: '+t)
  return text.strip()
 @lru_cache(None)
 def py(text):
  values=pinyin(text,style=Style.TONE,heteronym=False,errors=lambda v:[v])
  symbols=re.findall(r'[\u3400-\u9fff]|[^\u3400-\u9fff]+',text)
  if len(symbols)==len(values):
   for i,ch in enumerate(symbols[:-1]):
    next_tone=to_tone3(values[i+1][0])[-1:]
    if ch=='不' and next_tone=='4':values[i][0]='bú'
    if ch=='一' and (i==0 or symbols[i-1]not in '第零一二三四五六七八九十百千万') and symbols[i+1]not in '零一二三四五六七八九十百千万年月日号':
     if next_tone=='4':values[i][0]='yí'
     elif next_tone in '123':values[i][0]='yì'
  text=' '.join(v[0]for v in values)
  text=re.sub(r'\s+([，。！？、；：,.!?;:])',r'\1',text)
  return text[:1].upper()+text[1:]
 def line(id,en,meaning,alternatives=None):
  hanzi=translate(en); result={'id':id,'hanzi':hanzi,'pinyin':py(hanzi),'vi':vi(meaning)}
  if alternatives:
   result['alternatives']=list(dict.fromkeys(translate(a) for a in alternatives if translate(a)!=hanzi))
   result['alternativePinyin']=[py(a)for a in result['alternatives']]
  return result
 def topicid(a):return 'zh_'+('a1_'if a['level']=='A1'else'')+a['id']
 topics=[];counts=[]
 for lesson in source['lessons']:
  id=topicid(lesson)
  topic={'id':id,'sourceId':lesson['id'],'level':lesson['level'],'title':translate(lesson['title']),'vi':vi(lesson['vi']),
    'tip':'Nghe và đọc theo cụm ý; chú ý thanh điệu và từ phủ định. Pinyin là hướng dẫn đọc, kết quả nhận dạng chữ chưa chứng minh thanh điệu đúng.',
    'phrases':[line(id+'_p_'+p['id'],p['en'],p['vi'])for p in lesson['phrases']],
    'turns':[{'prompt':line(id+'_q_'+t['id'],t['prompt'],t['promptVi']), 'answer':line(id+'_a_'+t['id'],t['reply'],t['replyVi'])}for t in lesson['turns']],
    'readings':[],'exercises':[]}
  for passage in (p for p in source['readings']if p['lessonId']==lesson['id']):
   topic['readings'].append({'id':'zh_'+passage['id'],'title':translate(passage['title']),
     'lines':[line('zh_'+p['id'],p['en'],p['vi'])for p in passage['sentences']],
     'vocabulary':[line('zh_'+passage['id']+'_v'+str(n),p['en'],p['vi'])for n,p in enumerate(passage['vocabulary'])]})
  for exercise in (p for p in source['exercises']if p['lessonId']==lesson['id']):
   topic['exercises'].append({'id':'zh_'+exercise['id'],'title':vi(exercise['topic']),
     'lines':[line('zh_'+q['id'],q['answers'][0],q['vi'],q['answers'][1:])for q in exercise['questions']]})
  topics.append(topic)
 for level in ['A1','A2','B1','B2','C1','C2','FO','HK']:
  items=[t for t in topics if t['level']==level]
  counts.append({'level':level,'topics':len(items),'phrases':sum(len(t['phrases'])for t in items),'turns':sum(len(t['turns'])for t in items),
   'readings':sum(len(t['readings'])for t in items),'readingSentences':sum(len(p['lines'])for t in items for p in t['readings']),
   'exercises':sum(len(t['exercises'])for t in items),'exerciseQuestions':sum(len(p['lines'])for t in items for p in t['exercises'])})
 if unreviewed:raise ValueError('Unreviewed translation units: '+repr(sorted(unreviewed)[:20]))
 output={'schema':1,'reviewedUnitsOnly':True,'sourceMain':'4c353a2c69d10a7f48dc3183c9862f704d7e9910','sourceSha256':hashlib.sha256(args.english.read_bytes()).hexdigest(),
   'provenance':'Machine-assisted drafting with explicit Mandarin review units for every sentence. Pinyin is generated with pronunciation overrides; no acoustic or tone scoring. No independent native-speaker review claimed.', 'counts':counts,'topics':topics}
 path=ROOT/'app/src/main/resources/curriculum/chinese.json.gz';path.parent.mkdir(exist_ok=True,parents=True);path.write_bytes(gzip.compress((json.dumps(output,ensure_ascii=False,indent=2)+'\n').encode(),mtime=0))
 (ROOT/'docs/curriculum/chinese-parity.json').write_text(json.dumps({'sourceMain':output['sourceMain'],'core':counts,'retainedStarterExtras':{'readings':25,'readingSentences':100,'exercisePacks':25,'exerciseQuestions':100,'meaningChecks':75}},ensure_ascii=False,indent=2)+'\n')
 (ROOT/'build').mkdir(exist_ok=True)
 (ROOT/'build/mandarin-unreviewed-units.json').write_text(json.dumps({t:units.get(t)for t in sorted(unreviewed)},ensure_ascii=False,indent=2)+'\n')
 print('Units still machine-assisted:',len(unreviewed)); print(json.dumps(counts,ensure_ascii=False,indent=2));print('Written',path,path.stat().st_size,'bytes')
if __name__=='__main__':main()
