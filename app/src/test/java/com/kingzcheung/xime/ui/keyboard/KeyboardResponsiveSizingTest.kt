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
    fun scalingIsClampedForExtremeKeySizes() {
        assertEquals(0.9f, adaptiveKeyContentScale(keyHeightDp = 20f))
        assertEquals(1.5f, adaptiveKeyContentScale(keyHeightDp = 120f))
    }
}
