package com.example.farmapp.ui.room

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.data.room.RoomDemoDetailState
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RoomDemoDetailsScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun venderInvocaCallback() {
        var sold=0
        compose.setContent { FarmAppTheme { RoomDemoDetailScreen(
            RoomDemoDetailState(RoomDemoItem(1,"Gel",250,2),loaded=true),
            {}, {}, {sold++}, {}
        ) } }
        compose.onNodeWithTag("room26_sell").performClick()
        compose.runOnIdle { assertEquals(1,sold) }
    }
    @Test fun agotadoDeshabilitaVenta() {
        compose.setContent { FarmAppTheme { RoomDemoDetailScreen(
            RoomDemoDetailState(RoomDemoItem(1,"Gel",250,0),loaded=true),
            {}, {}, {}, {}
        ) } }
        compose.onNodeWithTag("room26_sell").assertIsNotEnabled()
    }
    @Test fun articuloInexistenteMuestraMensaje() {
        compose.setContent { FarmAppTheme { RoomDemoDetailScreen(
            RoomDemoDetailState(item=null,loaded=true),{}, {}, {}, {}
        ) } }
        compose.onNodeWithTag("room26_missing").assertIsDisplayed()
    }
    @Test fun edicionLlamaCallbackConDatos() {
        var received: Triple<String,String,String>? = null
        compose.setContent { FarmAppTheme { RoomDemoEditScreen(
            RoomDemoDetailState(RoomDemoItem(1,"Gel",250,2),loaded=true),
            {}, {n,p,s->received=Triple(n,p,s)}
        ) } }
        compose.onNodeWithTag("room26_update").performClick()
        compose.runOnIdle { assertEquals(Triple("Gel","2.50","2"),received) }
    }
}
