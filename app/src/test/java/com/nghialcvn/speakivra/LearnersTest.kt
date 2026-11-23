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
}
