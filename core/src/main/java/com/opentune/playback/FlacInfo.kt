package com.opentune.playback

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import java.util.Locale

/**
 * What a FLAC stream really is: the bit depth, sample rate and channel count
 * read from its STREAMINFO block, which the decoder reports far less
 * precisely. Also the few vendor decoders that cannot be trusted with FLAC.
 */
internal object FlacInfo {
    data class Info(val rate: Int, val channels: Int, val bits: Int, val durationMs: Long = 0)

    /** A vendor decoder that mishandles FLAC: some Samsung builds do. */
    fun unsafeDecoder(name: String): Boolean = name.lowercase(Locale.ROOT).let {
        it == "c2.sec.flac.decoder" || (it.startsWith("omx.sec.") && it.contains("flac"))
    }

    fun of(format: Format): Info? = if (format.sampleMimeType == MimeTypes.AUDIO_FLAC)
        format.initializationData.firstNotNullOfOrNull(::of) else null

    fun of(data: ByteArray): Info? {
        fun b(i: Int) = data[i].toInt() and 255
        fun header(i: Int) = data.size >= i + 4 && b(i) and 127 == 0 && b(i + 1) == 0 && b(i + 2) == 0 && b(i + 3) == 34
        val body = when {
            data.size >= 4 && String(data, 0, 4, Charsets.US_ASCII) == "fLaC" -> if (header(4)) 8 else 4
            header(0) -> 4
            data.size == 34 -> 0
            else -> return null
        }
        if (data.size < body + 34) return null
        val rate = (b(body + 10) shl 12) or (b(body + 11) shl 4) or (b(body + 12) shr 4)
        val channels = ((b(body + 12) shr 1) and 7) + 1
        val bits = (((b(body + 12) and 1) shl 4) or (b(body + 13) shr 4)) + 1
        var samples = (b(body + 13) and 15).toLong()
        for (i in 14..17) samples = (samples shl 8) or b(body + i).toLong()
        return Info(rate, channels, bits, if (rate > 0) samples * 1000 / rate else 0).takeIf { rate in 8000..384000 && bits in 4..32 }
    }
}
