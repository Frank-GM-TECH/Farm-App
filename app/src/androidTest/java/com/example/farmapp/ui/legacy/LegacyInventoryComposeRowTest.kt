package com.example.farmapp.ui.legacy

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** C31: pruebas de la fila Compose sin Firebase ni conexión a Internet. */
class LegacyInventoryComposeRowTest {
    @get:Rule val composeRule = createComposeRule()

    private val article = RoomDemoItem(
        id = 42L,
        name = "Venda elástica (demo)",
        priceCents = 990L,
        stock = 3
    )

    @Test fun rowShowsRoomValuesAndIsClickable() {
        composeRule.setContent {
            FarmAppTheme {
                LegacyInventoryComposeRow(item = article, onClick = {})
            }
        }
        composeRule.onNodeWithTag("legacy_compose_item_42")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeRule.onNodeWithText("Venda elástica (demo)").assertIsDisplayed()
        composeRule.onNodeWithText("3 unidades", substring = true).assertIsDisplayed()
    }

    @Test fun clickSendsStableRoomIdThroughCallback() {
        var selectedId: Long? = null
        composeRule.setContent {
            FarmAppTheme {
                LegacyInventoryComposeRow(item = article, onClick = { selectedId = article.id })
            }
        }
        composeRule.onNodeWithTag("legacy_compose_item_42").performClick()
        composeRule.runOnIdle { assertEquals(42L, selectedId ?: -1L) }
    }

    @Test fun rowRecomposesWhenRoomDataChanges() {
        val latest = androidx.compose.runtime.mutableStateOf(article)
        composeRule.setContent {
            FarmAppTheme {
                LegacyInventoryComposeRow(item = latest.value, onClick = {})
            }
        }
        composeRule.onNodeWithText("3 unidades", substring = true).assertIsDisplayed()
        composeRule.runOnIdle { latest.value = article.copy(stock = 2) }
        composeRule.onNodeWithText("2 unidades", substring = true).assertIsDisplayed()
    }
}
