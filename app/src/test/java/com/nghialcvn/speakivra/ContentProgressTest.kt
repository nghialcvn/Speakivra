package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class ContentProgressTest {
    private val original = Lessons.all.first()
    private fun completed() = LessonProgress(rounds = 3, phase = "summary",
        learnedPhrases = original.phrases.map { it.id }.toSet(),
        learnedTurns = original.conversation.map { it.id }.toSet())
    @Test fun insertingAndReorderingContentResumesOnlyUnlearnedIds() {
        val added = Phrase("A new sentence.", "Câu mới.", "", id = "new-phrase")
        val expanded = original.copy(phrases = listOf(added) + original.phrases.reversed())
        val resumed = completed().reconcile(expanded)
        assertEquals("repeat", resumed.phase)
        assertEquals(0, resumed.phrase)
        assertEquals(3, resumed.rounds)
        assertEquals("summary", resumed.nextPhrase(expanded).phase)
        assertTrue(resumed.nextPhrase(expanded).learnedPhrases!!.containsAll(original.phrases.map { it.id }))
    }
    @Test fun appendedConversationResumesAtTheNewTurn() {
        val expanded = original.copy(conversation = original.conversation + ConversationTurn("New?", "", "Yes.", "", id = "new-turn"))
        val resumed = completed().reconcile(expanded)
        assertEquals("chat", resumed.phase)
        assertEquals(original.conversation.size, resumed.turn)
        assertEquals("summary", resumed.nextTurn(expanded).phase)
    }
    @Test fun completedLegacySaveDoesNotCompleteFutureContent() {
        val expanded = original.copy(phrases = original.phrases + Phrase("New.", "", "", id = "future"))
        val restored = restoreLessonProgress(expanded, { key, default -> if (key.endsWith("rounds")) 2 else default },
            { key, default -> if (key.endsWith("phase")) "summary" else default })
        assertEquals("repeat", restored.phase)
        assertEquals(original.phrases.size, restored.phrase)
        assertEquals(2, restored.rounds)
        assertFalse(restored.learnedPhrases!!.contains("future"))
    }
    @Test fun replayingAnExistingLessonStillUsesTheNormalReviewRound() {
        var state = completed().restart()
        repeat(original.phrases.size) { state = state.nextPhrase(original) }
        assertEquals("chat", state.phase)
        repeat(original.conversation.size) { state = state.nextTurn(original) }
        assertEquals("review", state.phase)
        state = state.answerReview(true).answerReview(true).answerReview(true)
        assertEquals(4, state.rounds)
    }
    @Test fun personalizationKeepsIdsAndContentIdsAreUniquePerLesson() {
        assertEquals(original.phrases.map { it.id }, personalizeLesson(original, "Valkyrie").phrases.map { it.id })
        Lessons.all.forEach {
            assertEquals(it.id, it.phrases.size, it.phrases.map { p -> p.id }.toSet().size)
            assertEquals(it.id, it.conversation.size, it.conversation.map { t -> t.id }.toSet().size)
        }
    }
}
