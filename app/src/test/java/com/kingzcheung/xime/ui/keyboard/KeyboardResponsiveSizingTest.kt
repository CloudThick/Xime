package com.kingzcheung.xime.ui.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardResponsiveSizingTest {
    private val tolerance = 0.0001f

    @Test
    fun phoneSizedKeyKeepsOriginalSizing() {
        assertEquals(1f, adaptiveKeyContentScale(keyHeightDp = 56f), tolerance)
        assertEquals(1f, adaptiveHintScale(contentScale = 1f), tolerance)
        assertEquals(14f, adaptiveHintOffsetDp(contentScale = 1f), tolerance)
    }

    @Test
    fun largerKeyScalesLabelsAndHintsWithinLimits() {
        assertEquals(1.25f, adaptiveKeyContentScale(keyHeightDp = 70f), tolerance)
        assertEquals(1.7f, adaptiveHintScale(contentScale = 1.5f), tolerance)
        assertEquals(1.5f, adaptiveBubbleScale(contentScale = 1.5f), tolerance)
        assertEquals(24f, adaptiveHintOffsetDp(contentScale = 1.5f), tolerance)
    }

    @Test
    fun smallerKeysAreNotShrunkAndLargeKeysAreClamped() {
        assertEquals(1f, adaptiveKeyContentScale(keyHeightDp = 20f), tolerance)
        assertEquals(1.5f, adaptiveKeyContentScale(keyHeightDp = 120f), tolerance)
    }

    @Test
    fun cornerScaleFollowsContentScaleWithinLimits() {
        assertEquals(1f, adaptiveKeyCornerScale(1f), tolerance)
        assertEquals(1.25f, adaptiveKeyCornerScale(1.25f), tolerance)
        assertEquals(1.5f, adaptiveKeyCornerScale(1.5f), tolerance)
    }

    @Test
    fun portraitPaddingScaleGrowsAtHalfRate() {
        assertEquals(1f, adaptiveKeyPaddingScale(1f, isLandscape = false), tolerance)
        assertEquals(1.125f, adaptiveKeyPaddingScale(1.25f, isLandscape = false), tolerance)
        assertEquals(1.25f, adaptiveKeyPaddingScale(1.5f, isLandscape = false), tolerance)
    }

    @Test
    fun landscapePaddingScaleGrowsAtDoubleRate() {
        assertEquals(1f, adaptiveKeyPaddingScale(1f, isLandscape = true), tolerance)
        assertEquals(1.5f, adaptiveKeyPaddingScale(1.25f, isLandscape = true), tolerance)
        assertEquals(2f, adaptiveKeyPaddingScale(1.5f, isLandscape = true), tolerance)
    }

    @Test
    fun portraitHorizontalPaddingScaleGrowsFasterThanVertical() {
        assertEquals(1f, adaptiveKeyHorizontalPaddingScale(1f, isLandscape = false), tolerance)
        assertEquals(1.875f, adaptiveKeyHorizontalPaddingScale(1.25f, isLandscape = false), tolerance)
        assertEquals(2.5f, adaptiveKeyHorizontalPaddingScale(1.5f, isLandscape = false), tolerance)
    }

    @Test
    fun landscapeHorizontalPaddingScaleGrowsFasterThanVertical() {
        assertEquals(1f, adaptiveKeyHorizontalPaddingScale(1f, isLandscape = true), tolerance)
        assertEquals(2f, adaptiveKeyHorizontalPaddingScale(1.25f, isLandscape = true), tolerance)
        assertEquals(3f, adaptiveKeyHorizontalPaddingScale(1.5f, isLandscape = true), tolerance)
    }

    @Test
    fun geometryScalesDoNotShrinkBelowOneOrExceedCaps() {
        assertEquals(1f, adaptiveKeyCornerScale(0.5f), tolerance)
        assertEquals(1.5f, adaptiveKeyCornerScale(2f), tolerance)
        assertEquals(1f, adaptiveKeyPaddingScale(0.2f, isLandscape = false), tolerance)
        assertEquals(1.25f, adaptiveKeyPaddingScale(3f, isLandscape = false), tolerance)
        assertEquals(1f, adaptiveKeyPaddingScale(0.2f, isLandscape = true), tolerance)
        assertEquals(2f, adaptiveKeyPaddingScale(3f, isLandscape = true), tolerance)
        assertEquals(1f, adaptiveKeyHorizontalPaddingScale(0.2f, isLandscape = false), tolerance)
        assertEquals(2.5f, adaptiveKeyHorizontalPaddingScale(3f, isLandscape = false), tolerance)
        assertEquals(1f, adaptiveKeyHorizontalPaddingScale(0.2f, isLandscape = true), tolerance)
        assertEquals(3.5f, adaptiveKeyHorizontalPaddingScale(3f, isLandscape = true), tolerance)
        assertEquals(1f, adaptiveKeyCornerScale(Float.NaN), tolerance)
        assertEquals(1f, adaptiveKeyPaddingScale(Float.NaN, isLandscape = false), tolerance)
        assertEquals(1f, adaptiveKeyHorizontalPaddingScale(Float.NaN, isLandscape = true), tolerance)
        assertEquals(1f, adaptiveKeyContentScale(keyHeightDp = Float.NaN), tolerance)
        assertEquals(1f, adaptiveKeyContentScale(keyHeightDp = -8f), tolerance)
    }

    @Test
    fun phonePortraitGeometryKeepsDefaultCornerAndPadding() {
        val rowOuterHeight = QWERTY_PORTRAIT_REFERENCE_HEIGHT_DP +
            2f * QWERTY_PORTRAIT_PADDING_VERTICAL_DP
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = rowOuterHeight,
            configuredCornerRadiusDp = 8f,
            isLandscape = false,
            isFloatingMode = false,
        )
        assertEquals(1f, geometry.contentScale, tolerance)
        assertEquals(1f, geometry.cornerScale, tolerance)
        assertEquals(1f, geometry.paddingScale, tolerance)
        assertEquals(1f, geometry.horizontalPaddingScale, tolerance)
        assertEquals(8f, geometry.cornerRadiusDp, tolerance)
        assertEquals(2f, geometry.paddingHorizontalDp, tolerance)
        assertEquals(4.25f, geometry.paddingVerticalDp, tolerance)
        assertEquals(1f, geometry.shadowElevationDp, tolerance)
    }

    @Test
    fun phoneLandscapeGeometryKeepsCompactPadding() {
        val rowOuterHeight = QWERTY_LANDSCAPE_REFERENCE_HEIGHT_DP +
            2f * QWERTY_LANDSCAPE_PADDING_VERTICAL_DP
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = rowOuterHeight,
            configuredCornerRadiusDp = 8f,
            isLandscape = true,
            isFloatingMode = false,
        )
        assertEquals(1f, geometry.contentScale, tolerance)
        assertEquals(8f, geometry.cornerRadiusDp, tolerance)
        assertEquals(1f, geometry.paddingHorizontalDp, tolerance)
        assertEquals(2f, geometry.paddingVerticalDp, tolerance)
    }

    @Test
    fun portraitGeometryUsesConfirmedCheckpoints() {
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = 70f + 2f * QWERTY_PORTRAIT_PADDING_VERTICAL_DP,
            configuredCornerRadiusDp = 8f,
            isLandscape = false,
            isFloatingMode = false,
        )
        assertEquals(1.25f, geometry.contentScale, tolerance)
        assertEquals(10f, geometry.cornerRadiusDp, tolerance)
        assertEquals(2f * 1.875f, geometry.paddingHorizontalDp, tolerance)
        assertEquals(4.25f * 1.125f, geometry.paddingVerticalDp, tolerance)

        val maxGeometry = qwertyKeyGeometry(
            rowOuterHeightDp = 84f + 2f * QWERTY_PORTRAIT_PADDING_VERTICAL_DP,
            configuredCornerRadiusDp = 8f,
            isLandscape = false,
            isFloatingMode = false,
        )
        assertEquals(1.5f, maxGeometry.contentScale, tolerance)
        assertEquals(12f, maxGeometry.cornerRadiusDp, tolerance)
        assertEquals(5f, maxGeometry.paddingHorizontalDp, tolerance)
        assertEquals(4.25f * 1.25f, maxGeometry.paddingVerticalDp, tolerance)
        assertEquals(1.5f, maxGeometry.shadowElevationDp, tolerance)
    }

    @Test
    fun landscapeGeometryUsesConfirmedCheckpoints() {
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = 55f + 2f * QWERTY_LANDSCAPE_PADDING_VERTICAL_DP,
            configuredCornerRadiusDp = 8f,
            isLandscape = true,
            isFloatingMode = false,
        )
        assertEquals(1.25f, geometry.contentScale, tolerance)
        assertEquals(10f, geometry.cornerRadiusDp, tolerance)
        assertEquals(2f, geometry.paddingHorizontalDp, tolerance)
        assertEquals(3f, geometry.paddingVerticalDp, tolerance)

        val maxGeometry = qwertyKeyGeometry(
            rowOuterHeightDp = 66f + 2f * QWERTY_LANDSCAPE_PADDING_VERTICAL_DP,
            configuredCornerRadiusDp = 8f,
            isLandscape = true,
            isFloatingMode = false,
        )
        assertEquals(1.5f, maxGeometry.contentScale, tolerance)
        assertEquals(12f, maxGeometry.cornerRadiusDp, tolerance)
        assertEquals(3f, maxGeometry.paddingHorizontalDp, tolerance)
        assertEquals(4f, maxGeometry.paddingVerticalDp, tolerance)
        assertEquals(1.5f, maxGeometry.shadowElevationDp, tolerance)
    }

    @Test
    fun geometryPreservesConfiguredCornerRadiusAsBase() {
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = 84f + 2f * QWERTY_PORTRAIT_PADDING_VERTICAL_DP,
            configuredCornerRadiusDp = 10f,
            isLandscape = false,
            isFloatingMode = false,
        )
        assertEquals(15f, geometry.cornerRadiusDp, tolerance)
    }

    @Test
    fun geometryScaleIgnoresScaledPaddingFeedback() {
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = 84f + 2f * QWERTY_PORTRAIT_PADDING_VERTICAL_DP,
            configuredCornerRadiusDp = 8f,
            isLandscape = false,
            isFloatingMode = false,
        )
        assertEquals(1.5f, geometry.contentScale, tolerance)
    }

    @Test
    fun floatingModeKeepsOriginalGeometry() {
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = 120f,
            configuredCornerRadiusDp = 8f,
            isLandscape = false,
            isFloatingMode = true,
        )
        assertEquals(1f, geometry.contentScale, tolerance)
        assertEquals(1f, geometry.cornerScale, tolerance)
        assertEquals(1f, geometry.paddingScale, tolerance)
        assertEquals(1f, geometry.horizontalPaddingScale, tolerance)
        assertEquals(8f, geometry.cornerRadiusDp, tolerance)
        assertEquals(2f, geometry.paddingHorizontalDp, tolerance)
        assertEquals(4.25f, geometry.paddingVerticalDp, tolerance)
        assertEquals(1f, geometry.shadowElevationDp, tolerance)
    }

    @Test
    fun shadowElevationFollowsConfiguredBaseAndKeyScale() {
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = 70f + 2f * QWERTY_PORTRAIT_PADDING_VERTICAL_DP,
            configuredCornerRadiusDp = 8f,
            isLandscape = false,
            isFloatingMode = false,
            configuredShadowElevationDp = 2f,
        )
        assertEquals(2.5f, geometry.shadowElevationDp, tolerance)
    }

    @Test
    fun cornerRadiusDoesNotExceedHalfKeyCapHeight() {
        val geometry = qwertyKeyGeometry(
            rowOuterHeightDp = 20f,
            configuredCornerRadiusDp = 8f,
            isLandscape = false,
            isFloatingMode = false,
        )
        val visualCapHeight = 20f - 2f * QWERTY_PORTRAIT_PADDING_VERTICAL_DP
        assertEquals(visualCapHeight / 2f, geometry.cornerRadiusDp, tolerance)
    }
}
