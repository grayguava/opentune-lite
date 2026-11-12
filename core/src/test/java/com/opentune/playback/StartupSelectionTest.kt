package com.opentune.playback

import androidx.media3.datasource.DataSpec
import com.opentune.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class StartupSelectionTest {
    @Test fun playerRetainsTheSourceFactorySelectionToken() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val delegate = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
        val factory = object : androidx.media3.exoplayer.source.MediaSource.Factory by delegate {
            override fun createMediaSource(item: androidx.media3.common.MediaItem): androidx.media3.exoplayer.source.MediaSource =
                delegate.createMediaSource(item.buildUpon().setUri(original.buildUpon().appendQueryParameter("startup", "test").build()).build())
        }
        val player = androidx.media3.exoplayer.ExoPlayer.Builder(context).setMediaSourceFactory(factory).build()
        try {
            player.setMediaItem(androidx.media3.common.MediaItem.Builder().setMediaId(song.videoId).setUri(original).build())
            assertEquals("test", startupToken(player.currentMediaItem!!.localConfiguration!!.uri))
        } finally {
            player.release()
        }
    }

    private val song = Song("abcdefghijk", "Song", "Artist", null, durationText = "3:00")
    private val original = streamUri(song.videoId, false)
    private val spec = DataSpec.Builder().setUri(original).setPosition(42).build()

    @Test fun theSongStartsOnYouTube() = kotlinx.coroutines.runBlocking {
        val selection = StartupSelection(song, original)
        val chosen = selection.resolve(spec)
        assertEquals(original, chosen.uri)
        assertEquals(42L, chosen.position)
    }

    @Test fun theChoiceIsKeptForASeek() = kotlinx.coroutines.runBlocking {
        val selection = StartupSelection(song, original)
        val first = selection.resolve(spec)
        val seek = selection.resolve(spec.buildUpon().setPosition(900).build())
        assertEquals(first.uri, seek.uri)
        assertEquals(900L, seek.position)
    }

    @Test fun fallbackBypassesSelectionAndRetaggingDoesNotKeepOldTokens() {
        val fallback = youtubeFallbackUri(song.videoId, upgraded = true)
        assertEquals("youtube", fallback.getQueryParameter("fallback"))
        assertTrue(isUpgradedUri(fallback))
        val tagged = fallback.buildUpon().appendQueryParameter("startup", "old").build()
        assertEquals(fallback, withoutStartup(tagged))
    }
}
