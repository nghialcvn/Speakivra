package com.nghialcvn.speakivra

internal data class LessonProgress(
    val phrase: Int = 0, val turn: Int = 0, val review: Int = 0,
    val correct: Int = 0, val rounds: Int = 0, val phase: String = "repeat"
) {
    fun nextPhrase(lesson: Lesson) = if (phrase < lesson.phrases.lastIndex) copy(phrase = phrase + 1)
        else copy(phase = "chat")
    fun nextTurn(lesson: Lesson) = if (turn < lesson.conversation.lastIndex) copy(turn = turn + 1)
        else copy(phase = "review")
    fun answerReview(isCorrect: Boolean): LessonProgress {
        if (phase != "review" || review >= 3) return this
        val next = review + 1
        return copy(review = next, correct = correct + if (isCorrect) 1 else 0,
            rounds = rounds + if (next == 3) 1 else 0, phase = if (next == 3) "summary" else "review")
    }
    fun restart() = LessonProgress(rounds = rounds)
    fun sanitized(lesson: Lesson): LessonProgress {
        val safePhase = phase.takeIf { it in listOf("repeat", "chat", "review", "summary") } ?: "repeat"
        return copy(phrase = phrase.coerceIn(0, lesson.phrases.lastIndex),
            turn = turn.coerceIn(0, lesson.conversation.lastIndex),
            review = review.coerceIn(0, 3), correct = correct.coerceIn(0, review.coerceIn(0, 3)),
            rounds = rounds.coerceAtLeast(0),
            phase = if (safePhase == "review" && review >= 3) "summary" else safePhase)
    }
}

/** Three meaning checks, using distinct translations from this lesson as distractors. */
internal data class ReviewQuestion(val phrase: Phrase, val options: List<String>, val answer: Int)
internal fun reviewQuestion(lesson: Lesson, index: Int): ReviewQuestion {
    val source = listOf(0, 3, 6)[index.coerceIn(0, 2)]
    val choices = listOf(source, (source + 1) % lesson.phrases.size, (source + 2) % lesson.phrases.size)
    val rotation = index % choices.size
    val ordered = choices.drop(rotation) + choices.take(rotation)
    return ReviewQuestion(lesson.phrases[source], ordered.map { lesson.phrases[it].vietnamese }, ordered.indexOf(source))
}

/** Preserve the original first-lesson position; never import it into another topic. */
internal fun restoreLessonProgress(
    lesson: Lesson, readInt: (String, Int) -> Int, readString: (String, String) -> String
): LessonProgress {
    val prefix = "lesson.${lesson.id}."
    val legacy = if (lesson.id == "greetings") readInt("phrase", 0) else 0
    return LessonProgress(readInt(prefix + "phrase", legacy), readInt(prefix + "turn", 0),
        readInt(prefix + "review", 0), readInt(prefix + "correct", 0), readInt(prefix + "rounds", 0),
        readString(prefix + "phase", "repeat")).sanitized(lesson)
}
