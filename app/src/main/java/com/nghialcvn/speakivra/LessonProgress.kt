package com.nghialcvn.speakivra

internal data class LessonProgress(
    val phrase: Int = 0, val turn: Int = 0, val review: Int = 0,
    val correct: Int = 0, val rounds: Int = 0, val phase: String = "repeat",
    val learnedPhrases: Set<String>? = null, val learnedTurns: Set<String>? = null,
    val supplemental: Boolean = false
) {
    fun nextPhrase(lesson: Lesson): LessonProgress {
        val learned = (learnedPhrases ?: lesson.phrases.take(phrase).map { it.id }.toSet()) + lesson.phrases[phrase].id
        val remaining = lesson.phrases.indexOfFirst { it.id !in learned }
        return if (remaining >= 0) copy(phrase = remaining, learnedPhrases = learned)
        else {
            val pending = lesson.conversation.indexOfFirst { it.id !in learnedTurns.orEmpty() }
            if (pending >= 0) copy(turn = pending, phase = "chat", learnedPhrases = learned)
            else copy(phase = if (supplemental) "summary" else "review", learnedPhrases = learned)
        }
    }
    fun nextTurn(lesson: Lesson): LessonProgress {
        val learned = (learnedTurns ?: lesson.conversation.take(turn).map { it.id }.toSet()) + lesson.conversation[turn].id
        val remaining = lesson.conversation.indexOfFirst { it.id !in learned }
        return if (remaining >= 0) copy(turn = remaining, learnedTurns = learned)
        else copy(phase = if (supplemental && learnedPhrases?.containsAll(lesson.phrases.map { it.id }) == true) "summary" else "review", learnedTurns = learned)
    }
    /** Old completed rounds remain historical; pending content controls completion now. */
    fun reconcile(lesson: Lesson): LessonProgress {
        val p = learnedPhrases ?: return this
        val t = learnedTurns ?: return this
        val missingPhrase = lesson.phrases.indexOfFirst { it.id !in p }
        val missingTurn = lesson.conversation.indexOfFirst { it.id !in t }
        return when {
            missingPhrase >= 0 && phase in listOf("repeat", "summary") -> copy(phrase = missingPhrase, phase = "repeat", supplemental = supplemental || phase == "summary")
            missingTurn >= 0 && phase in listOf("chat", "summary") -> copy(turn = missingTurn, phase = "chat", supplemental = supplemental || phase == "summary")
            else -> this
        }
    }
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
    val saved = LessonProgress(readInt(prefix + "phrase", legacy), readInt(prefix + "turn", 0),
        readInt(prefix + "review", 0), readInt(prefix + "correct", 0), readInt(prefix + "rounds", 0),
        readString(prefix + "phase", "repeat")).sanitized(lesson)
    val baseline = legacyLessonContent[lesson.id]
    val oldPhrases = baseline?.first.orEmpty()
    val oldTurns = baseline?.second.orEmpty()
    fun restored(key: String, fallback: Set<String>): Set<String> {
        val raw = readString(prefix + key, "__legacy__")
        return if (raw == "__legacy__") fallback else raw.split("|").filter { it.isNotEmpty() }.toSet()
    }
    val phrasesDone = if (saved.phase != "repeat") oldPhrases else oldPhrases.take(saved.phrase)
    val turnsDone = if (saved.phase in listOf("review", "summary")) oldTurns
        else if (saved.phase == "chat") oldTurns.take(saved.turn) else emptyList()
    return saved.copy(
        supplemental = readInt(prefix + "supplemental", 0) == 1,
        learnedPhrases = restored("learned_phrases", phrasesDone.toSet()),
        learnedTurns = restored("learned_turns", turnsDone.toSet())
    ).reconcile(lesson)
}
