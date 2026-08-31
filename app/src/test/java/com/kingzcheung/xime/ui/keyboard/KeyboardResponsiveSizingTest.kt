package com.kingzcheung.xime.ui.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardResponsiveSizingTest {
    @Test
    fun phoneSizedKeyKeepsOriginalSizing() {
        assertEquals(1f, adaptiveKeyContentScale(keyHeightDp = 56f))
    }

    @Test
    fun largerKeyScalesContinuously() {
        assertEquals(1.25f, adaptiveKeyContentScale(keyHeightDp = 70f))
    }

    @Test
    fun smallerKeysAreNotShrunkAndLargeKeysAreClamped() {
        assertEquals(1f, adaptiveKeyContentScale(keyHeightDp = 20f))
        assertEquals(1.4f, adaptiveKeyContentScale(keyHeightDp = 120f))
    }

    @Test
    fun hintsGrowMoreSlowlyAndMoveAwayFromMainLabel() {
        assertEquals(1.22f, adaptiveHintScale(contentScale = 1.4f))
        assertEquals(21.2f, adaptiveHintOffsetDp(contentScale = 1.4f))
    }
}
