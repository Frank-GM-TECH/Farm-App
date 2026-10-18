package com.example.farmapp.ui.room

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.data.room.RoomDemoUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RoomDemoScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun formularioEnviaDatosAlPadre() {
        var received: Triple<String,String,String>? = null
        compose.setContent { FarmAppTheme {
            RoomDemoScreen(RoomDemoUiState(), {}, { n,p,s -> received = Triple(n,p,s) }, {}, {})
        } }
        compose.onNodeWithTag("room_demo_list").performScrollToNode(hasTestTag("room_name"))
        compose.onNodeWithTag("room_name").performTextInput("Gasas")
        compose.onNodeWithTag("room_demo_list").performScrollToNode(hasTestTag("room_price"))
        compose.onNodeWithTag("room_price").performTextInput("6.50")
        compose.onNodeWithTag("room_demo_list").performScrollToNode(hasTestTag("room_stock"))
        compose.onNodeWithTag("room_stock").performTextInput("3")
        compose.onNodeWithTag("room_demo_list").performScrollToNode(hasTestTag("room_save"))
        compose.onNodeWithTag("room_save").performClick()
        compose.runOnIdle { assertEquals(Triple("Gasas","6.50","3"), received) }
    }

    @Test fun listaMuestraItemConClaveEstable() {
        compose.setContent { FarmAppTheme {
            RoomDemoScreen(RoomDemoUiState(items=listOf(RoomDemoItem(1,"Gel",850,2))), {}, {_,_,_->}, {}, {})
        } }
        compose.onNodeWithTag("room_demo_list").performScrollToNode(hasTestTag("room_item_1"))
        compose.onNodeWithTag("room_item_1").assertExists()
    }

    @Test fun baseVaciaMuestraMensaje() {
        compose.setContent { FarmAppTheme {
            RoomDemoScreen(RoomDemoUiState(), {}, {_,_,_->}, {}, {})
        } }
        compose.onNodeWithTag("room_demo_list").performScrollToNode(hasTestTag("room_empty"))
        compose.onNodeWithTag("room_empty").assertIsDisplayed()
    }
}
