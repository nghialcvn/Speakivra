package com.nghialcvn.speakivra

/** The first three topics in EACH level are open. Completing #3 opens #4, etc.
 * Historical completed rounds keep later topics open while a learner repeats a lesson. */
internal fun topicIsOpen(lesson: Lesson, catalogue: List<Lesson>, load: (Lesson) -> LessonProgress): Boolean {
    val level = catalogue.filter { Lessons.levelOf(it) == Lessons.levelOf(lesson) }
    val index = level.indexOfFirst { it.id == lesson.id }
    if (index < 0) return false
    return index < 3 || level.subList(2, index).all { load(it).rounds > 0 }
}
