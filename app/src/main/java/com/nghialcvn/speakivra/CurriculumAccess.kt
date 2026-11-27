package com.nghialcvn.speakivra

internal object CourseLevels {
    val all = listOf("A1", "A2", "B1", "B2", "C1", "C2")
    fun title(level: String) = when (level) {
        "A1" -> "A1 • Căn bản"
        "A2" -> "A2 • Sơ trung cấp"
        "B1" -> "B1 • Trung cấp"
        "B2" -> "B2 • Trung cao cấp"
        "C1" -> "C1 • Cao cấp"
        "C2" -> "C2 • Chuyên nghiệp"
        else -> level
    }
    fun prerequisite(level: String): String? = when (level) {
        "B2" -> "B1"; "C1" -> "B2"; "C2" -> "C1"; else -> null
    }
    fun lockMessage(level: String) = prerequisite(level)?.let { "Hoàn thành 20/20 chủ đề $it để mở" }
        ?: "Học xong chủ đề trước để mở"
}

/** Completed rounds survive replay, so repeating a finished topic never relocks the next level. */
internal fun levelIsOpen(level: String, catalogue: List<Lesson>, load: (Lesson) -> LessonProgress): Boolean {
    if (level !in CourseLevels.all) return false
    val previous = CourseLevels.prerequisite(level) ?: return true
    val required = catalogue.filter { Lessons.levelOf(it) == previous }
    return required.size == 20 && levelIsOpen(previous, catalogue, load) && required.all { load(it).rounds > 0 }
}

/** Existing first-three-topic progression is retained inside each unlocked level. */
internal fun topicIsOpen(lesson: Lesson, catalogue: List<Lesson>, load: (Lesson) -> LessonProgress): Boolean {
    if (HotelCurriculum.isHotel(lesson)) return catalogue.any { it.id == lesson.id }
    if (!levelIsOpen(Lessons.levelOf(lesson), catalogue, load)) return false
    val level = catalogue.filter { Lessons.levelOf(it) == Lessons.levelOf(lesson) }
    val index = level.indexOfFirst { it.id == lesson.id }
    if (index < 0) return false
    return index < 3 || level.subList(2, index).all { load(it).rounds > 0 }
}
