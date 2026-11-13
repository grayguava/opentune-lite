package com.grayguava.opentune.lite.data.reco

import com.grayguava.opentune.lite.data.MusicRepository
import com.grayguava.opentune.lite.data.model.SearchFilter
import com.grayguava.opentune.lite.data.model.SearchResult
import com.grayguava.opentune.lite.data.model.Song
import java.util.Locale

/**
 * Finds a song named by another service on YouTube Music: the closest
 * catalogue song is kept, preferring one whose length is within a few
 * seconds.
 */
object TrackMatch {
    /** The YouTube Music song for [track], or null when nothing is close enough. */
    suspend fun match(track: SourceTrack, exactLength: Boolean = false): Song? {
        val artist = track.artists.firstOrNull().orEmpty()
        val rows = MusicRepository.search("${track.title} $artist".trim(), SearchFilter.SONGS)
        val candidates = rows.mapNotNull { (it as? SearchResult.Track)?.song ?: (it as? SearchResult.TopTrack)?.song }
        return best(track, candidates, exactLength)
    }

    /**
     * The candidate whose title and artist match [track], preferring one
     * whose length is within a few seconds. Null when no title matches.
     * With [exactLength] and the track's length known, only a candidate
     * within 3 s of it counts, so a live, sped-up or cover version isn't
     * taken for the studio one.
     */
    internal fun best(track: SourceTrack, candidates: List<Song>, exactLength: Boolean = false): Song? {
        val strict = exactLength && track.durationMs > 0
        val wantTitle = key(track.title)
        val wantArtists = track.artists.map(::key).filter { it.isNotEmpty() }
        val pick = candidates
            .map { song ->
                val title = key(song.title)
                val artist = key(song.artist)
                var score = 0
                if (title == wantTitle) score += 4 else if (title.contains(wantTitle) || wantTitle.contains(title)) score += 2 else return@map song to -1
                val sameArtist = wantArtists.any { artist.contains(it) || it.contains(artist) }
                if (sameArtist) score += 3
                val seconds = durationSeconds(song.durationText)
                val off = if (seconds != null && track.durationMs > 0) kotlin.math.abs(seconds * 1000 - track.durationMs) else null
                // Whole seconds on YouTube Music, so half a second more for the rounding.
                if (strict && (off == null || off > 3_500)) return@map song to -1
                // Credited to someone else: only the very same title and length will do.
                if (!sameArtist && !placeholder(artist) && (title != wantTitle || off == null || off > 3_500)) return@map song to -1
                if (off != null && off <= 7_000) score += 2
                song to score
            }
            .filter { it.second >= 5 } // a title and the artist, or a title and the length
            .maxByOrNull { it.second }?.first ?: return null
        // An upload credited to "Various Artists" or the like is still the song; it shows who sings it.
        return if (placeholder(key(pick.artist)) && track.artists.isNotEmpty()) pick.copy(artist = track.artists.joinToString(", "), artistId = null) else pick
    }

    /** "Various Artists", "VA", "Unknown Artist": an upload's stand-in for whoever sings. */
    internal fun placeholder(artistKey: String) =
        com.grayguava.opentune.lite.data.model.isPlaceholderArtist(artistKey)

    /** Lower-case letters and digits, with "(feat. …)", "- Remastered 2011" and similar taken off. */
    internal fun key(s: String): String =
        s.lowercase(Locale.ROOT)
            .replace(Regex("""\s*[(\[](?:feat|ft|with|from|remaster|live|radio edit|mono|stereo)[^)\]]*[)\]]"""), "")
            .replace(Regex("""\s+-\s+.*(?:remaster|version|edit|mix|live|mono|stereo).*$"""), "")
            .filter { it.isLetterOrDigit() }

    private fun durationSeconds(text: String?): Long? =
        text?.split(':')?.map { it.trim().toLongOrNull() ?: return null }?.fold(0L) { acc, n -> acc * 60 + n }
}
