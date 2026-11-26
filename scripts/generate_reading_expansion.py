from pathlib import Path
import json
root=Path(__file__).resolve().parents[1];q=lambda x:json.dumps(x,ensure_ascii=False)
groups={}
for line in (root/'scripts/content/reading_expansion.tsv').read_text().splitlines():
 if not line or line.startswith('#'):continue
 if line.startswith('@'):h=line[1:].split('|');ident=h[0];groups[ident]=(h[1:],[])
 else:groups[ident][1].append(line.split('|'))
assert len(groups)==20
vocab={
'neighbours':[('Neighbour','Hàng xóm'),('Hold the door','Giữ cửa'),('Vegetables','Rau'),('Introduce yourself','Tự giới thiệu')],
'class':[('Evening class','Lớp buổi tối'),('Starting date','Ngày bắt đầu'),('Book a place','Đăng ký chỗ'),('Attend a course','Theo học một khóa')],
'market':[('Compare','So sánh'),('Guarantee','Bảo hành'),('Receipt','Hóa đơn'),('Reasonable','Hợp lý')],
'repair':[('Fitting','Bộ phận lắp đặt'),('Repair request','Yêu cầu sửa chữa'),('Affected','Bị ảnh hưởng'),('Replace','Thay thế')],
'bus':[('Service notice','Thông báo dịch vụ'),('Route','Tuyến đường'),('Delay','Sự chậm trễ'),('Passenger','Hành khách')],
'housing':[('Flatmate','Bạn cùng nhà'),('Flexible','Linh hoạt'),('Shared space','Không gian chung'),('Maintain','Duy trì')],
'budget':[('Essential costs','Chi phí thiết yếu'),('Savings target','Mục tiêu tiết kiệm'),('Estimate','Dự kiến'),('Unexpected expenses','Chi phí bất ngờ')],
'digital':[('Suspicious','Đáng ngờ'),('Verify','Xác minh'),('Independent','Độc lập'),('Personal information','Thông tin cá nhân')],
'community':[('Volunteer','Tình nguyện viên'),('Availability','Thời gian sẵn có'),('Handover','Bàn giao'),('Contribute','Đóng góp')],
'services':[('Reference number','Mã tham chiếu'),('Investigation','Việc kiểm tra'),('Deadline','Thời hạn'),('Confirm receipt','Xác nhận đã nhận')],
}
adds=[];new=[]
for ident,(h,rows) in groups.items():
 isnew=bool(h);start=1 if isnew else (5 if '_a2_' in ident else 6)
 assert len(rows)==(10 if isnew else 11-start),(ident,len(rows),start)
 lines=[f'ReadingSentence({q(ident+"_s"+str(start+i))}, {q(en)}, {q(vi)})' for i,(en,vi) in enumerate(rows)]
 if not isnew:adds.append(q(ident)+' to listOf(\n'+',\n'.join(lines)+'\n)')
 else:
  lesson,title,vi,topic=h;level='A2' if '_a2_' in ident else 'B1';vs=vocab[ident.split('_')[-1]]
  new.append(f'ReadingPassage({q(ident)}, {q(level)}, {q(lesson)}, {q(title)}, {q(vi)}, {q(topic)}, listOf(\n'+',\n'.join(lines)+'\n), listOf('+', '.join(f'PracticeVocabulary({q(a)}, {q(b)})' for a,b in vs)+'))')
code='''package com.nghialcvn.speakivra

/** Append-only reading content and translation practice with stable IDs. */
internal object ExtraPracticeExpansion {
    private val additions = mapOf(
'''+',\n'.join(adds)+'''
    )
    private val newPassages = listOf(
'''+',\n'.join(new)+'''
    )
    fun passages(original: List<ReadingPassage>) = original.map {
        it.copy(sentences = it.sentences + additions[it.id].orEmpty())
    } + newPassages
    // Existing questions and alternatives remain untouched. New drills use reviewed bilingual
    // curriculum lines, avoiding duplicate prompts already used in this exercise level.
    fun exercises(original: List<SpeakingExercise>): List<SpeakingExercise> = original +
        listOf("A2", "B1").flatMap { level ->
            val lessons = if (level == "A2") Lessons.a2 else Lessons.b1
            val used = original.filter { it.level == level }.flatMap { it.questions.flatMap { q -> q.answers } }.toMutableSet()
            (if (level == "B1") lessons.drop(5) else lessons).take(15).mapIndexed { index, lesson ->
                val count = if (index < 10) 5 else 4
                val phrases = lesson.phrases.filter { it.english !in used }.distinctBy { it.english }.take(count)
                check(phrases.size == count)
                used.addAll(phrases.map { it.english })
                SpeakingExercise("exercise_extended_${lesson.id}", level, lesson.id, "${lesson.vietnamese} • Luyện thêm",
                    phrases.map { ExerciseQuestion("exercise_extended_${it.id}", it.vietnamese, listOf(it.english)) })
            }
        }
}
'''
(root/'app/src/main/java/com/nghialcvn/speakivra/ExtraPracticeExpansion.kt').write_text(code)
