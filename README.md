<p align="center">
  <a href="https://github.com/grayguava/opentune-lite/releases/latest"><b>Download the latest APK</b></a>
  &nbsp;·&nbsp;
  <a href="https://discord.gg/6udrCVh5n">Discord</a>
  &nbsp;·&nbsp;
  <a href="#features">Features</a>
  &nbsp;·&nbsp;
  <a href="#building">Build it yourself</a>
</p>

<p align="center">
  <a href="https://github.com/grayguava/opentune-lite/actions/workflows/ci.yml"><img src="https://github.com/grayguava/opentune-lite/actions/workflows/ci.yml/badge.svg" alt="CI"></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Kotlin-Compose-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin and Jetpack Compose">
  <a href="LICENSE"><img src="https://img.shields.io/badge/LICENSE-GPL--3.0-DA2C43" alt="GPL-3.0"></a>
</p>

# OpenTune

An Android music player for YouTube Music. It talks to YouTube's internal
("Innertube") API directly, so there's no API key, no account required and
no server of your own. Sign in if you want your likes and playlists.

Built with Kotlin, Jetpack Compose and Media3. Android 8.0 and newer.

Questions, ideas or bug reports: join the
[Discord](https://discord.gg/6udrCVh5n).

## Install

1. Open the [latest release](https://github.com/grayguava/opentune-lite/releases/latest)
   and download the APK for your phone. `arm64-v8a` fits almost every phone
   from the last several years; `universal` runs on anything.
2. Open the file and allow your browser or file manager to install apps
   when Android asks.
3. That's it.

Each release lists SHA-256 checksums of its APKs in `SHA256SUMS.txt`.

## Highlights

- **No ads in the app, no account, no server.** Signing in is optional and
  only brings your likes and playlists along.
- **Best audio by default**, with crossfade, loudness levelling, a 15-band
  equalizer and AutoEq correction for 8,000+ headphones.
- **Synced lyrics**, lit word by word, that also work offline with downloads.
- **Live radio** from 50,000+ stations, nearest first.
- **Name that song**: hold the phone up and it tells you what's playing.
- **Podcasts**, downloads, Android Auto and a home-screen widget.

## Features

### Listening

- **JioSaavn renditions.** An optional 320 kbps route, with YouTube Music as the
  fallback. The quality label under the seek bar reports the source and the
  detected audio format.

- **Two stream engines that switch on their own.**
  InnerTubeX leads and
  OpenTune's own client walk backs it up; after a failure the walk leads
  for ten minutes, then InnerTubeX gets its turn back. NewPipeExtractor is
  the last resort. Every stream URL is
  test-fetched before playback, so a dead link fails fast instead of
  spinning. Stats for nerds shows which engine served the track.
- **Best audio by default.** Streams are ranked by how they sound, not by
  raw bitrate, so Opus wins over AAC at a similar rate, on Wi-Fi and mobile
  data alike, with a lower ceiling available per network.
- **Quality upgrade.** A few seconds into a song, OpenTune looks for a
  clearly better copy (your Premium account's 256 kbps streams, or Opus
  when the song started on AAC) and switches to it without stopping. The
  player's "…" menu has "Upgrade quality" to ask on demand.
- **Crossfade** from 1 to 12 seconds, with equal-power curves so the blend
  stays level. Off by default.
- **One steady volume.** Loudness normalization uses YouTube's own
  measurement of each song. Figures are kept on the device, so songs played
  from the cache or from downloads get the same level as fresh ones. A
  volume level sets where songs land: Quiet, Normal (YouTube's reference)
  or Loud, the default, about 5 dB higher with a limiter so peaks don't
  clip. On the phone's own speaker songs are never turned down, only quiet
  ones lifted; headphones and Bluetooth are fully levelled.
- **Your phone's own sound effects.** The audio session is opened to the
  system, so Dolby Atmos, Samsung SoundAlive, the system equalizer and
  similar effects apply to OpenTune the way they do to the stock players.
- **A clean signal path.** With the equalizer and effects off, decoded
  audio goes to the output untouched. When they're on, the app's own DSP
  adds headroom so boosts don't clip.
- **Bit-perfect USB output** (Android 14+, optional). A USB DAC gets the
  song at its own rate with no mixing, resampling or effects, through
  Android's bit-perfect mixer. The app applies the volume, and at full
  volume the signal is bit-exact.
- **Clarity** (optional). A tone curve that cuts rumble, firms up the
  bass, clears mud and adds presence and air, with headroom and a soft
  limiter so peaks stay clean.
- **8D audio** (Remix). The music circles your head, slowly, at a medium
  pace or fast: it pans from ear to ear with the small delay and softening
  a sound has when it's beside or behind you. Best with headphones.
- **3D sound** (Remix). The music goes all the way round you: in front,
  overhead, behind and underneath, on a circle that tips up and over
  every few turns. The near ear hears it louder and a fraction sooner,
  the far ear duller; the treble changes the way the outer ear changes
  it, brighter above and darker below; and a few faint echoes place it
  in a room around you. Slow, medium or fast; best with headphones. It
  isn't Dolby Atmos, which needs tracks mixed for it; it moves any
  stereo song.
- **A sleep timer that winds down.** Over the last few minutes (a third of
  a short timer, at most three) the music fades out and the player
  dims; "End of song" fades the last 15 seconds. A switch in the sleep
  timer turns it off.
- **Fast starts and skips.** Playback begins after half a second of audio.
  The songs either side of the current one are found before you get to
  them, and the next one is buffered while this one plays. Played songs
  stay in a song cache (512 MB by default) for instant replays and seeks.
- **Daily mixes.** Home has mixes made on the phone from what you play,
  new each day: one for each of your top three artists with the songs you
  play around them, one for this time of day, On repeat and Rediscover
  (favourites you haven't played lately). When a mix runs out, autoplay
  carries on.
- **Covers for your playlists.** A playlist made in the app shows four of
  its songs' covers as a collage; with fewer, it gets a soft gradient in
  colours picked from its name, so it always looks the same.
- **Queue and radio.** Autoplay keeps similar songs coming, also after a
  song you started on its own from Home or search. Shuffle,
  repeat, play next, add to queue, swipe-to-queue on any row, and
  drag-to-reorder in the player's queue. Shuffle, repeat and autoplay
  each feel different under your finger (a riffle, a loop closing, a
  swell) and move in their own way as they change.
- **Pick who recommends.** Autoplay and Home's "Because you played" can
  follow YouTube Music's radio or JioSaavn's (Settings → Playback →
  Recommendations). JioSaavn's picks come from its public recommendations.
  No account or key is needed. Only song names are read, each one is found
  on YouTube Music and plays from there, and when a service doesn't know a
  song, YouTube Music's radio takes over.
- **Sound tools.** A 15-band equalizer (ISO 25 Hz to 16 kHz) with presets,
  tone and balance, bass boost,
  stereo widening, skip silence, USB DAC preference, optional 32-bit float
  output, and a Remix sheet for speed, pitch and reverb (Slowed + reverb,
  Nightcore and more).
- **Headphone correction.** Pick your headphones from AutoEq's 8,000+
  measurements and their parametric correction runs ahead of your own EQ,
  so you start from a neutral sound.
- **Sleep timer.** Stop after 15 to 90 minutes, or at the end of the song.
- **SponsorBlock.** Music videos skip the parts viewers have marked as
  non-music (talking, skits), sponsor reads, self-promotion and like or
  subscribe reminders, using [SponsorBlock](https://sponsor.ajay.app)'s
  segments. Choose the categories in Settings. Only a 4-character hash of
  the video id is sent.
- **Headphones and volume** (optional, off by default). Playback can carry
  on when headphones are plugged in or a Bluetooth device connects, and
  pause when the volume is turned all the way down, playing again when it
  comes back up.
- **Podcasts.** A tab of its own: search shows or episodes, browse
  topics, popular episodes and shows to try from YouTube Music, and show
  pages with every episode. Subscribe to shows (kept on the phone, no
  account needed) to see their newest episodes up top. Episodes pick up
  where you stopped, with "Continue listening" for the ones in progress
  and a played mark for the ones finished.
- **Internet radio.** 50,000+ live stations from
  [Radio Browser](https://www.radio-browser.info), the free community
  directory: stations near you, your country's most played, worldwide
  favourites, genres and search. "Near you" uses approximate location,
  only when you ask, sends Radio Browser a position rounded to about a
  kilometre and keeps nothing; it lists stations nearest first with how far
  away they are, then stations listed for your city or state that have no
  map location (often the public broadcaster's FM channels), with the FM
  frequency when the name carries one. Stations the directory lacks can be
  added by stream address, or suggested to Radio Browser. The song on air shows in the player and notification when
  the station sends it. Favourite stations are in Android Auto too.
- **Picks up where you left off.** The queue, song and position come back
  at launch, paused, without fetching anything until you press play.
- **Home-screen widget.** Cover, title and artist, with previous,
  play/pause, next and like. Play on the widget, or on a Bluetooth headset,
  starts the saved queue even after the app was closed.
- **Like and shuffle in the notification.** Both sit in the media
  notification's extra buttons and in Android Auto, and stay in step with the
  app.
- **Android Auto.** Browse Home, Recent and Library (Liked, Downloads,
  playlists, radio stations) on the car screen, search by typing or voice ("play … on
  OpenTune"), and tap a song to play its list from there. Sideloaded
  builds show up in Android Auto once "Unknown sources" is turned on in
  Android Auto's developer settings.
- **Opens YouTube links.** music.youtube.com, youtube.com and youtu.be
  links, or links shared to the app, open in OpenTune.

### Library and account

- **Sign in to YouTube Music** on Google's own page inside the app. Likes
  sync to the account, your playlists show in Library, and Home turns
  personal. The login cookie stays in app-private storage and is left out
  of backups.
- **Downloads** for offline listening, with a quality setting and Wi-Fi
  only by default. They keep going after the app is closed. Every
  playlist and album has Download all, which counts its way through and
  ends with a little run of taps.
- **Add songs to your playlists** from the playlist itself: search, or
  pick from what you played lately, and tap + on each one.
- **Library:** a card for your year so far (minutes, plays, top song and
  artist) that opens your Wrapped, shortcuts to Liked, Downloads, On this
  phone and Replay, your playlists from the app and from YouTube in one
  list, and the last five songs you played, with the rest behind
  "Show all".
- **Replay:** top songs, artists and albums from this device's history.
- **Wrapped:** your listening as a quiet animated story. Each page is a
  flat field of colour with one large shape drifting slowly on it (a low
  sun, a wave, nested arcs, a grid of dots) under a fine film grain, with
  the numbers set large in a serif and headlines that ink in from the
  left. A new page opens as a circle growing from where you tapped. The
  intro scatters your top covers like prints, and the top artist and top
  song pages take their colours from the cover. Minutes listened, top artist and top five, top song and top five, what
  part of the day you listen most, your biggest month and day, your
  longest streak, the song you played most in one day and the one it all
  started with, then a summary card with Play, Shuffle and Share, which
  sends your Wrapped as a story-sized poster. Covers this
  year, the last 30 days or all time. Tap to go on, tap the left side to
  go back, hold to pause.
- **Your own music server** (optional): Navidrome, Gonic, Airsonic,
  Nextcloud Music or any Subsonic-compatible server. Browse recently added
  and most played albums, artists and playlists, search, and play songs
  as they're stored, so FLAC and hi-res files stay lossless. Also in
  Android Auto. Only a sign-in token is kept, never the password, and it's
  left out of backups.
- **Your listening, where it counts**: signed in to YouTube Music, plays
  go into your YouTube Music history.
- **Sharper album covers**: the player shows the album's cover from Apple
  Music (up to 3000px), Deezer (1200px) or the Cover Art Archive, in place
  of YouTube's smaller cover or a video frame. A cover is only taken when
  the title, artist and length (within 3 s) match.
- **New-release alerts.** Follow an artist from their page (no account
  needed) and a notification tells you when they put out an album or
  single. Checked twice a day.
- **Explicit content, marked.** Songs and albums YouTube Music marks
  explicit carry a small "E" in front of the artist, as in the official
  apps: in lists, on Home and in search, on the album page, in the player
  and the mini player. The mark is kept with liked songs, playlists,
  downloads and history.
- **Hide explicit content** (optional): those songs and albums are left
  out of Home, search, album and artist pages, autoplay and radio. Tracks
  the catalogue doesn't label still show.
- **Set as ringtone.** A downloaded song or a file on the phone can be the
  ringtone, notification sound or alarm. A downloaded song is copied to
  Ringtones/OpenTune first, since the system can't read app storage.
- **Export and import** of settings, history, likes and playlists as JSON.

### Look and feel

- **Liquid Glass.** The floating bars and player buttons can frost and bend
  what's behind them, like Apple's material (Android 13 and newer; smoke
  glass below that, solid with "Reduce dynamic blur"). The glass blurs the
  real page as it scrolls under it, not a flat colour. Album, playlist and
  artist pages get a frosted top bar once the header scrolls away, which
  fades out softly below the bar instead of ending in a line, and popup
  sheets let the phone's blur of the screen behind show through.
- **Pull to refresh with the logo.** On Home and Podcasts, pulling down
  brings in the OpenTune mark and fills it with the playing song's colours
  from left to right while the page follows your finger with some give; past the line
  it ticks and pops, while it loads the mark sways like a sound wave with
  light running across it, and when it's done it shrinks away.
- **Skeleton loading.** Pages show the shape of what's coming while they
  load, with one soft band of light sweeping across every placeholder.
- **A dock that gets out of the way.** Home, Explore, Podcasts and Library
  sit in a floating glass pill, each an icon over its name; a soft lens of
  the accent colour slides to the open one and its icon fills in. Search
  has its own round button beside the pill. The now-playing card above
  takes its tint from the cover and has the play button inside a progress
  ring. Scroll down and the dock gets out of the way; scroll up and it
  comes back. Pick how: Minimize (like iOS 26, the default: the dock pill
  contracts to the left into a circle holding the open tab, the other tabs
  slipping under its edge, with the song in a slim bar beside it), Capsule
  (the tabs and the song gather into one small capsule in the middle, the
  open tab at one end and the cover, ringed by progress, at the other) or
  Sink (the tabs sink away into a blur and the song settles into their
  place). The lens stretches like a drop and wobbles into place.
- **Smoke glass.** The dock, buttons and bars are dark smoked glass, over
  the real page as it scrolls under them. A switch in Settings › Appearance
  turns on Apple's Liquid Glass instead: hardly frosted, with a deep lens at
  the edges that bends and splits the light, vibrant colour through it, a
  bright specular rim and a soft inner shade (Android 13 and newer). Pages
  also blur and fade as they scroll under the status bar; that's a switch of
  its own.
- **Motion to taste.** Pages can Slide, Fade, Zoom or Rise into view, and
  the player can open on a Spring, a Smooth glide, a Bouncy overshoot or
  Snappy. On phones with 90 or 120 Hz screens the app asks for the fastest
  rate, which can be turned off to save battery.
- **Home greets you** by the time of day and your name, with your avatar
  leading to settings. Below YouTube Music's own shelves it adds a radio
  of your last song, albums, singles and similar artists for the artists
  you play most, new releases, the charts, Explore, and playlists for a
  couple of moods that change daily.
- **Explore as a board.** Pills for artists to jump to (the ones you play
  most first, then the charts' top artists), chips to narrow it to moods,
  genres, new releases or charts, and a two-column board of mood cards,
  new albums and chart playlists at different heights, Pinterest-style.
- **An Apple Music-style player.** Cover, lyrics and queue modes, heart and
  "…" buttons, the current lyric line under the title, the artist name
  opening their page, time remaining,
  large back/play/forward controls, a shuffle/repeat/autoplay pill, and the
  name of the output device. Drag it down and it shrinks into a card
  before it closes.
- **Lyrics that rise into place.** Opening the lyrics slides them up on a
  soft spring and brings them into focus from a blur while the cover sinks
  back; closing them brings the cover forward again.
- **Synced lyrics** from [LRCLIB](https://lrclib.net) (word by word where
  it has the timing), KuGou (line-synced, strong on Asian music), NetEase
  Cloud Music (line-synced, a large catalogue), Unison (the community
  lyrics of [Better Lyrics](https://github.com/better-lyrics/better-lyrics),
  often word by word; entries its users voted down are skipped), a music
  video's own hand-made captions (timed to the video, as Better Lyrics
  finds them) and YouTube Music (unsynced), in an order you set, each
  credited under the lyrics. Plain
  text never ends the search: every source is asked until one has the
  song in time, a busy LRCLIB is asked again, and plain lyrics found while
  a source was down are looked up again a little later. Titles are cleaned
  before the lookup ("- 2009 Remaster", "(Deluxe Edition)", "feat." and
  video noise go), LRCLIB's exact match and its search run side by side,
  and a last search by title alone catches artists it spells another way,
  taking only a hit by the same artist. Four animation
  styles (Fluid, Karaoke, Focus zoom and Minimal), a per-song timing
  offset, adjustable text size, left or centred lines, an optional glow on
  the sung line, and lyrics saved with downloads so they work offline.
- **Lyric cards.** The quote button in the lyrics opens a sheet where you
  pick up to five lines and share them as a picture: over the blurred
  cover, in the cover's colour, or on a light page.
- **Lyrics while seeking.** Drag the seek bar and the line sung at that
  spot shows above your finger (Settings › Player).
- **One song menu everywhere:** round buttons up top for like, download,
  add to playlist and share, then play next, add to queue, start radio,
  view artist, open album, convert to the music video, and dislike (kept
  out of autoplay); plus upgrade quality, track details, sleep timer, lyrics
  offset and copy log in the player.
- **One player layout,** the Apple Music one: the cover runs to the edges
  of the player, the surfaces stay quiet and the controls are crisp white.
- **Quick screens.** Home opens on the last copy saved while the fresh one
  loads; pages you've visited and searches you've made come back at once
  and refresh in the background.
- **iOS-style bounce** at the ends of every list.
- **Smooth tab changes.** The dock's highlight stretches toward the tab
  you pick and settles into it, without overshooting.
- **Landscape player.** Turn the phone on its side and the cover fills
  the left; the right shows where the sound is going, the song, a slim
  time bar, the line being sung and the next ones in a serif, the
  controls with like and output buttons, and the volume, all over a
  blurred, dimmed cover. Lyrics and the queue open in the cover's place.
- **Share with a picture.** Sharing a song sends its link with a square
  image: the cover blurred into the background and, in the middle, the
  cover in a dark rounded frame with the artist's photo and name at the
  top, a share button and a heart, and the title, the time, a progress bar
  and the controls over its foot.
- **Player buttons.** Play sits in an accent pill that stretches wide
  while music plays, between skip buttons that tilt toward where they go.
  Play folds into pause and the skip arrows roll a step on with each tap.
- **Song change.** The cover dissolves from one blur into the other. It
  applies to the full-screen cover too.
- **A burst of hearts.** Liking a song in the player throws small hearts
  and sparks out in the cover's colours.
- **Lyrics in colour.** The cover's colours drift slowly behind the
  lyrics while they're open.
- **Blurred headers.** Album, artist and playlist pages have their cover
  blurred and stretched behind the top, fading into the page; artist photos
  stay sharp at the top and melt into the blur below.
- **What's new.** After an update, a short sheet lists what changed, once.
- **The cover behind the player,** blurred smooth and drifting slowly, so
  the colours of the song's own art sit under the controls.
- **A player you can feel.** Play swells into a deep thump, pause thumps
  and fades, next rushes forward into a kick, previous kicks and pulls
  back, a like beats twice like a heart, and dragging the seek bar ticks
  past each tenth of the song. These stay strong even on a light setting.
- **Haptic feedback you can tune**, from off to strong. Taps use the phone's
  crisp haptic primitives where it has them, and follow the system's touch
  feedback switch.
- **One dark look.** The app is dark throughout, on true black, with reduced
  motion and blur.

YouTube's audio is lossy, so there's no lossless or Dolby Atmos stream
option from YouTube. Lossless can come from your own music server, which
serves the files as they are stored. The optional JioSaavn route remains
lossy. Source availability varies.

OpenTune does not decrypt Deezer streams. Sharper album covers come from
Apple Music, Deezer or the Cover Art Archive.

Not built yet: lyric translation. The app speaks one language, English.

## Building

Open the repository in Android Studio, or build from the command line:

```
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-<abi>-debug.apk
./gradlew assembleRelease      # app/build/outputs/apk/release/app-<abi>-release.apk
./gradlew testDebugUnitTest    # unit tests
./gradlew lintDebug            # lint
```

Each build makes one APK per processor type (`arm64-v8a` for most phones,
`armeabi-v7a`, `x86_64`) and a `universal` one that runs anywhere. Release
builds go through R8, which only removes unused code: nothing is renamed,
so crash logs stay readable (see `app/proguard-rules.pro`).

You need JDK 17 or newer (CI uses 21) and an Android SDK with platform 37
(InnerTubeX is compiled against it; the app targets 36), set through
`ANDROID_HOME` or `local.properties`.

### CI and releases

GitHub Actions builds, tests and lints every push (`.github/workflows/ci.yml`)
and keeps the release APKs as an artifact. Pushing a tag such as `v0.2.0`
builds signed APKs and publishes them as a GitHub release
(`.github/workflows/release.yml`). Release signing uses the repository
secrets `OPENTUNE_KEYSTORE_BASE64`, `OPENTUNE_KEYSTORE_PASSWORD`,
`OPENTUNE_KEY_ALIAS` and `OPENTUNE_KEY_PASSWORD` when they're set, and the
debug key otherwise. Set the secrets before the first release: a release
signed with a different key cannot replace the copy already on the phone,
and a CI runner's debug key changes from run to run.

## Project layout

```
core/src/main/java/com/grayguava/opentune/lite/core/
  data/innertube/   Innertube client and parser, StreamResolver (engine switch),
                    InnerTubeXResolver, PoToken WebView
  data/             account, downloads, library, history, lyrics, loudness, settings
  playback/         PlaybackService (ExoPlayer, cache, loudness, upgrade, sleep timer),
                    Crossfade, DSP
app/src/main/java/com/grayguava/opentune/lite/
  ui/               Compose screens: home, explore, search, library, player, settings
```

## Disclaimer

The screenshots and video show real albums, artists and songs to show the
app as it's used. Cover art, names and recordings belong to their owners.
OpenTune isn't affiliated with or endorsed by YouTube, Google, Spotify or
any artist or label shown.

## License

OpenTune is licensed under the GNU General Public License v3.0. See
[LICENSE](LICENSE). Code, libraries, fonts and data from other projects
are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md), as their
licences require.
