# Third-party notices

OpenTune is licensed under the GNU General Public License v3.0 (see
[LICENSE](LICENSE)). It includes or uses the following code, libraries,
fonts and data, under their own licences.

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
frosted glass uses [Haze](https://github.com/chrisbanes/haze). Album covers
come from the [Cover Art Archive](https://coverartarchive.org) through
[MusicBrainz](https://musicbrainz.org), both run by the MetaBrainz
Foundation. Headphone corrections come from
[AutoEq](https://github.com/jaakkopasanen/AutoEq) by Jaakko Pasanen (MIT).
Skip segments come from [SponsorBlock](https://sponsor.ajay.app) (data
under CC BY-NC-SA 4.0), and radio stations from the
[Radio Browser](https://www.radio-browser.info) community directory.

## Audio compatibility fixes

`FlacInfo` adapts BitChord's Samsung FLAC decoder exclusion and
STREAMINFO measurement approach from `AudioOutputPolicy.kt` and
`PlaybackService.kt` at commit
`edc1e6f36febf71a118e649411d2fc21b70ead45` of
[kushagrasinghx/BitChord](https://github.com/kushagrasinghx/BitChord),
licensed GPL-3.0. It excludes affected Samsung vendor
FLAC decoders and reads actual FLAC metadata rather than treating float PCM
as proof of a hi-res master.
