package com.kingzcheung.xime.ui.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardResponsiveSizingTest {
    @Test
    fun phoneSizedKeyKeepsOriginalSizing() {
        assertEquals(1f, adaptiveKeyContentScale(keyHeightDp = 56f))
        assertEquals(1f, adaptiveHintScale(contentScale = 1f))
        assertEquals(14f, adaptiveHintOffsetDp(contentScale = 1f))
    }

    @Test
    fun largerKeyScalesLabelsAndHintsWithinLimits() {
        assertEquals(1.25f, adaptiveKeyContentScale(keyHeightDp = 70f))
        assertEquals(1.6f, adaptiveHintScale(contentScale = 1.4f))
        assertEquals(24f, adaptiveHintOffsetDp(contentScale = 1.4f))
    }

    @Test
    fun smallerKeysAreNotShrunkAndLargeKeysAreClamped() {
        assertEquals(1f, adaptiveKeyContentScale(keyHeightDp = 20f))
        assertEquals(1.4f, adaptiveKeyContentScale(keyHeightDp = 120f))
    }
}
