package com.example.farmapp.ui.legacy

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyEntryModeTest {
    @Test fun fabUsesNewMode() = assertEquals(LegacyEntryMode.NEW, legacyEntryModeFor(0L))
    @Test fun rowUsesEditMode() = assertEquals(LegacyEntryMode.EDIT, legacyEntryModeFor(1L))
    @Test fun largestValidIdUsesEditMode() = assertEquals(LegacyEntryMode.EDIT, legacyEntryModeFor(Long.MAX_VALUE))
    @Test(expected = IllegalArgumentException::class)
    fun negativeIdRejected() { legacyEntryModeFor(-1L) }
}
