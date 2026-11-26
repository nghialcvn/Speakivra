"""Compile reviewed bilingual hotel service drills; append IDs preserve existing progress."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
q=lambda x:json.dumps(x,ensure_ascii=False)
groups={}
for line in (root/'scripts/content/hotel_expansion.tsv').read_text().splitlines():
    if not line or line.startswith('#'):continue
    if line.startswith('@'):
        header=line[1:].split('|'); ident=header[0]; groups[ident]=(header[1:],[])
    else:groups[ident][1].append(line.split('|'))
assert len(groups)==40
new={
'hotel_fo_airport':('Airport transfers','Đưa đón sân bay','map',False),
'hotel_fo_wakeup':('Wake-up calls','Gọi báo thức','clock',False),
'hotel_fo_dining':('Restaurant bookings','Đặt bàn & yêu cầu ăn uống','food',False),
'hotel_fo_groups':('Group arrivals','Đón đoàn & phối hợp','family',False),
'hotel_fo_wifi':('Guest Wi-Fi support','Wi-Fi & hỗ trợ kết nối','phone',True),
'hotel_hk_turndown':('Evening turndown service','Chuẩn bị phòng buổi tối','towel',False),
'hotel_hk_eco':('Linen reuse and recycling','Tái sử dụng đồ vải & phân loại rác','sparkle',False),
'hotel_hk_family':('Family room setup','Bố trí phòng gia đình','family',False),
'hotel_hk_pests':('Insects and pest reports','Côn trùng & phối hợp xử lý','tools',True),
'hotel_hk_handover':('Service follow-up and handovers','Theo dõi yêu cầu & bàn giao ca','chat',False),
}
tip='Nói rõ yêu cầu, nhấn từ khóa và giữ giọng lịch sự với khách.'
adds={};newtopics=[]
for ident,(h,rows) in groups.items():
    n,vn,question,vquestion,act,vact=h
    templ=[
        (f'May I confirm the details of {n}?',f'Tôi có thể xác nhận thông tin về {vn} không?'),
        (f'Could you tell me {question}?',f'Anh/chị có thể cho biết {vquestion} không?'),
        (f'Let me {act}.',f'Để tôi {vact}.'),
        (f'I will {act} before confirming the arrangements.',f'Tôi sẽ {vact} trước khi xác nhận việc sắp xếp.'),
        (f'I have noted the details of {n}.',f'Tôi đã ghi thông tin về {vn}.'),
        (f'Is there anything else we should know about {n}?',f'Chúng tôi có cần biết thêm điều gì về {vn} không?'),
        (f'May I share the necessary details of {n} with the responsible team?',f'Tôi có thể chia sẻ thông tin cần thiết về {vn} với bộ phận phụ trách không?'),
        (f'I will check for an update on {n}.',f'Tôi sẽ kiểm tra thông tin mới về {vn}.'),
        (f'Could you confirm that I understood {n} correctly?',f'Anh/chị có thể xác nhận tôi đã hiểu đúng về {vn} không?'),
    ]
    if ident.endswith('_emergency'):
        role='reception' if '_fo_' in ident else 'housekeeping'
        templ=[
            (f'This is {role}. I am reporting an emergency at the guest room.',f'Đây là bộ phận {"lễ tân" if role=="reception" else "buồng phòng"}. Tôi đang báo tình huống khẩn cấp tại phòng khách.'),
            (f'I will give the emergency team your exact location from {role}.',f'Tôi sẽ báo vị trí chính xác của anh/chị cho đội ứng phó từ bộ phận {"lễ tân" if role=="reception" else "buồng phòng"}.'),
            (f'Please tell the {role} team if anyone cannot evacuate without help.',f'Anh/chị vui lòng báo bộ phận {"lễ tân" if role=="reception" else "buồng phòng"} nếu có người không thể sơ tán mà không được hỗ trợ.'),
            ('Do not enter a smoke-filled corridor. Follow the emergency team\'s instructions.','Đừng đi vào hành lang đầy khói. Hãy theo hướng dẫn của đội ứng phó.'),
            ('I will report the danger immediately rather than wait for a routine service visit.','Tôi sẽ báo nguy hiểm ngay thay vì chờ lần phục vụ thường lệ.'),
            ('Please tell the responder about any immediate change in the situation.','Anh/chị vui lòng báo người ứng phó khi tình hình có thay đổi ngay lúc này.'),
            ('The emergency team will decide when it is safe to re-enter this area.','Đội ứng phó sẽ quyết định khi nào có thể vào lại khu vực này an toàn.'),
            ('Please do not block the emergency exit with luggage.','Anh/chị vui lòng không chặn lối thoát hiểm bằng hành lý.'),
            ('I will direct the responders to the location that you reported.','Tôi sẽ hướng dẫn người ứng phó đến vị trí anh/chị đã báo.'),
        ]
    count=16 if ident in new else 8
    assert len(rows)==count,(ident,len(rows))
    pairs=[x[:2] for x in rows]+templ
    start=1 if ident in new else 9
    phrases=[f'Phrase({q(en)}, {q(vi)}, {q(tip)}, id = {q(ident+"_p"+str(start+i))})' for i,(en,vi) in enumerate(pairs)]
    if ident not in new:adds[ident]=phrases
    else:
        title,vi,icon,incident=new[ident];role='FO' if '_fo_' in ident else 'HK'
        assert all(len(x)==4 for x in rows[:6]),ident
        turns=[f'ConversationTurn({q(x[2])}, {q(x[3])}, {q(x[0])}, {q(x[1])}, id = {q(ident+"_t"+str(i+1))})' for i,x in enumerate(rows[:6])]
        newtopics.append('HotelCurriculum.Topic('+q(role)+', '+q(icon)+', '+str(incident).lower()+', Lesson('+q(ident)+', '+q(title)+', '+q(vi)+', listOf(\n'+',\n'.join(phrases)+'\n), listOf(\n'+',\n'.join(turns)+'\n)))')
code='''package com.nghialcvn.speakivra

/** Reviewed contextual service drills. Never reorder or replace a saved content ID. */
internal object HotelExpansion {
    private val additions: Map<String, List<Phrase>> = mapOf(
'''+',\n'.join(q(k)+' to listOf(\n'+',\n'.join(v)+'\n)' for k,v in adds.items())+'''
    )
    private val newTopics = listOf(
'''+',\n'.join(newtopics)+'''
    )
    fun expand(original: List<HotelCurriculum.Topic>): List<HotelCurriculum.Topic> =
        original.map { topic -> topic.copy(lesson = topic.lesson.copy(
            phrases = topic.lesson.phrases + additions.getValue(topic.lesson.id))) } + newTopics
}
'''
(root/'app/src/main/java/com/nghialcvn/speakivra/HotelExpansion.kt').write_text(code)
print('Added',sum(len(v) for v in adds.values())+len(newtopics)*25,'staff lines; 40 topics at 25 lines each.')
