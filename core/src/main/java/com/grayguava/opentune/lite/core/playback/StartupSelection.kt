package com.grayguava.opentune.lite.playback

import android.net.Uri
import androidx.media3.datasource.DataSpec
import com.grayguava.opentune.lite.data.model.Song
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * The stream a song starts on. It is YouTube's own, chosen when the item is
 * opened; the player holds the choice while the song is being prepared.
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal class StartupSelection(
    private val song: Song,
    private val original: Uri,
) {
    private val mutex = Mutex()
    @Volatile var selected: Uri? = null
        private set
    suspend fun resolve(spec: DataSpec): DataSpec = mutex.withLock {
        currentCoroutineContext().ensureActive()
        if (selected == null) {
            selected = original
            com.grayguava.opentune.lite.data.DebugLog.d("StartupSelection", "${song.videoId} starts on YouTube")
        }
        spec.withUri(selected!!)
    }
}

internal fun startupToken(uri: Uri): String? = if (videoIdOf(uri) != null) uri.getQueryParameter("startup") else null
internal fun withoutStartup(uri: Uri): Uri = uri.buildUpon().clearQuery().apply {
    uri.queryParameterNames.filterNot { it == "startup" }.forEach { name ->
        uri.getQueryParameters(name).forEach { appendQueryParameter(name, it) }
    }
}.build()
internal fun youtubeFallbackUri(id: String, upgraded: Boolean = false): Uri = streamUri(id, upgraded).buildUpon().appendQueryParameter("fallback", "youtube").build()
