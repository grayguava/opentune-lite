# Third-party notices

OpenTune is licensed under the GNU General Public License v3.0 (see
[LICENSE](LICENSE)). It includes or uses the following code, libraries,
fonts and data, under their own licences.

The optional Echo motion effects are adapted from [Echo Music](https://github.com/EchoMusicApp/Echo-Music/tree/a31a72bd958f7cdc128db17d2ea6ae90c3489aa1),
by the Echo Music contributors, under GPLv3. The sources are
`ui/player/MiniPlayer.kt` (animated glow and wavy play button) and
`ui/component/BottomSheet.kt` (soft spring), under
`app/src/main/kotlin/com/music/echo/` at that revision.
OpenTune adapts these in `ui/player/EchoMotion.kt`, `PlayerButtons.kt`
and `ui/PageMotion.kt`, with its own palette, pause/lifecycle
handling and reduced-motion behavior. See [Echo motion](docs/echo-motion.md).

The Innertube client, response parser and YouTube stream resolver under
`data/innertube/` are adapted from
[BitChord](https://github.com/kushagrasinghx/BitChord), used under its
GPLv3 license. The InnerTubeX glue and the PoToken WebView
(`data/innertube/InnerTubeXResolver.kt`, `data/innertube/potoken/`,
`assets/po_token.html`) also come from BitChord; the PoToken code follows
NewPipe's design. [InnerTubeX](https://github.com/MetrolistGroup/innertubex)
by MetrolistGroup and [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor)
are used as libraries under GPLv3. Liquid Glass uses Kyant's
[backdrop](https://github.com/Kyant0/AndroidLiquidGlass) library, and
frosted glass uses [Haze](https://github.com/chrisbanes/haze). The
[Outfit](https://github.com/Outfitio/Outfit-Fonts) font is used under the
SIL Open Font License 1.1 (`third_party/outfit/OFL.txt`). Album covers
come from the [Cover Art Archive](https://coverartarchive.org) through
[MusicBrainz](https://musicbrainz.org), and recommendations from
[ListenBrainz](https://listenbrainz.org), all run by the MetaBrainz
Foundation. Headphone corrections come from
[AutoEq](https://github.com/jaakkopasanen/AutoEq) by Jaakko Pasanen (MIT).
Skip segments come from [SponsorBlock](https://sponsor.ajay.app) (data
under CC BY-NC-SA 4.0), and radio stations from the
[Radio Browser](https://www.radio-browser.info) community directory.
The QR code on a Listen together invite is made with
[ZXing](https://github.com/zxing/zxing) (`com.google.zxing:core`), by the
ZXing authors, under the Apache License 2.0.

## Audio compatibility fixes

`FlacInfo` adapts BitChord's Samsung FLAC decoder exclusion and
STREAMINFO measurement approach from `AudioOutputPolicy.kt` and
`PlaybackService.kt` at commit
`edc1e6f36febf71a118e649411d2fc21b70ead45` of
[kushagrasinghx/BitChord](https://github.com/kushagrasinghx/BitChord),
licensed GPL-3.0. It excludes affected Samsung vendor
FLAC decoders and reads actual FLAC metadata rather than treating float PCM
as proof of a hi-res master.
