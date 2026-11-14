package com.grayguava.opentune.lite.data.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Stream quality ceilings [StreamResolver][com.grayguava.opentune.lite.data.innertube.StreamResolver]
 * ranks YouTube's formats against. YouTube's audio is lossy: Opus at about
 * 130-160 kbps is the best most accounts get, AAC 256 only with Premium.
 */
@Serializable
enum class AudioQuality(val label: String, val summary: String, val maxKbps: Int) {
    LOW("Low", "About 64 kbps", 64),
    NORMAL("Normal", "About 128 kbps", 128),
    HIGH("High", "Opus up to 160 kbps", 160),
    MAX("Best", "The best YouTube gives you, AAC 256 with Premium", 256),
}

/** Reverb presets map onto android.media.audiofx.PresetReverb's. */
@Serializable
enum class ReverbLevel(val label: String, val preset: Short) {
    OFF("Off", 0),
    SMALL_ROOM("Small room", 1),
    MEDIUM_ROOM("Room", 2),
    LARGE_ROOM("Large room", 3),
    MEDIUM_HALL("Hall", 4),
    LARGE_HALL("Large hall", 5),
    PLATE("Plate", 6),
}

@Serializable
data class SoundSettings(
    val speed: Float = 1f,
    val pitch: Float = 1f,
    val reverb: ReverbLevel = ReverbLevel.OFF,
    /** 0..1000; drives the low-shelf in [com.grayguava.opentune.lite.playback.dsp.AudioDsp]. */
    val bassBoost: Int = 0,
    /** The sound circling around the listener's head. */
    val eightD: EightD = EightD.OFF,
    /** The sound travelling all round the listener, above and below too; takes over from [eightD]. */
    val space: Space3DSpeed = Space3DSpeed.OFF,
) {
    val isDefault: Boolean get() = this == SoundSettings()
}

/** How fast 8D audio circles: once around in this many seconds. */
@Serializable
enum class EightD(val label: String, val periodSeconds: Float) {
    OFF("Off", 0f),
    SLOW("Slow", 16f),
    MEDIUM("Medium", 10f),
    FAST("Fast", 6f),
}

/** How fast 3D sound goes round: once in this many seconds, tipping over and under every few turns. */
@Serializable
enum class Space3DSpeed(val label: String, val periodSeconds: Float) {
    OFF("Off", 0f),
    SLOW("Slow", 14f),
    MEDIUM("Medium", 9f),
    FAST("Fast", 5f),
}

/** One-tap remixes. Speed and pitch move together, as on a turntable. */
enum class RemixPreset(val label: String, val sound: SoundSettings) {
    NORMAL("Normal", SoundSettings()),
    SLOWED_REVERB("Slowed + reverb", SoundSettings(0.82f, 0.82f, ReverbLevel.LARGE_HALL)),
    NIGHTCORE("Nightcore", SoundSettings(1.25f, 1.25f)),
    SPED_UP("Sped up", SoundSettings(1.15f, 1.15f)),
    DAYCORE("Daycore", SoundSettings(0.9f, 0.9f, ReverbLevel.MEDIUM_ROOM)),
    VAPORWAVE("Vaporwave", SoundSettings(0.75f, 0.75f, ReverbLevel.PLATE)),
    BASS_BOOSTED("Bass boosted", SoundSettings(bassBoost = 800)),
    EIGHT_D("8D", SoundSettings(eightD = EightD.MEDIUM)),
    SPACE_3D("3D", SoundSettings(space = Space3DSpeed.MEDIUM)),
    ;

    companion object {
        fun matching(sound: SoundSettings): RemixPreset? = entries.firstOrNull { it.sound == sound }
    }
}

/** Band centres for the 7-band equalizer, in Hz. */
/** Band centres for the 15-band equalizer: the ISO 2/3-octave series, in Hz. */
val EQ_BANDS_HZ = listOf(25f, 40f, 63f, 100f, 160f, 250f, 400f, 630f, 1_000f, 1_600f, 2_500f, 4_000f, 6_300f, 10_000f, 16_000f)

/** The seven bands older versions saved, kept to carry those curves over. */
private val SEVEN_BAND_HZ = listOf(60f, 150f, 400f, 1_000f, 2_400f, 6_000f, 15_000f)

/**
 * [gains] at [from] frequencies, redrawn at [EQ_BANDS_HZ] by interpolating
 * on a log-frequency axis, flat beyond the ends.
 */
fun resampleBands(gains: List<Float>, from: List<Float> = SEVEN_BAND_HZ): List<Float> {
    if (gains.size == EQ_BANDS_HZ.size && from.size != gains.size) return gains
    val points = from.zip(gains).sortedBy { it.first }
    if (points.isEmpty()) return List(EQ_BANDS_HZ.size) { 0f }
    return EQ_BANDS_HZ.map { f ->
        val x = kotlin.math.ln(f)
        when {
            f <= points.first().first -> points.first().second
            f >= points.last().first -> points.last().second
            else -> {
                val hi = points.indexOfFirst { it.first >= f }
                val (f0, g0) = points[hi - 1]
                val (f1, g1) = points[hi]
                val t = (x - kotlin.math.ln(f0)) / (kotlin.math.ln(f1) - kotlin.math.ln(f0))
                (g0 + (g1 - g0) * t).let { (it * 10).toInt() / 10f }
            }
        }
    }
}

@Serializable
data class EqualizerSettings(
    val enabled: Boolean = false,
    /** Gain per band in dB, -12..12, one per [EQ_BANDS_HZ]. */
    val bands: List<Float> = List(15) { 0f },
    val preampDb: Float = 0f,
    /** Tone controls: shelves at 120 Hz and 8 kHz, dB. */
    val bassDb: Float = 0f,
    val trebleDb: Float = 0f,
    /** -1 (left only) .. 1 (right only). */
    val balance: Float = 0f,
    /**
     * A correction for the listener's headphones from AutoEq. Runs on its own,
     * whether or not the equalizer above is on.
     */
    val headphone: HeadphoneEq? = null,
)

/** One AutoEq parametric profile: its preamp and filters, as published. */
@Serializable
data class HeadphoneEq(
    val name: String,
    /** Who measured it, e.g. "oratory1990". */
    val source: String,
    val preampDb: Float,
    val filters: List<ParametricFilter>,
)

@Serializable
data class ParametricFilter(val type: FilterType, val freqHz: Float, val gainDb: Float, val q: Float)

@Serializable
enum class FilterType { PEAK, LOW_SHELF, HIGH_SHELF }

/** Where a preset sits on the equalizer page. */
enum class EqGroup(val label: String) { SIGNATURE("Signature"), SITUATION("For the moment"), GENRE("Genres") }

/**
 * Curves for the 15 bands. Gentle on purpose (no more than about 6 dB): the
 * equalizer takes the biggest boost back off the input, so a steep curve
 * mostly makes everything quieter. Matched by its bands.
 */
enum class EqPreset(val label: String, val bands: List<Float>, val summary: String = "", val group: EqGroup = EqGroup.GENRE) {
    FLAT("Flat", List(15) { 0f }, "The song as mastered", EqGroup.SIGNATURE),
    /** Deep, clean lows and open highs. */
    STUDIO("Studio", listOf(2.6f, 2.8f, 2.2f, 0.6f, -1.8f, -2.6f, -1.2f, 0f, 1.2f, 2.4f, 3.6f, 4.0f, 4.2f, 4.5f, 4.8f), "Firm lows, clear mids, open top", EqGroup.SIGNATURE),
    HIFI("Hi-Fi", listOf(2.5f, 2.2f, 1.6f, 0.8f, 0f, -0.6f, -0.8f, -0.4f, 0f, 0.4f, 1f, 1.6f, 2f, 2.4f, 2.6f), "A gentle smile: a touch more weight and sparkle", EqGroup.SIGNATURE),
    WARM("Warm", resampleBands(listOf(3f, 2f, 1f, 0f, -1f, -2f, -2f)), "Rounder, softer highs", EqGroup.SIGNATURE),
    BRIGHT("Bright", resampleBands(listOf(-1f, 0f, 0f, 1f, 2f, 4f, 5f)), "More detail and presence", EqGroup.SIGNATURE),
    TREBLE_AIR("Air", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.5f, 1f, 2f, 3f, 4.5f, 5.5f), "Shimmer above 10 kHz, nothing else touched", EqGroup.SIGNATURE),
    LATE_NIGHT("Late night", listOf(5f, 4.5f, 3.5f, 2f, 0.5f, -0.5f, -1f, -1f, -0.5f, 0f, 0.5f, 1.5f, 2.5f, 3f, 3f), "Full sound at low volume, as ears hear less bass and treble when it's quiet", EqGroup.SITUATION),
    LOUDNESS("Loudness", resampleBands(listOf(5f, 3f, 0f, -1f, 0f, 3f, 4f)), "Punchy lows and highs for noisy places", EqGroup.SITUATION),
    PHONE_SPEAKER("Phone speaker", listOf(-6f, -6f, -3f, 1f, 2.5f, 2f, 1f, 0f, 0f, 1f, 2f, 2f, 1f, 0.5f, 0f), "Leaves out bass a small speaker can't play, so it doesn't rattle", EqGroup.SITUATION),
    CAR("Car", listOf(3f, 3f, 2.5f, 1f, 0f, -1f, -1f, 0f, 0.5f, 1f, 1.5f, 2f, 2f, 1.5f, 1f), "Lows and clarity that carry over road noise", EqGroup.SITUATION),
    SPEECH("Speech", listOf(-6f, -5f, -3f, -1f, 0f, 0.5f, 1f, 1.5f, 2f, 3f, 3f, 2f, 0f, -1f, -2f), "Podcasts and talk: voices forward, rumble gone", EqGroup.SITUATION),
    VOCAL_CLARITY("Vocal clarity", listOf(-1.5f, -1.5f, -1f, -0.5f, 0f, -0.5f, 0f, 1f, 2f, 3f, 3f, 2f, 1f, 0.5f, 0f), "Singers step out of the mix", EqGroup.SITUATION),
    BASS("Bass", resampleBands(listOf(6f, 4f, 1f, 0f, 0f, 0f, 0f)), "More low end", EqGroup.GENRE),
    DEEP_BASS("Deep bass", listOf(6f, 5.5f, 4.5f, 3f, 1.5f, 0f, -1f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), "Sub-bass you feel", EqGroup.GENRE),
    HIPHOP("Hip-hop", listOf(5f, 5f, 4f, 2.5f, 0.5f, -1f, -1.5f, -1f, 0f, 1f, 1.5f, 1f, 1.5f, 2f, 2f), "Heavy kick, clear vocals", EqGroup.GENRE),
    VOCAL("Vocal", resampleBands(listOf(-2f, -1f, 1f, 3f, 3f, 1f, 0f)), "Warm, forward voices", EqGroup.GENRE),
    ROCK("Rock", listOf(4f, 4f, 3.5f, 2.5f, 1f, -0.5f, -1.5f, -1.5f, -0.5f, 1f, 2.5f, 3.5f, 4f, 4f, 4f), "Big drums, bite in the guitars", EqGroup.GENRE),
    POP("Pop", listOf(-1f, -0.5f, 0.5f, 1.5f, 2.5f, 3f, 2.5f, 1.5f, 0.5f, 0f, -0.5f, -0.5f, 0f, 0.5f, 1f), "Present, polished mids", EqGroup.GENRE),
    ELECTRONIC("Electronic", resampleBands(listOf(5f, 3f, 0f, -2f, 1f, 3f, 4f)), "Deep drops, crisp hats", EqGroup.GENRE),
    ACOUSTIC("Acoustic", resampleBands(listOf(3f, 2f, 1f, 1f, 2f, 2f, 1f)), "Natural body and string detail", EqGroup.GENRE),
    CLASSICAL("Classical", listOf(3f, 3f, 2.5f, 2f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, -1f, -1.5f, -2f, -2.5f), "A hall's warmth, gentle highs", EqGroup.GENRE),
}

@Serializable
data class PlaybackSettings(
    /** The sleep timer fades the music out over its last minutes instead of stopping it dead. */
    val sleepWindDown: Boolean = true,
    val wifiQuality: AudioQuality = AudioQuality.MAX,
    val mobileQuality: AudioQuality = AudioQuality.MAX,
    /**
     * Carry the signal to the output as 32-bit float. With float output on, the
     * decoder is asked for high-res PCM rather than 16-bit (Android 12+ FLAC
     * decoders oblige), so a 24-bit source keeps its precision to the mixer
     * instead of being cut to 16-bit at the decoder. On by default for
     * end-to-end hi-res; a device whose mixer dislikes float can turn it off and
     * fall back to dithered 16-bit.
     */
    val floatOutput: Boolean = true,
    val preferUsbDac: Boolean = true,
    /** Slowly level every track toward the same loudness. */
    val loudnessNormalization: Boolean = true,
    /**
     * Level songs on the phone's own speaker too. Off by default: there it
     * mostly turns loud masters down, and a small speaker needs the level.
     */
    val normalizeOnSpeaker: Boolean = false,
    /** Where normalization sets songs: below, at or above YouTube's reference level. */
    val volumeLevel: VolumeLevel = VolumeLevel.LOUD,
    val skipSilence: Boolean = false,
    /** Mid/side stereo widening. */
    val spatialAudio: Boolean = false,
    /** The "Clarity" tone curve in the app's DSP. */
    val clarity: Boolean = false,
    val autoplay: Boolean = true,
    /** Autoplay won't add anything already played or queued this session. */
    val noRepeatInSession: Boolean = false,
    /** Whose suggestions autoplay and Home's "Because you played" follow. */
    val recommender: Recommender = Recommender.YOUTUBE,
    val listeningHistoryTarget: com.grayguava.opentune.lite.data.history.ListeningHistoryTarget = com.grayguava.opentune.lite.data.history.ListeningHistoryTarget.YOUTUBE_MUSIC,
    val pauseListeningHistory: Boolean = false,
    /**
     * Smart shuffle can be chosen: the player's shuffle button gets a third
     * state, and a long press on a playlist or album's Shuffle starts it.
     * Plain shuffle never mixes anything in, so this is on by default; off
     * takes the extra state and the long press away.
     */
    val smartShuffle: Boolean = true,
    val stopOnTaskRemoved: Boolean = true,
    /** Swiping a song row plays it next (toward the end of the line) or adds it to the queue (toward the start). */
    val swipeToQueue: Boolean = true,
    /** Buffer the next track and resolve its neighbours early, for instant skips. */
    val preloadUpcoming: Boolean = true,
    /** A downloaded song streams in its best quality while there's internet; its file plays offline. */
    val streamDownloadsOnline: Boolean = true,
    /** Swap to a clearly better stream mid-song when one turns up. */
    val qualityUpgrade: Boolean = true,
    /** Overlap the end of a song with the start of the next; 0 is off. */
    val crossfadeSeconds: Int = 0,
    /** Bit-perfect output to a USB DAC (Android 14+): no mixing, effects or resampling. */
    val bitPerfectUsb: Boolean = false,
    /** Let the phone's own effects (Dolby, SoundAlive, system EQ) process playback. */
    val systemEffects: Boolean = true,
    /** Carry on playing when headphones are plugged in or a Bluetooth device connects. */
    val resumeOnConnect: Boolean = false,
    /** Pause when the media volume is turned all the way down, and resume when it comes back up. */
    val pauseAtZeroVolume: Boolean = false,
    /** Skip the non-music parts of music videos, from SponsorBlock. */
    val sponsorBlock: Boolean = true,
    /** SponsorBlock categories to skip, by API name. */
    val sponsorBlockCategories: Set<String> = DEFAULT_SPONSORBLOCK_CATEGORIES,
)

val DEFAULT_SPONSORBLOCK_CATEGORIES = setOf("music_offtopic", "sponsor", "selfpromo", "interaction")

@Serializable
data class InterfaceSettings(
    val playlistGrid: Boolean = false,
    /** Playlists on Library as a list down the page, grouped by where they're from, rather than a row of tiles. */
    val playlistList: Boolean = false,
    /** Playlists pinned to the top of Library, first pinned first: a local playlist's id, or a YouTube Music browse id. */
    val pinnedPlaylists: List<String> = emptyList(),
    val reduceAnimation: Boolean = false,
    /** Solid fills instead of frosted glass. */
    val reduceBlur: Boolean = false,
    val hideVolumeBar: Boolean = false,
    val hideSongStatus: Boolean = false,
    /** Show the current lyric above the controls, independently of the player layout. */
    val lyricsPreview: Boolean = true,
    val syncedLyrics: Boolean = true,
    val blurLyrics: Boolean = true,
    /** On the lyrics view, the controls slide away after a few seconds untouched, leaving the lyrics. */
    val lyricsFocus: Boolean = true,
    val statsForNerds: Boolean = false,
    val recentsAsGrid: Boolean = false,
    /** Speed dial at the top of Home: the most-played songs, albums and artists. */
    val speedDial: Boolean = true,
    /** Apple's Liquid Glass on Android 13+; the app's smoke glass otherwise. */
    val liquidGlass: Boolean = false,
    /** How thick the seek bar's track is. */
    val seekbarThickness: SeekbarThickness = SeekbarThickness.REGULAR,
    /** What marks the place on the seek bar. */
    val seekbarThumb: SeekbarThumb = SeekbarThumb.DOT,
    /** What the time on the right of the seek bar shows. */
    val seekbarTime: SeekbarTime = SeekbarTime.LEFT,
    /** The audio details page: cards of format, routing and source, or the live pipeline. */
    val signalPathStyle: SignalPathStyle = SignalPathStyle.DETAILS,
    /** Small dots on the seek bar where the song's parts (verse, chorus) begin. */
    val seekbarMarks: Boolean = true,
    /** A "Skip to chorus" button beside the source pill, when the lyrics mark the chorus. */
    val skipToChorus: Boolean = false,
    /** The pill under the title naming where the audio comes from and its quality. */
    val sourcePill: Boolean = true,
    /** Double-tapping the cover's left or right side goes back or on ten seconds. */
    val doubleTapSeek: Boolean = true,
    /** While the seek bar is dragged, the lyric line at that point shows above the finger. */
    val lyricScrubPreview: Boolean = true,
    /** Sharp album covers from Apple Music, Deezer or the Cover Art Archive in the player. */
    val albumCovers: Boolean = true,
    /** Apple Music's animated album covers loop, muted, in the player's cover. */
    val animatedCovers: Boolean = true,
    /** How sharp Apple's animated covers play; sharper uses more data. */
    val motionQuality: MotionQuality = MotionQuality.MEDIUM,
    /** Animated covers only load on Wi-Fi or another unmetered network. */
    val animatedCoversWifiOnly: Boolean = true,
    val dockMotion: DockMotion = DockMotion.MINIMIZE,
    /** The cover drifts and zooms slowly in the player while a song plays. */
    val movingCover: Boolean = true,
    /** One or two of Home's song and album cards drift and zoom the same way while the page sits still. */
    val homeMovingCovers: Boolean = true,
    /** How strong taps and buzzes are, 0 (off) to 1. */
    val hapticStrength: Float = 0.6f,
    /** Pages blur and fade out as they scroll under the status bar. */
    val frostedTopEdge: Boolean = true,
    /** High performance mode: ask for a fast refresh rate while the app is open. */
    val highRefreshRate: Boolean = true,
    /** The rate high performance mode asks for, in Hz; 0 for the screen's fastest. */
    val refreshRateHz: Int = 0,
    /** The first-run welcome: whether it's still to be shown. See [com.grayguava.opentune.lite.data.Welcome]. */
    val welcome: WelcomeState = WelcomeState.UNDECIDED,
    /** On wide windows (tablets, unfolded phones, phones on their side): the tabs in a rail and pages in a readable column. */
    val wideLayout: Boolean = true,
    /** Explore has its own tab; off, it leaves the dock and fills the Search page until a search starts. */
    val separateExplore: Boolean = true,
    /** On large screens, the player stays docked beside the page while a song is loaded. */
    val sidePlayer: Boolean = true,
    /** Your top songs play behind the Wrapped story; off is its mute button, kept for next time. */
    val wrappedMusic: Boolean = true,
)

/**
 * Where the first-run welcome stands. Settings saved before it existed read
 * back as [UNDECIDED], like a fresh install's, and the first start after
 * that decides once which of the other two it is.
 */
@Serializable
enum class WelcomeState {
    UNDECIDED,
    /** To be shown: a fresh install, or "Show the welcome again". */
    DUE,
    /** Seen, skipped, or not needed because the app was already in use. */
    SEEN,
}

@Serializable
data class LibrarySettings(
    /** A MediaStore relative path ("Music/"), or null for every folder. */
    val localFolder: String? = null,
    val filterNonMusic: Boolean = true,
    val songCacheMb: Int = 512,
    val downloadQuality: AudioQuality = AudioQuality.MAX,
    val downloadWifiOnly: Boolean = true,
    /** Also copy each download into the phone's Music folder, in a format other apps play; see [com.grayguava.opentune.lite.data.download.DownloadExport]. */
    val exportDownloads: Boolean = false,
    /** Leave out songs and albums marked explicit; see [com.grayguava.opentune.lite.data.ContentFilter]. */
    val hideExplicit: Boolean = false,
    /** Notify about new albums and singles from followed artists. */
    val releaseAlerts: Boolean = true,
)

/**
 * The level loudness normalization aims for, against YouTube's reference
 * (about −14 LUFS). [offsetDb] moves every song by the same amount; [maxGainDb]
 * caps how far a quiet song is lifted. Lifts go through a limiter, so Loud
 * doesn't clip.
 */
@Serializable
enum class VolumeLevel(val label: String, val summary: String, val offsetDb: Float, val maxGainDb: Float) {
    QUIET("Quiet", "About 5 dB under YouTube's level, for quiet rooms and long listens", -5f, 3f),
    NORMAL("Normal", "YouTube's own reference level", 0f, 3f),
    LOUD("Loud", "About 5 dB over YouTube's level, held by a limiter so peaks don't clip", 5f, 9f),
}

/** Whose recommendations pick the songs autoplay adds. Every song still plays from YouTube Music. */
@Serializable(with = RecommenderSerializer::class)
enum class Recommender(val label: String, val summary: String) {
    YOUTUBE("YouTube Music", "YouTube Music's own radio for the last song"),
    JIOSAAVN("JioSaavn", "JioSaavn's picks after the last song, strong on Indian music, found on YouTube Music"),
}

/**
 * Reads a saved choice that a later version dropped as the default, so a
 * setting left over from an earlier build doesn't reset the rest.
 */
object RecommenderSerializer : kotlinx.serialization.KSerializer<Recommender> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
        "Recommender", kotlinx.serialization.descriptors.PrimitiveKind.STRING,
    )
    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Recommender =
        runCatching { Recommender.valueOf(decoder.decodeString()) }.getOrDefault(Recommender.YOUTUBE)
    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Recommender) =
        encoder.encodeString(value.name)
}

/**
 * How the dock tucks away while a page scrolls down, and comes back. Each
 * ends in a different shape and gets there a different way. A saved choice
 * that has since been dropped reads back as [MINIMIZE], the default.
 */
@Serializable
enum class DockMotion(val label: String, val summary: String) {
    MINIMIZE("Minimize", "Like iOS 26: the tabs shrink to the open one, with the song in a slim bar beside it"),
    CAPSULE("Capsule", "The tabs and the song gather into one small capsule in the middle"),
    SINK("Sink", "The tabs sink away into a blur and the song settles into their place"),
}

/** How sharp an animated cover plays: the largest picture and the most data a second it may use. */
@Serializable
enum class MotionQuality(val label: String, val maxPx: Int, val maxBitrate: Int) {
    LOW("Low", 768, 1_500_000),
    MEDIUM("Medium", 1080, 4_000_000),
    // Apple's sharpest are 2160 px square (up to about 15 Mbps) and 2048 × 2732 tall.
    HIGH("High", 2732, 16_000_000),
}

/** How synced lyrics move as the song plays. */
@Serializable
enum class SeekbarThickness(val label: String, val dp: Float) {
    THIN("Thin", 3f),
    REGULAR("Regular", 5f),
    THICK("Thick", 9f),
}

enum class SeekbarThumb(val label: String) {
    DOT("Dot"),
    LINE("Line"),
    NONE("None"),
}

/** How the audio details from the quality label are laid out. */
enum class SignalPathStyle(val label: String) {
    DETAILS("Format details"),
    TIMELINE("Pipeline"),
}

enum class SeekbarTime(val label: String) {
    LEFT("Time left"),
    TOTAL("Length"),
    HIDDEN("Hidden"),
}

/** Where lyrics can come from. */
@Serializable
enum class LyricsSource(val label: String, val summary: String) {
    LRC_RED("lrc.red", "Word-by-word lyrics filed by recording, as BitChord uses them first"),
    PAXSENIX("Paxsenix", "Apple Music's word-by-word lyrics, with duets and backing vocals"),
    LRCLIB("LRCLIB", "Community lyrics, line by line and sometimes word by word"),
    LYRICSPLUS("LyricsPlus", "Word-by-word lyrics from the servers behind YouLyPlus, as Echo Music uses them"),
    BETTERLYRICS("Better Lyrics", "Word-by-word lyrics from Better Lyrics, for songs its listeners have already looked up"),
    SIMPMUSIC("SimpMusic", "Community lyrics for YouTube songs, line by line and word by word"),
    KUGOU("KuGou", "Line-synced lyrics from KuGou's large catalogue, strong on Asian music"),
    NETEASE("NetEase", "Line-synced lyrics from NetEase Cloud Music, a large catalogue of every kind of music"),
    UNISON("Unison", "Better Lyrics' community lyrics, written and timed by its users, often word by word"),
    YOUTUBE_CAPTIONS("YouTube captions", "A music video's own hand-made subtitles, timed line by line to the video"),
    YOUTUBE_MUSIC("YouTube Music", "YouTube Music's own lyrics, as plain text"),
}

@Serializable
data class LyricsSourceEntry(val source: LyricsSource, val enabled: Boolean = true)

@Serializable
data class LyricsSettings(
    /** Tried in this order; the first with lyrics wins unless [preferWordSynced]. */
    val sources: List<LyricsSourceEntry> = DEFAULT_LYRICS_SOURCES,
    /** Keep looking past a line-synced match for word-by-word lyrics. */
    val preferWordSynced: Boolean = true,
    /** Per-song timing shift in ms, set from the player's "Lyrics offset". */
    val offsets: Map<String, Long> = emptyMap(),
    /** A line in Latin letters under each one in another script. */
    val romanize: Boolean = true,
    /** Per-song source picked from the player's "Lyrics source"; the others aren't asked first. */
    val chosen: Map<String, LyricsSource> = emptyMap(),
) {
    /**
     * The saved order, with any source added since appended at the end; lrc.red
     * and Paxsenix, the most exact of them, go in first when a saved order
     * hasn't met them yet.
     */
    val ordered: List<LyricsSourceEntry>
        get() {
            val missing = LyricsSource.entries.filter { s -> sources.none { it.source == s } }.map(::LyricsSourceEntry)
            val (front, back) = missing.partition { it.source == LyricsSource.LRC_RED || it.source == LyricsSource.PAXSENIX }
            return front + sources.distinctBy { it.source } + back
        }
}

val DEFAULT_LYRICS_SOURCES = LyricsSource.entries.map(::LyricsSourceEntry)

/** Everything persisted, as one document: what's stored, exported and imported. */
@Serializable
data class SettingsState(
    val sound: SoundSettings = SoundSettings(),
    val equalizer: EqualizerSettings = EqualizerSettings(),
    val playback: PlaybackSettings = PlaybackSettings(),
    val ui: InterfaceSettings = InterfaceSettings(),
    val library: LibrarySettings = LibrarySettings(),
    val lyrics: LyricsSettings = LyricsSettings(),
    val recentSearches: List<String> = emptyList(),
)

/** The colour the app's own scheme starts from when nothing else supplies one. */
val SEED_COLOR = 0xFF039BE5.toInt()

/**
 * App-wide settings, stored as one JSON document in SharedPreferences and
 * exposed as StateFlows so the UI and the playback service (same process)
 * both react to changes. [init] runs from the Application.
 */
object AppSettings {
    private var prefs: SharedPreferences? = null
    private var connectivity: ConnectivityManager? = null

    // coerceInputValues: a choice a later version dropped reads back as that setting's default
    // instead of failing the whole file.
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; coerceInputValues = true }

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _sound = MutableStateFlow(SoundSettings())
    val sound: StateFlow<SoundSettings> = _sound.asStateFlow()
    private val _equalizer = MutableStateFlow(EqualizerSettings())
    val equalizer: StateFlow<EqualizerSettings> = _equalizer.asStateFlow()
    private val _playback = MutableStateFlow(PlaybackSettings())
    val playback: StateFlow<PlaybackSettings> = _playback.asStateFlow()
    private val _ui = MutableStateFlow(InterfaceSettings())
    val ui: StateFlow<InterfaceSettings> = _ui.asStateFlow()
    private val _library = MutableStateFlow(LibrarySettings())
    val library: StateFlow<LibrarySettings> = _library.asStateFlow()
    private val _lyrics = MutableStateFlow(LyricsSettings())
    val lyrics: StateFlow<LyricsSettings> = _lyrics.asStateFlow()
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    /** Read by StreamResolver on every resolve: the ceiling for the network in use. */
    val effectiveAudioQuality: AudioQuality
        get() = _playback.value.let { if (onMeteredNetwork()) it.mobileQuality else it.wifiQuality }

    /** Whether the phone has a working internet connection right now. */
    fun isOnline(): Boolean {
        val cm = connectivity ?: return true
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun onMeteredNetwork(): Boolean {
        val cm = connectivity ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }

    fun init(context: Context) {
        val p = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs = p
        connectivity = context.getSystemService(ConnectivityManager::class.java)
        val stored = p.getString(K_STATE, null)?.let { runCatching { json.decodeFromString<SettingsState>(it) }.getOrNull() }
        publish(stored ?: migrateLegacy(p))
    }

    private fun publish(stored: SettingsState) {
        // Curves saved by the seven-band equalizer are redrawn on fifteen bands.
        val s = if (stored.equalizer.bands.size == EQ_BANDS_HZ.size) stored
        else stored.copy(equalizer = stored.equalizer.copy(bands = resampleBands(stored.equalizer.bands)))
        _state.value = s
        _sound.value = s.sound
        _equalizer.value = s.equalizer
        _playback.value = s.playback
        _ui.value = s.ui
        _library.value = s.library
        _lyrics.value = s.lyrics
        _recentSearches.value = s.recentSearches
    }

    private fun update(transform: (SettingsState) -> SettingsState) {
        val s = transform(_state.value)
        publish(s)
        prefs?.edit { putString(K_STATE, json.encodeToString(SettingsState.serializer(), s)) }
    }

    fun updateSound(t: (SoundSettings) -> SoundSettings) = update { it.copy(sound = t(it.sound)) }
    fun updateEqualizer(t: (EqualizerSettings) -> EqualizerSettings) = update { it.copy(equalizer = t(it.equalizer)) }
    fun updatePlayback(t: (PlaybackSettings) -> PlaybackSettings) = update { it.copy(playback = t(it.playback)) }
    fun updateUi(t: (InterfaceSettings) -> InterfaceSettings) = update { it.copy(ui = t(it.ui)) }
    fun updateLibrary(t: (LibrarySettings) -> LibrarySettings) = update { it.copy(library = t(it.library)) }
    fun updateLyrics(t: (LyricsSettings) -> LyricsSettings) = update { it.copy(lyrics = t(it.lyrics)) }

    /** Sets [videoId]'s lyrics offset; 0 clears it, unless [keepZero] (a 0 that overrides the automatic shift). */
    fun setLyricsOffset(videoId: String, ms: Long, keepZero: Boolean = false) = updateLyrics { l ->
        l.copy(offsets = if (ms == 0L && !keepZero) l.offsets - videoId else (l.offsets + (videoId to ms)).entries.toList().takeLast(500).associate { it.toPair() })
    }

    /** Pins [videoId]'s lyrics to [source]; null goes back to the usual order. */
    fun setLyricsSource(videoId: String, source: LyricsSource?) = updateLyrics { l ->
        l.copy(chosen = if (source == null) l.chosen - videoId else (l.chosen - videoId + (videoId to source)).entries.toList().takeLast(500).associate { it.toPair() })
    }

    fun setAutoplay(enabled: Boolean) = updatePlayback { it.copy(autoplay = enabled) }

    fun addRecentSearch(query: String) = update { s ->
        s.copy(recentSearches = (listOf(query) + s.recentSearches.filterNot { it.equals(query, true) }).take(MAX_RECENT))
    }

    fun removeRecentSearch(query: String) = update { it.copy(recentSearches = it.recentSearches - query) }

    fun clearRecentSearches() = update { it.copy(recentSearches = emptyList()) }

    fun exportJson(): JsonElement = json.encodeToJsonElement(SettingsState.serializer(), _state.value)

    fun importJson(element: JsonElement) {
        val s = json.decodeFromJsonElement(SettingsState.serializer(), element)
        update { s }
    }

    /** Settings saved by the previous version, one key per value. */
    private fun migrateLegacy(p: SharedPreferences): SettingsState {
        fun <E : Enum<E>> enumOf(values: Array<E>, name: String?, default: E): E =
            values.firstOrNull { it.name == name } ?: default
        if (!p.contains("theme_mode")) return SettingsState()
        return SettingsState(
            sound = SoundSettings(
                speed = p.getFloat("sound_speed", 1f),
                pitch = p.getFloat("sound_pitch", 1f),
                reverb = enumOf(ReverbLevel.entries.toTypedArray(), p.getString("sound_reverb", null), ReverbLevel.OFF),
                bassBoost = p.getInt("sound_bass", 0),
            ),
            playback = PlaybackSettings(autoplay = p.getBoolean("autoplay", true)),
            recentSearches = p.getString("recent_searches", null)?.split('\u001F')?.filter { it.isNotBlank() }.orEmpty(),
        )
    }

    private const val MAX_RECENT = 12
    private const val K_STATE = "state_v2"
}

