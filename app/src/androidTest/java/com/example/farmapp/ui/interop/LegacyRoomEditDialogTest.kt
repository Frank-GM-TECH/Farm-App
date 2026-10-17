package com.example.farmapp.ui.interop

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.farmapp.R
import com.example.farmapp.data.room.RoomDemoDetailState
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.ui.room.RoomDemoDetailScreen
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Requieren emulador: verifica AndroidViewBinding con EditText reales. */
class LegacyRoomEditDialogTest {
    @get:Rule val compose = createComposeRule()

    private val testItem = RoomDemoItem(10, "Gasas demo", 650, 3)

    @Test fun abreDialogoConViewsDesdePantallaCompose() {
        compose.setContent { FarmAppTheme {
            RoomDemoDetailScreen(
                RoomDemoDetailState(testItem, loaded = true), {}, {}, {}, {}, { _, _, _ -> }
            )
        } }
        compose.onNodeWithTag("room32_open_dialog").performClick()
        compose.onNodeWithTag("room32_dialog").assertIsDisplayed()
    }

    @Test fun editarLosEditTextEntregaDatosActualizados() {
        var received: Triple<String,String,String>? = null
        compose.setContent { FarmAppTheme {
            RoomDemoDetailScreen(
                RoomDemoDetailState(testItem, loaded = true), {}, {}, {}, {},
                { n, p, s -> received = Triple(n, p, s) }
            )
        } }
        compose.onNodeWithTag("room32_open_dialog").performClick()
        onView(withId(R.id.legacy_item_name)).perform(replaceText("Gasas editadas"))
        onView(withId(R.id.legacy_item_price)).perform(replaceText("7.25"))
        onView(withId(R.id.legacy_item_stock)).perform(replaceText("9"))
        compose.onNodeWithTag("room32_save").performClick()
        compose.runOnIdle { assertEquals(Triple("Gasas editadas", "7.25", "9"), received) }
    }

    @Test fun datosInvalidosNoGuardan() {
        var calls = 0
        compose.setContent { FarmAppTheme {
            RoomDemoDetailScreen(
                RoomDemoDetailState(testItem, loaded = true), {}, {}, {}, {},
                { _, _, _ -> calls++ }
            )
        } }
        compose.onNodeWithTag("room32_open_dialog").performClick()
        onView(withId(R.id.legacy_item_price)).perform(replaceText("-12"))
        compose.onNodeWithTag("room32_save").performClick()
        compose.runOnIdle { assertEquals(0, calls) }
        compose.onNodeWithTag("room32_dialog").assertIsDisplayed()
    }

    @Test fun cancelarNoGuarda() {
        var calls = 0
        compose.setContent { FarmAppTheme {
            RoomDemoDetailScreen(
                RoomDemoDetailState(testItem, loaded = true), {}, {}, {}, {},
                { _, _, _ -> calls++ }
            )
        } }
        compose.onNodeWithTag("room32_open_dialog").performClick()
        compose.onNodeWithTag("room32_cancel").performClick()
        compose.runOnIdle { assertEquals(0, calls) }
    }
}
