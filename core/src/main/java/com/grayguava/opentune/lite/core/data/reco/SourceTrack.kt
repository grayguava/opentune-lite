package com.grayguava.opentune.lite.data.reco

/**
 * A song named by title and artists by another service, to be found on
 * YouTube Music. Only names are read; nothing is streamed from the other
 * service.
 */
data class SourceTrack(val title: String, val artists: List<String>, val album: String?, val durationMs: Long)
