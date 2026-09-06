package com.kingzcheung.xime.ui.menubar

import org.junit.Assert.assertEquals
import org.junit.Test

class ToolbarCustomizeLayoutTest {
    @Test
    fun phonePortraitUsesFourColumns() {
        assertEquals(4, toolbarCustomizeColumnCount(392f, isLandscape = false))
    }

    @Test
    fun widePhoneAndTabletPortraitUseFiveColumns() {
        assertEquals(5, toolbarCustomizeColumnCount(430f, isLandscape = false))
        assertEquals(5, toolbarCustomizeColumnCount(752f, isLandscape = false))
    }

    @Test
    fun landscapeAddsColumnsWithoutCreatingAnUnboundedGrid() {
        assertEquals(6, toolbarCustomizeColumnCount(700f, isLandscape = true))
        assertEquals(8, toolbarCustomizeColumnCount(960f, isLandscape = true))
        assertEquals(8, toolbarCustomizeColumnCount(1600f, isLandscape = true))
    }
}
