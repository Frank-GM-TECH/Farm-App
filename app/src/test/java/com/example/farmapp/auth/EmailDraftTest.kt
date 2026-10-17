package com.example.farmapp.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailDraftTest {
    @Test fun setterEliminaEspaciosExternos() {
        val draft = EmailDraft(" cliente@farmapp.pe ")
        assertEquals("cliente@farmapp.pe", draft.value)
        assertTrue(draft.isValid)
        draft.value = " sin-arroba "
        assertEquals("sin-arroba", draft.value)
        assertFalse(draft.isValid)
    }
}
