package com.grayguava.opentune.lite.data

import android.app.Application
import com.grayguava.opentune.lite.data.account.AccountStore
import com.grayguava.opentune.lite.data.autoeq.AutoEq
import com.grayguava.opentune.lite.data.covers.AlbumCovers
import com.grayguava.opentune.lite.data.history.History
import com.grayguava.opentune.lite.data.innertube.InnerTubeXResolver
import com.grayguava.opentune.lite.data.innertube.UpgradedTracks
import com.grayguava.opentune.lite.data.library.LibraryStore
import com.grayguava.opentune.lite.data.settings.AppSettings
import com.grayguava.opentune.lite.data.subsonic.Subsonic
import com.grayguava.opentune.lite.playback.QueueStore

/** Starts the shared services, in the order they depend on each other. */
object Core {
    /**
     * Starts everything the phone and TV app share. New-release alerts, update
     * checks and usage counts belong to it too.
     */
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun init(app: Application) {
        // Media3's warnings and errors (a failed load, a decoder that won't
        // start) into the app's own log as well as logcat, for "Copy log".
        androidx.media3.common.util.Log.setLogger(object : androidx.media3.common.util.Log.Logger {
            private val system = androidx.media3.common.util.Log.Logger.DEFAULT
            override fun d(tag: String, message: String, throwable: Throwable?) = system.d(tag, message, throwable)
            override fun i(tag: String, message: String, throwable: Throwable?) = system.i(tag, message, throwable)
            override fun w(tag: String, message: String, throwable: Throwable?) {
                AppLog.add('W', tag, message, throwable)
                system.w(tag, message, throwable)
            }
            override fun e(tag: String, message: String, throwable: Throwable?) {
                AppLog.add('E', tag, message, throwable)
                system.e(tag, message, throwable)
            }
        })
        AppSettings.init(app)
        History.init(app)
        com.grayguava.opentune.lite.data.history.ListenedCollections.init(app)
        com.grayguava.opentune.lite.data.covers.ArtistPhotos.init(app)
        LoudnessStore.init(app)
        UpgradedTracks.init(app)
        QueueStore.init(app)
        Subsonic.init(app)
        com.grayguava.opentune.lite.data.radio.Radio.init(app)
        // Spotify sign-in is gone; drop any tokens an earlier version kept.
        app.deleteSharedPreferences("spotify")
        // The waveform seek bar is gone; drop the waveforms an earlier version saved.
        java.io.File(app.filesDir, "waveforms").takeIf { it.exists() }?.let { dir -> Thread { dir.deleteRecursively() }.start() }
        com.grayguava.opentune.lite.data.library.ResumePoints.init(app)
        AlbumCovers.init(app)
        AutoEq.init(app)
        com.grayguava.opentune.lite.data.library.LibraryArtwork.init(app)
        LibraryStore.init(app)
        AccountStore.init(app)
        com.grayguava.opentune.lite.data.library.LibraryCache.init(app)
        // After library and account: it sends playlist changes still waiting, when signed in.
        com.grayguava.opentune.lite.data.library.PlaylistSync.init(app)
        InnerTubeXResolver.init(app)
    }

    /**
     * Saves at once what's still waiting for its save (each store waits a moment
     * after a change, so a run of changes is written once): the app is leaving
     * the screen or the player is closing, and the process may go any moment.
     */
    fun flush() {
        LibraryStore.flush()
        History.flush()
        com.grayguava.opentune.lite.data.history.ListenedCollections.flush()
    }
}
