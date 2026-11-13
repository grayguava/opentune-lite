package com.grayguava.opentune.lite.data

import com.grayguava.opentune.lite.data.settings.LyricsSource
import com.grayguava.opentune.lite.data.settings.ThemeSettings
import com.grayguava.opentune.lite.data.settings.SettingsState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDefaultsTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; coerceInputValues = true }

    @Test fun lyricsPreviewDefaultsOnAndExplicitOffSurvivesBackup() {
        assertTrue(json.decodeFromString<SettingsState>("{}").ui.lyricsPreview)
        val off = json.decodeFromString<SettingsState>("""{"ui":{"lyricsPreview":false}}""")
        assertFalse(off.ui.lyricsPreview)
        assertFalse(json.decodeFromString<SettingsState>(json.encodeToString(off)).ui.lyricsPreview)
    }

    @Test fun missingSavedSettingsUseTheNewDefaults() {
        for (body in listOf("{}", """{"playback":{},"lyrics":{}}""")) {
            val settings = json.decodeFromString<SettingsState>(body)
            assertTrue(settings.playback.stopOnTaskRemoved)
            assertFalse(settings.playback.pauseListeningHistory)
            assertEquals(com.grayguava.opentune.lite.data.history.ListeningHistoryTarget.YOUTUBE_MUSIC, settings.playback.listeningHistoryTarget)
            assertTrue(settings.lyrics.preferWordSynced)
            assertTrue(settings.lyrics.romanize)
            assertEquals(LyricsSource.entries.toList(), settings.lyrics.ordered.map { it.source })
            assertTrue(settings.lyrics.ordered.all { it.enabled })
        }
        // Smoke glass by default, with the dock that minimizes.
        val ui = json.decodeFromString<SettingsState>("{}").ui
        assertFalse(ui.liquidGlass)
        assertEquals(com.grayguava.opentune.lite.data.settings.DockMotion.MINIMIZE, ui.dockMotion)
    }

    @Test fun aPlayerBackgroundThatWasDroppedIsIgnored() {
        // The player has one background now: an old choice of another one is ignored.
        val settings = json.decodeFromString<SettingsState>("""{"theme":{"playerBackground":"MESH","pureBlack":false}}""")
        assertFalse(settings.theme.pureBlack)
        assertEquals(ThemeSettings().copy(pureBlack = false), settings.theme)
    }

    @Test fun aDroppedDockMotionReadsBackAsTheDefault() {
        for (old in listOf("FOLD", "GLIDE", "RETRACT", "CASCADE")) {
            val settings = json.decodeFromString<SettingsState>("""{"ui":{"dockMotion":"$old","dockLens":"JELLY"}}""")
            assertEquals(com.grayguava.opentune.lite.data.settings.DockMotion.MINIMIZE, settings.ui.dockMotion)
            // The rest of the file still reads.
            assertEquals(com.grayguava.opentune.lite.data.settings.DockLens.JELLY, settings.ui.dockLens)
        }
    }

    @Test fun anUpdateAndBackupKeepExplicitSavedChoices() {
        val body = """{
            "playback":{"stopOnTaskRemoved":false,"pauseListeningHistory":true,"listeningHistoryTarget":"YOUTUBE_MUSIC"},
            "lyrics":{"preferWordSynced":false,"romanize":false,
                "sources":[{"source":"SIMPMUSIC","enabled":false}],
                "offsets":{"4NRXx6U8ABQ":500},"chosen":{"4NRXx6U8ABQ":"SIMPMUSIC"}}
        }"""
        val settings = json.decodeFromString<SettingsState>(body)
        assertFalse(settings.playback.stopOnTaskRemoved)
        assertTrue(settings.playback.pauseListeningHistory)
        assertEquals(com.grayguava.opentune.lite.data.history.ListeningHistoryTarget.YOUTUBE_MUSIC, settings.playback.listeningHistoryTarget)
        assertFalse(settings.lyrics.preferWordSynced)
        assertFalse(settings.lyrics.romanize)
        // lrc.red and Paxsenix, new since this was saved, go in ahead; the saved entry comes next, still off.
        assertEquals(listOf(LyricsSource.LRC_RED, LyricsSource.PAXSENIX, LyricsSource.SIMPMUSIC), settings.lyrics.ordered.take(3).map { it.source })
        assertFalse(settings.lyrics.ordered[2].enabled)
        assertEquals(500L, settings.lyrics.offsets["4NRXx6U8ABQ"])
        assertEquals(LyricsSource.SIMPMUSIC, settings.lyrics.chosen["4NRXx6U8ABQ"])
        assertEquals(settings, json.decodeFromString<SettingsState>(json.encodeToString(settings)))
    }
}
