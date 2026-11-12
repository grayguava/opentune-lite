package com.opentune.data.reco

import com.opentune.data.model.Song
import com.opentune.data.settings.Recommender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class RecommendationSourceTest {
    private val picks = listOf(Song("one", "First", "Artist", null), Song("two", "Second", "Artist", null))

    @Test fun shortResultsKeepTheirSourceAndOrder() = runBlocking {
        val result = Recommendations.resolve(Recommender.JIOSAAVN, { picks }, { error("Must not fetch YouTube recommendations") })
        assertEquals(Recommender.JIOSAAVN, result.engine)
        assertEquals(picks, result.songs)
    }

    @Test fun emptyResultsDoNotSilentlySwitchProviders() = runBlocking {
        val result = Recommendations.resolve(Recommender.JIOSAAVN, { emptyList() }, { error("Must not fetch YouTube recommendations") })
        assertEquals(Recommender.YOUTUBE, result.engine)
        assertTrue(result.songs.isEmpty())
    }

    @Test fun anUnavailableSourceFallsBackToYouTubeMusic() = runBlocking {
        val result = Recommendations.resolve(Recommender.JIOSAAVN, { throw java.io.IOException("Unavailable") }, { picks })
        assertEquals(Recommender.YOUTUBE, result.engine)
        assertEquals(picks, result.songs)
    }

    @Test fun cancellingAnOldRecommendationRequestDoesNotStartFallback() = runBlocking {
        try {
            Recommendations.resolve(Recommender.JIOSAAVN, { throw CancellationException("Source changed") }, { error("Cancelled request must not continue") })
            fail("Cancellation must reach the caller")
        } catch (_: CancellationException) { }
    }

    @Test fun otherProviderFallbackStillReportsItsActualSource() = runBlocking {
        val result = Recommendations.resolve(Recommender.JIOSAAVN, { emptyList() }, { picks })
        assertEquals(Recommender.YOUTUBE, result.engine)
        assertEquals(picks, result.songs)
    }
}
