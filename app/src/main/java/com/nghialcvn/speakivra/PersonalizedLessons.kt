package com.nghialcvn.speakivra

import java.text.Normalizer

/** Only substitute the name slot; preserve its spelling, accents, sentence and stable content IDs. */
internal fun personalizeLesson(lesson: Lesson, name: String): Lesson {
    val displayName = Normalizer.normalize(name.trim(), Normalizer.Form.NFC)
    fun text(value: String) = value.replace("Alex", displayName)
    return lesson.copy(
        phrases = lesson.phrases.map { it.copy(english = text(it.english), vietnamese = text(it.vietnamese), tip = text(it.tip)) },
        conversation = lesson.conversation.map { it.copy(prompt = text(it.prompt), translation = text(it.translation),
            reply = text(it.reply), replyTranslation = text(it.replyTranslation)) }
    )
}
