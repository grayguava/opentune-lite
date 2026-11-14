package com.grayguava.opentune.lite.data.covers

import com.grayguava.opentune.lite.data.model.PLAYER_ART_PX
import com.grayguava.opentune.lite.data.model.Song
import com.grayguava.opentune.lite.data.model.artworkAt

/**
 * One high-resolution image per playing track, even if metadata arrives again
 * later. The one exception is [better]: a sharper catalogue cover found for
 * this very track while it plays, which replaces the first image once.
 */
class PlaybackArtwork(
    // A catalogue cover found before isn't started with: it comes as [better] once it has
    // loaded, as a link that no longer worked left the player with no cover at all.
) {
    private var id: String? = null
    private var url: String? = null
    private var upgraded = false
    fun choose(song: Song, better: String? = null): String? {
        if (id != song.videoId || url == null) {
            id = song.videoId
            upgraded = false
            url = song.thumbnailUrl.artworkAt(PLAYER_ART_PX)
        }
        // A downloaded song's own saved cover stays: it's what was kept with the file.
        if (better != null && !upgraded) {
            upgraded = true
            url = better.artworkAt(PLAYER_ART_PX)
        }
        return url
    }
    fun clear() { id = null; url = null; upgraded = false }
}
