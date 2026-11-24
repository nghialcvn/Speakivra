package com.nghialcvn.speakivra

import org.junit.Assert.*
import org.junit.Test

class LearnersTest {
    @Test fun firstLearnerUsesOriginalProgressFileAndOthersCannotCollide() {
        assertEquals("speakivra_progress", learnerPreferenceName(ORIGINAL_LEARNER))
        val id = "551c9a62-451d-43d5-8a44-9c619011689a"
        assertNotEquals(learnerPreferenceName(ORIGINAL_LEARNER), learnerPreferenceName(id))
        assertEquals("speakivra_learner_$id", learnerPreferenceName(id))
    }
    @Test fun emptyNewInstallHasNoPreassignedLearner() {
        val empty = emptyList<Learner>()
        assertEquals(empty, decodeLearners(encodeLearners(empty)))
    }
    @Test fun firstRealLearnerCanOwnANewUniqueProgressFile() {
        val created = Learner("551c9a62-451d-43d5-8a44-9c619011689a", "Lan", 1)
        assertEquals(listOf(created), decodeLearners(encodeLearners(listOf(created))))
        assertNotEquals("speakivra_progress", learnerPreferenceName(created.id))
    }
    @Test fun vietnameseNamesAndAvatarSurviveRegistryRoundTrip() {
        val learners = listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0), Learner("551c9a62-451d-43d5-8a44-9c619011689a", "Linh", 5))
        assertEquals(learners, decodeLearners(encodeLearners(learners)))
    }
    @Test(expected = IllegalArgumentException::class) fun unsafeIdentifierIsRejected() {
        learnerPreferenceName("../speakivra_progress")
    }
    @Test(expected = IllegalArgumentException::class) fun duplicateIdentityCannotAliasTwoProfiles() {
        decodeLearners(encodeLearners(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0), Learner(ORIGINAL_LEARNER, "An", 1))))
    }
    @Test fun editingKeepsStableIdentityAndOtherLearnersUnchanged() {
        val original = Learner(ORIGINAL_LEARNER, "Nghĩa", 0)
        val other = Learner("551c9a62-451d-43d5-8a44-9c619011689a", "Linh", 1)
        val updated = updateLearnerList(listOf(original, other), original.id, "  Nghĩa An  ", 5)
        assertEquals(original.id, updated.first().id)
        assertEquals(learnerPreferenceName(original.id), learnerPreferenceName(updated.first().id))
        assertEquals("Nghĩa An", updated.first().name)
        assertEquals(5, updated.first().avatar)
        assertEquals(other, updated.last())
    }
    @Test(expected = IllegalArgumentException::class) fun editingCannotTakeAnotherLearnersName() {
        updateLearnerList(listOf(Learner(ORIGINAL_LEARNER, "Nghĩa", 0), Learner("551c9a62-451d-43d5-8a44-9c619011689a", "Linh", 1)), ORIGINAL_LEARNER, "Linh", 2)
    }
}
