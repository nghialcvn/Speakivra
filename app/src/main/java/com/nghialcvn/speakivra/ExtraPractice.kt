package com.nghialcvn.speakivra

internal data class ReadingSentence(val id: String, val english: String, val vietnamese: String)
internal data class PracticeVocabulary(val english: String, val vietnamese: String)
internal data class ReadingPassage(val id: String, val level: String, val lessonId: String,
    val title: String, val vietnamese: String, val topic: String,
    val sentences: List<ReadingSentence>, val vocabulary: List<PracticeVocabulary>) {
    val english get() = sentences.joinToString(" ") { it.english }
}
internal data class ExerciseQuestion(val id: String, val vietnamese: String, val answers: List<String>) {
    val answer get() = answers.first()
}
internal data class SpeakingExercise(val id: String, val level: String, val lessonId: String,
    val topic: String, val questions: List<ExerciseQuestion>)
internal data class ExerciseEvaluation(val accepted: Boolean, val reference: String, val words: WordMatch)

/** Bidirectional complete alignment: never accept omitted negations/objects or extra wrong words.
 * Equivalent translations must be explicitly authored; no invented semantic or acoustic score. */
internal fun evaluateExercise(question: ExerciseQuestion, transcript: String?): ExerciseEvaluation {
    val results = question.answers.map { answer ->
        val words = matchWords(answer, transcript)
        val reverse = matchWords(transcript.orEmpty(), answer)
        ExerciseEvaluation(words.hasResult && words.incorrect == 0 && reverse.incorrect == 0, answer, words)
    }
    return results.firstOrNull { it.accepted } ?: results.maxBy { it.words.correct.toDouble() / it.words.tokens.size.coerceAtLeast(1) }
}
internal fun nextUnfinishedQuestion(exercise: SpeakingExercise, completed: Set<String>): Int =
    exercise.questions.indexOfFirst { it.id !in completed }.coerceAtLeast(0)
internal fun extraTopicOpen(lessonId: String, load: (Lesson) -> LessonProgress): Boolean =
    Lessons.catalogue.firstOrNull { it.id == lessonId }?.let { topicIsOpen(it, Lessons.catalogue, load) } ?: false
