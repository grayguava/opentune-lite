package com.grayguava.opentune.lite.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grayguava.opentune.lite.data.MusicRepository
import com.grayguava.opentune.lite.data.model.SearchFilter
import com.grayguava.opentune.lite.data.model.SearchResult
import com.grayguava.opentune.lite.data.model.durationMillis
import com.grayguava.opentune.lite.data.model.UiState
import com.grayguava.opentune.lite.data.settings.AppSettings
import com.grayguava.opentune.lite.ui.friendlyError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.grayguava.opentune.lite.data.innertube.StreamResolver
import kotlinx.coroutines.Dispatchers

enum class SearchSource(val label: String, val placeholder: String) {
    YOUTUBE("YouTube", "Songs, albums, artists"),
    LIBRARY("Library", "Songs, artists and albums"),
}

class SearchViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val _source = MutableStateFlow(SearchSource.YOUTUBE)
    val source = _source.asStateFlow()

    /** Library matches for the text so far; null while there's nothing to search for. */
    private val _library = MutableStateFlow<List<com.grayguava.opentune.lite.data.model.Song>?>(null)
    val library = _library.asStateFlow()

    /** Playlists in the library whose name matches the text so far. */
    private val _libraryLists = MutableStateFlow<List<LibraryList>>(emptyList())
    val libraryLists = _libraryLists.asStateFlow()


    /** The Local Music folder, read once a search first needs it. */
    private var localSongs: List<com.grayguava.opentune.lite.data.model.Song>? = null

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    /** The query the results belong to; null before the first search. */
    private val _submitted = MutableStateFlow<String?>(null)
    val submitted = _submitted.asStateFlow()

    private val _filter = MutableStateFlow(SearchFilter.ALL)
    val filter = _filter.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    /** What a search for the text so far finds, shown under the suggestions while typing. */
    private val _live = MutableStateFlow<List<SearchResult>>(emptyList())
    val live = _live.asStateFlow()

    private val _results = MutableStateFlow<UiState<List<SearchResult>>>(UiState.Loading)
    val results = _results.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch { watchSuggestions() }
        viewModelScope.launch { watchLive() }
        viewModelScope.launch { watchOtherSources() }
    }

    fun setSource(source: SearchSource) {
        _source.value = source
    }

    @OptIn(FlowPreview::class)
    private suspend fun watchOtherSources() {
        kotlinx.coroutines.flow.combine(_query.map { it.trim() }.distinctUntilChanged().debounce(OTHER_DEBOUNCE_MS), _source) { q, src -> q to src }
            .distinctUntilChanged().collectLatest { (q, src) ->
                when {
                    src == SearchSource.LIBRARY -> {
                        _libraryLists.value = if (q.isEmpty()) emptyList() else searchLists(q)
                        _library.value = if (q.isEmpty()) null else searchLibrary(q)
                    }
                }
            }
    }

    /**
     * Your playlists (made here) and the albums and
     * playlists saved with their heart, every word of [q] in the name; names
     * that start with it first.
     */
    private fun searchLists(q: String): List<LibraryList> {
        val words = q.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        fun matches(name: String) = name.lowercase().let { n -> words.all { it in n } }
        val store = com.grayguava.opentune.lite.data.library.LibraryStore
        val own = store.playlists.value.filter { matches(it.name) }.map { p ->
            LibraryList(
                "pl:${p.id}", p.name,
                "${p.songs.size} songs",
                null, p.songs.map { it.thumbnailUrl }, playlistId = p.id,
            )
        }
        val saved = store.savedLists.value.filter { matches(it.title) }.map { l ->
            LibraryList("saved:${l.browseId}", l.title, l.subtitle.ifBlank { "Saved" }, l.thumbnailUrl, emptyList(), browseId = l.browseId)
        }
        val start = q.lowercase().trim()
        return (own + saved).sortedBy { if (it.title.lowercase().startsWith(start)) 0 else 1 }.take(MAX_LIBRARY_LISTS)
    }

    /** Liked songs, your playlists, downloads and the Local Music folder, every word of [q] in the title, artist or album. */
    private suspend fun searchLibrary(q: String): List<com.grayguava.opentune.lite.data.model.Song> = kotlinx.coroutines.withContext(Dispatchers.Default) {
        val local = localSongs ?: runCatching {
            com.grayguava.opentune.lite.data.local.LocalMusic.tracks(getApplication(), AppSettings.library.value)
        }.getOrDefault(emptyList()).also { localSongs = it }
        val store = com.grayguava.opentune.lite.data.library.LibraryStore
        val all = store.liked.value.map { it.toSong() } +
            store.playlists.value.flatMap { p -> p.songs.map { it.toSong() } } +
            com.grayguava.opentune.lite.data.download.Downloads.doneNow().map { it.song.toSong() } + local
        val words = q.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val title = q.lowercase()
        all.distinctBy { it.videoId }
            .filter { s -> "${s.title} ${s.artist} ${s.albumName.orEmpty()}".lowercase().let { text -> words.all { it in text } } }
            .sortedBy { s ->
                val t = s.title.lowercase()
                when {
                    t == title -> 0
                    t.startsWith(title) -> 1
                    title in t -> 2
                    else -> 3
                }
            }
            .take(MAX_LIBRARY_ROWS)
    }

    /** Remembers [q] as a search in the tab it was made in. */
    fun remember(q: String) {
        val text = q.trim().takeIf { it.isNotEmpty() } ?: return
        when (_source.value) {
            SearchSource.LIBRARY -> Unit
            SearchSource.YOUTUBE -> AppSettings.addRecentSearch(text)
        }
    }

    /**
     * Searches as the words come, a moment after typing stops: the songs,
     * artists and lists found go under the suggestions, so what's wanted can
     * often be tapped without pressing search at all. The search is kept,
     * so pressing search then shows the full results at once.
     */
    @OptIn(FlowPreview::class)
    private suspend fun watchLive() {
        _query.map { it.trim() }.distinctUntilChanged().debounce(LIVE_DEBOUNCE_MS).collectLatest { q ->
            if (q.length < 2 || q == _submitted.value) {
                if (q.isEmpty()) _live.value = emptyList()
                return@collectLatest
            }
            val key = "${SearchFilter.ALL}:${q.lowercase()}"
            val rows = recentSearches[key] ?: runCatching { MusicRepository.search(q, SearchFilter.ALL) }.getOrNull()?.also { recentSearches[key] = it }
            if (rows != null) _live.value = rows.take(LIVE_ROWS)
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun watchSuggestions() {
        _query.map { it.trim() }.distinctUntilChanged().debounce(220).collectLatest { q ->
            if (q.isEmpty() || q == _submitted.value) {
                _suggestions.value = emptyList()
                return@collectLatest
            }
            _suggestions.value = runCatching { MusicRepository.suggestions(q) }.getOrDefault(_suggestions.value)
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun submit(value: String) {
        val q = value.trim()
        if (q.isEmpty()) return
        _query.value = q
        _submitted.value = q
        AppSettings.addRecentSearch(q)
        run()
    }

    fun setFilter(filter: SearchFilter) {
        if (_filter.value == filter) return
        _filter.value = filter
        run()
    }

    fun retry() = run()

    private fun run() {
        val q = _submitted.value ?: return
        val f = _filter.value
        searchJob?.cancel()
        val key = "$f:${q.trim().lowercase()}"
        // A search made a minute ago comes back at once; it's refreshed anyway.
        _results.value = recentSearches[key]?.let { UiState.Success(it) } ?: UiState.Loading
        searchJob = viewModelScope.launch {
            val fresh = try {
                UiState.Success(MusicRepository.search(q, f))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_results.value is UiState.Success) null else UiState.Error(friendlyError(e))
            }
            fresh?.let { _results.value = it }
            (fresh as? UiState.Success)?.data?.let { rows ->
                recentSearches[key] = rows
                warmTopResult(rows)
            }
        }
    }

    /**
     * Finds the stream for the first song in the results before it's tapped,
     * since it's the one most likely to be. The stream URL is cached for 20
     * minutes, so the tap only has to fetch audio.
     */
    private fun warmTopResult(rows: List<SearchResult>) {
        val top = rows.firstNotNullOfOrNull {
            when (it) {
                is SearchResult.TopTrack -> it.song
                is SearchResult.Track -> it.song
                else -> null
            }
        } ?: return
        viewModelScope.launch(Dispatchers.IO) { runCatching { StreamResolver.resolve(top.videoId) } }
    }

    private companion object {
        const val LIVE_DEBOUNCE_MS = 380L
        const val LIVE_ROWS = 12
        const val OTHER_DEBOUNCE_MS = 300L
        const val MAX_LIBRARY_ROWS = 100
    }

    private val recentSearches = object : LinkedHashMap<String, List<SearchResult>>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<SearchResult>>?) = size > 60
    }
}

/** A playlist or saved album in the library that a search found: one of yours ([playlistId]) or one saved from YouTube Music ([browseId]). */
data class LibraryList(
    val key: String,
    val title: String,
    val subtitle: String,
    val coverUrl: String?,
    /** The songs' covers, for a playlist with no cover of its own. */
    val songCovers: List<String?>,
    val playlistId: String? = null,
    val browseId: String? = null,
)

private const val MAX_LIBRARY_LISTS = 8
