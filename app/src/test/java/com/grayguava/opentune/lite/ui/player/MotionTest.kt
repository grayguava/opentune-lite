package com.grayguava.opentune.lite.ui.player

import com.grayguava.opentune.lite.ui.components.Haptics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionTest {
    @Test fun hapticStrengthMapsToWords() {
        assertEquals("Off", Haptics.label(0f))
        assertEquals("Light", Haptics.label(0.3f))
        assertEquals("Medium", Haptics.label(0.6f))
        assertEquals("Strong", Haptics.label(1f))
    }
}
