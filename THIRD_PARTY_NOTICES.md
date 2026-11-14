# Third-party notices

OpenTune is free software, licensed under the GNU General Public License
version 3 (see [LICENSE](LICENSE)). It is built on other people's code and
reads other people's data; this file says what, and where it came from.

Code adapted from another project keeps that project's licence, and the
libraries below are used under their own. Nothing here is vendored except
where a section says so.

## Adapted code

### BitChord

- Source: [kushagrasinghx/BitChord](https://github.com/kushagrasinghx/BitChord), GPL-3.0
- The Innertube client, its response parser and the YouTube stream resolver
  under `data/innertube/` are adapted from it, as are the InnerTubeX glue and
  the PoToken WebView (`data/innertube/InnerTubeXResolver.kt`,
  `data/innertube/potoken/`, `assets/po_token.html`).
- `FlacInfo` adapts its Samsung FLAC decoder exclusion and its STREAMINFO
  measurement approach, from `AudioOutputPolicy.kt` and `PlaybackService.kt`
  at commit `edc1e6f36febf71a118e649411d2fc21b70ead45`. The affected
  Samsung vendor decoders are excluded, and real FLAC metadata is read
  rather than treating float PCM as proof of a hi-res master.

### NewPipe

- Source: [TeamNewPipe/NewPipe](https://github.com/TeamNewPipe/NewPipe), GPL-3.0
- The PoToken WebView follows NewPipe's design.

### Echo Music

- Source: [EchoMusicApp/Echo-Music](https://github.com/EchoMusicApp/Echo-Music/tree/a31a72bd958f7cdc128db17d2ea6ae90c3489aa1),
  GPL-3.0
- The optional motion effects were adapted from `ui/player/MiniPlayer.kt`
  (the animated glow and the wavy play button) and `ui/component/BottomSheet.kt`
  (the soft spring), under `app/src/main/kotlin/com/music/echo/` at that
  revision.
- OpenTune's adaptations lived in `ui/player/EchoMotion.kt` and
  `ui/PageMotion.kt`. Those effects have since been removed; this notice
  stays as the record of where the code came from.

## Libraries

### InnerTubeX

- Source: [MetrolistGroup/innertubex](https://github.com/MetrolistGroup/innertubex), GPL-3.0
- The YouTube client library the Innertube code is built on.

### NewPipeExtractor and Nanojson

- Source: [TeamNewPipe/NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor)
  and [TeamNewPipe/nanojson](https://github.com/TeamNewPipe/nanojson), GPL-3.0
- The stream extractor library, and the JSON parser it uses.

### Mozilla Rhino

- Source: [mozilla/rhino](https://github.com/mozilla/rhino), MPL-2.0
- The JavaScript engine that runs YouTube's own player code to work out how
  to stream a song.

### Ktor and OkHttp

- Source: [ktorio/ktor](https://github.com/ktorio/ktor) and
  [square/okhttp](https://github.com/square/okhttp), Apache-2.0
- The HTTP client the app talks to services with, and the one it runs on.

### Coil

- Source: [coil-kt/coil](https://github.com/coil-kt/coil), Apache-2.0
- Loading and caching the artwork.

### Jsoup

- Source: [jsoup/jsoup](https://github.com/jsoup/jsoup), MIT
- Parsing the pages the lyrics and the metadata come from.

### Protocol Buffers

- Source: [protocolbuffers/protobuf](https://github.com/protocolbuffers/protobuf),
  BSD-3-Clause
- The wire format the Innertube responses arrive in.

### Kotlin libraries

- Source: [Kotlin/kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
  and [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines),
  Apache-2.0
- JSON, and the background work the app does off the main thread.

### AndroidX

- Source: [androidx/androidx](https://github.com/androidx/androidx), Apache-2.0
- Jetpack Compose for the interface, Media3 for playback, WorkManager for
  the background jobs, and Lifecycle and Navigation for the app's own state.

### Material Kolar

- Source: [material-kolor/material-kolor](https://github.com/material-kolor/material-kolor),
  Apache-2.0
- The colour scheme the theme is built from.

### Haze

- Source: [chrisbanes/haze](https://github.com/chrisbanes/haze), Apache-2.0
- The frosted glass behind the floating bars.

### Kyant backdrop

- Source: [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass),
  Apache-2.0
- The backdrop layer the liquid glass bends.

## Data and services

### MusicBrainz and the Cover Art Archive

- Source: [metabrainz/musicbrainz-server](https://github.com/metabrainz/musicbrainz-server)
  and [coverartarchive.org](https://coverartarchive.org), run by the MetaBrainz
  Foundation
- Album covers and artist photographs.

### Apple Music and iTunes

- Source: [music.apple.com](https://music.apple.com) and the
  [iTunes Search API](https://performance-partners.apple.com/search-api)
- Animated cover art, artist photographs, track metadata, and word-by-word
  lyrics through Paxsenix.

### AutoEq

- Source: [jaakkopasanen/AutoEq](https://github.com/jaakkopasanen/AutoEq), MIT
- The headphone correction data behind the equalizer presets.

### SponsorBlock

- Source: [Ajaxy/sponsor.ajay.app](https://github.com/ajaxy/sponsor.ajay.app),
  CC BY-NC-SA 4.0 for the data
- The skip segments that cut out the parts of a song people ask to skip.

### Radio Browser

- Source: [radio-browser.info](https://www.radio-browser.info)
- The community directory of radio stations.

### Lyrics services

- Sources: [lrc.red](https://lrc.red/),
  [lrclib.net](https://lrclib.net/api),
  [lyricsplus.binimum.org](https://lyricsplus.binimum.org) and its mirrors,
  [lyrics-api.binimum.org](https://lyrics-api.binimum.org/),
  [api-lyrics.simpmusic.org](https://api-lyrics.simpmusic.org/v1/),
  [music.163.com](https://music.163.com/) for NetEase, and
  [lyrics-api.boidu.dev](https://lyrics-api.boidu.dev/getLyrics) for KuGou
- Word-by-word and line-synced lyrics, each read from the service that has
  them.
