package com.nghialcvn.speakivra

import com.nghialcvn.speakivra.AdvancedScenarioData.B
import com.nghialcvn.speakivra.AdvancedScenarioData.Case
import com.nghialcvn.speakivra.AdvancedScenarioData.Topic

/** Fixed authored response ladders, not machine-generated feedback or a live AI service.
 * Five concrete cases × seven communicative purposes = 35 responses per topic.
 * C1 develops a sustained answer. C2 changes pragmatic precision, not a word-count multiplier. */
internal object AdvancedCurriculum {
    private val levels = listOf("B2", "C1", "C2")
    private fun join(vararg text: B) = B(text.joinToString(" ") { it.en }, text.joinToString(" ") { it.vi })
    private fun b(en: String, vi: String) = B(en, vi)
    private fun answer(level: String, t: Topic, c: Case, purpose: Int): B = when (level) {
        "B2" -> when (purpose) {
            0 -> join(c.issue, c.action, b("This seems a practical starting point for both sides, subject to agreement.", "Đây có vẻ là điểm khởi đầu thực tế cho cả hai bên, nếu thống nhất."))
            1 -> join(c.reason, c.action, b("That is why I would favour this approach at this stage.", "Đó là lý do ở giai đoạn này tôi thiên về cách làm này."))
            2 -> join(c.issue, c.reason, b("I would check this before promising an immediate solution to everyone involved.", "Tôi sẽ kiểm tra điều này trước khi hứa với mọi người liên quan rằng sẽ giải quyết ngay."))
            3 -> join(c.action, c.reason, b("However, we should confirm the details carefully with everyone directly affected.", "Tuy nhiên, chúng ta nên xác nhận kỹ chi tiết với mọi người bị ảnh hưởng trực tiếp."))
            4 -> join(c.issue, c.action, b("If that proves impractical, we should discuss another workable option together.", "Nếu cách đó không khả thi, chúng ta nên cùng bàn phương án khả thi khác."))
            5 -> join(c.reason, c.issue, b("Could we clarify the main practical constraint before making a decision together?", "Chúng ta có thể làm rõ hạn chế thực tế chính trước khi cùng đưa ra quyết định không?"))
            else -> join(c.action, c.issue, b("Let's review the actual outcome together rather than simply assume it worked.", "Hãy cùng xem lại kết quả thực tế thay vì đơn giản cho rằng cách làm đã thành công."))
        }
        "C1" -> {
            val opening = listOf(
                b("My assessment remains provisional at this stage.", "Ở giai đoạn này, đánh giá của tôi vẫn là tạm thời."),
                b("There are competing considerations to weigh carefully.", "Có những yếu tố trái chiều cần được cân nhắc kỹ."),
                b("The underlying assumptions matter to this decision.", "Các giả định nền tảng có vai trò quan trọng với quyết định này."),
                b("This recommendation has limits worth acknowledging explicitly.", "Đề xuất này có những giới hạn cần được nêu rõ."),
                b("Several interests are at stake in this discussion.", "Nhiều lợi ích đang bị tác động trong cuộc trao đổi này."),
                b("A credible explanation needs scrutiny before we act.", "Giải thích đáng tin cần được xem xét kỹ trước khi hành động."),
                b("The available evidence is incomplete in important respects.", "Bằng chứng hiện có còn thiếu ở những mặt quan trọng."))[purpose]
            val closing = listOf(
                b("Let's review the outcome together.", "Hãy cùng rà soát kết quả."),
                b("Alternative proposals remain welcome.", "Các đề xuất khác vẫn được hoan nghênh."),
                b("Changed assumptions require reconsideration.", "Giả định thay đổi đòi hỏi xem xét lại."),
                b("The uncertainty should remain explicit.", "Sự bất định cần được nêu rõ."),
                b("Those interests deserve fair consideration.", "Những lợi ích ấy cần được cân nhắc công bằng."),
                b("Unresolved objections should be recorded.", "Ý kiến phản đối chưa giải quyết cần được ghi lại."),
                b("Please confirm responsibility for follow-up.", "Vui lòng xác nhận trách nhiệm theo dõi."))[purpose]
            val points = when (purpose) {
                0 -> listOf(c.issue, c.reason, t.context[0], t.context[1], c.action, t.context[2])
                1 -> listOf(t.context[1], c.issue, c.reason, t.context[0], t.context[2], c.action)
                2 -> listOf(c.issue, t.context[0], c.reason, c.action, t.context[1], t.context[2])
                3 -> listOf(c.reason, t.context[1], c.issue, t.context[0], c.action, t.context[2])
                4 -> listOf(t.context[0], c.issue, t.context[1], c.reason, t.context[2], c.action)
                5 -> listOf(c.issue, t.context[1], c.reason, t.context[2], t.context[0], c.action)
                else -> listOf(c.reason, c.issue, t.context[0], t.context[1], c.action, t.context[2])
            }
            join(opening, *points.toTypedArray(), closing)
        }
        else -> when (purpose) {
            0 -> b("When you say, \"${c.issue.en}\", are you asking me to suggest a change, or simply flagging a concern? ${c.reason.en} I would rather clarify the implication than assume a commitment you have not made.",
                "Khi bạn nói: \"${c.issue.vi}\", bạn muốn tôi đề xuất thay đổi hay chỉ lưu ý một lo ngại? ${c.reason.vi} Tôi muốn làm rõ hàm ý thay vì mặc định bạn đã cam kết điều chưa nói.")
            1 -> join(b("I take your point, although I would be cautious about treating it as the whole picture.", "Tôi hiểu ý bạn, dù tôi sẽ thận trọng nếu coi đó là toàn bộ bức tranh."), c.reason, c.action)
            2 -> join(b("May I suggest a different way forward?", "Cho phép tôi đề xuất một hướng xử lý khác nhé?"), c.action,
                b("I am putting that forward for consideration, not presenting it as a decision already made.", "Tôi đưa ra để cân nhắc, chưa coi đó là quyết định đã được chốt."))
            3 -> join(t.idiom, c.issue, c.action)
            4 -> join(b("With respect, that conclusion goes a little further than the information supports.", "Xin phép được nói, kết luận đó đi hơi xa so với thông tin hiện có."), c.reason, c.action,
                b("I would welcome evidence that challenges that assessment.", "Tôi sẵn sàng xem bằng chứng phản biện đánh giá ấy."))
            5 -> join(b("I can accept the concern without accepting every proposed consequence.", "Tôi có thể chấp nhận lo ngại nhưng chưa chấp nhận mọi hệ quả được đề xuất."), c.reason, c.action,
                b("That distinction matters if we want qualified agreement rather than reluctant compliance.", "Phân biệt ấy quan trọng nếu muốn sự đồng ý có điều kiện thay vì miễn cưỡng làm theo."))
            else -> join(b("To be clear, I am proposing a next step, not promising a particular outcome.", "Nói rõ hơn, tôi đang đề xuất bước tiếp theo chứ không hứa một kết quả cụ thể."), c.action,
                b("Could we confirm that this is what we have agreed, and leave the unresolved points explicitly open?", "Chúng ta có thể xác nhận đó là điều đã thống nhất và ghi rõ những điểm còn bỏ ngỏ không?"))
        }
    }
    private val purposes = listOf(
        b("What would you recommend as a practical first response?", "Bạn đề xuất phản hồi thực tế đầu tiên như thế nào?"),
        b("How would you justify your preferred approach?", "Bạn giải thích cơ sở cho cách làm mình ưu tiên ra sao?"),
        b("How would you make a careful proposal without implying a final decision?", "Bạn đề xuất thận trọng thế nào để không ngụ ý đã chốt quyết định?"),
        b("How would you explain the wider point in natural language?", "Bạn giải thích ý rộng hơn bằng ngôn ngữ tự nhiên ra sao?"),
        b("How would you challenge an overconfident conclusion politely?", "Bạn phản biện lịch sự một kết luận quá chắc chắn thế nào?"),
        b("How would you acknowledge the concern while limiting your agreement?", "Bạn ghi nhận lo ngại nhưng giới hạn sự đồng ý như thế nào?"),
        b("How would you close the discussion without overstating the agreement?", "Bạn kết thúc trao đổi thế nào để không nói quá mức đồng thuận?"))
    private fun coaching(level: String, t: Topic, purpose: Int): String = when (level) {
        "B2" -> "Nói rõ đề xuất và lý do; ngắt theo cụm ý. ${purposes[purpose].vi}"
        "C1" -> "Ngắt giữa quan điểm, bằng chứng, giới hạn và bước tiếp theo; giữ trọng âm ở ý phản biện. ${purposes[purpose].vi}"
        else -> if (purpose == 3) "Thành ngữ trong tình huống này: ${t.idiom.vi} Chú ý sắc thái, tránh dịch từng chữ."
            else "Luyện sắc thái lịch sự, hàm ý và mức độ cam kết. ${purposes[purpose].vi}"
    }
    val lessons: List<Lesson> = levels.flatMap { level -> AdvancedScenarioData.topics.map { topic ->
        val id = "${level.lowercase()}_${topic.id}"
        val phrases = topic.cases.flatMapIndexed { caseIndex, case -> (0..6).map { purpose ->
            val response = answer(level, topic, case, purpose)
            Phrase(response.en, response.vi, coaching(level, topic, purpose), id = "${id}_p${caseIndex * 7 + purpose + 1}")
        } }
        val conversation = topic.cases.flatMapIndexed { caseIndex, case -> listOf(0, 4, 6).mapIndexed { turn, purpose ->
            val response = answer(level, topic, case, purpose)
            val question = purposes[purpose]
            ConversationTurn("${case.issue.en} ${question.en}", "${case.issue.vi} ${question.vi}",
                response.en, response.vi, id = "${id}_t${caseIndex * 3 + turn + 1}")
        } }
        Lesson(id, topic.en, topic.vi, phrases, conversation)
    } }
    val passages: List<ReadingPassage> = levels.flatMap { level -> AdvancedScenarioData.topics.take(10).map { topic ->
        val id = "passage_${level.lowercase()}_${topic.id}"
        val sentences = topic.cases.flatMapIndexed { index, case ->
            val pair = when (level) {
                "B2" -> listOf(join(case.issue, case.reason), case.action)
                "C1" -> listOf(join(case.issue, case.reason, topic.context[0], topic.context[1]),
                    join(case.action, topic.context[2], b("We should review the resulting decision.",
                        "Chúng ta nên rà soát quyết định được đưa ra.")))
                else -> listOf(answer(level, topic, case, 0), answer(level, topic, case, 3))
            }
            pair.mapIndexed { part, text -> ReadingSentence("${id}_s${index * 2 + part + 1}", text.en, text.vi) }
        }
        ReadingPassage(id, level, "${level.lowercase()}_${topic.id}", topic.en, topic.vi, topic.vi, sentences,
            listOf(PracticeVocabulary("qualified agreement", "đồng ý có điều kiện"),
                PracticeVocabulary("underlying assumption", "giả định nền tảng"),
                PracticeVocabulary("provisional recommendation", "đề xuất tạm thời"), PracticeVocabulary(topic.idiom.en, topic.idiom.vi)))
    } }
    val exercises: List<SpeakingExercise> = levels.flatMap { level -> AdvancedScenarioData.topics.map { topic ->
        val id = "exercise_${level.lowercase()}_${topic.id}"
        SpeakingExercise(id, level, "${level.lowercase()}_${topic.id}", topic.vi,
            topic.cases.mapIndexed { index, case ->
                val text = answer(level, topic, case, index)
                ExerciseQuestion("${id}_q${index + 1}", text.vi, listOf(text.en))
            })
    } }
}
