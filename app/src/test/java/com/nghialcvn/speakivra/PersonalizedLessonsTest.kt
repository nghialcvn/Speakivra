package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class PersonalizedLessonsTest {
    @Test fun everyNameTemplateUsesTheCurrentLearnerAndKeepsLessonPositions() {
        val original = Lessons.all.first()
        val tailored = personalizeLesson(original, "Nghĩa")
        assertEquals(original.id, tailored.id)
        assertEquals(original.phrases.size, tailored.phrases.size)
        assertEquals(original.conversation.size, tailored.conversation.size)
        assertEquals("My name is Nghĩa.", tailored.phrases[2].english)
        assertEquals("Tên tôi là Nghĩa.", tailored.phrases[2].vietnamese)
        assertEquals("My name is Nghĩa.", tailored.conversation[0].reply)
        assertEquals(original.conversation[0].prompt, tailored.conversation[0].prompt)
        assertEquals(original.phrases[0], tailored.phrases[0])
        assertEquals("My name is Alex.", original.phrases[2].english)
    }
    @Test fun switchingOrRenamingDoesNotReuseAnotherLearnersNameOrAlexRecording() {
        val first = personalizeLesson(Lessons.all.first(), "Nghĩa")
        val second = personalizeLesson(Lessons.all.first(), "Linh")
        assertFalse(second.phrases[2].english.contains("Nghĩa"))
        assertEquals("My name is Linh.", second.phrases[2].english)
        assertNull(SpeechProfiles.forText(first.phrases[2].english).clip)
        assertNull(SpeechProfiles.forText(second.conversation[0].reply).clip)
        assertNotNull(SpeechProfiles.forText(second.phrases[0].english).clip)
        Lessons.all.drop(1).forEach { assertEquals(it, personalizeLesson(it, "Nghĩa")) }
    }
}
