package com.nghialcvn.speakivra

/** Keep lesson IDs/order and progress positions intact; only learner-name fields change. */
internal fun personalizeLesson(lesson: Lesson, name: String): Lesson {
    fun text(value: String) = value.replace("Alex", name)
    return lesson.copy(
        phrases = lesson.phrases.map { phrase ->
            if (phrase.english == "My name is Alex.") Phrase("I'm $name.", "Tôi là $name.", "Nói tự nhiên, nối I'm với tên của bạn.")
            else phrase.copy(english = text(phrase.english), vietnamese = text(phrase.vietnamese), tip = text(phrase.tip))
        },
        conversation = lesson.conversation.map { turn ->
            if (turn.reply == "My name is Alex.") turn.copy(prompt = text(turn.prompt), translation = text(turn.translation),
                reply = "I'm $name.", replyTranslation = "Tôi là $name.")
            else turn.copy(prompt = text(turn.prompt), translation = text(turn.translation), reply = text(turn.reply), replyTranslation = text(turn.replyTranslation))
        }
    )
}
