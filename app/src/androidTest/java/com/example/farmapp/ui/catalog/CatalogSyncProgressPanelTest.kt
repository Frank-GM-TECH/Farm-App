package com.example.farmapp.ui.catalog

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.farmapp.catalog.CatalogSyncUiState
import com.example.farmapp.catalog.SyncPhase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** C20: verifica interfaz sin conectar Firebase ni ejecutar corrutinas reales. */
class CatalogSyncProgressPanelTest {
    @get:Rule val rule = createComposeRule()

    @Test fun inicialPermiteIniciarYTieneDosIndicadores() {
        var starts = 0
        rule.setContent {
            CatalogSyncProgressPanel(CatalogSyncUiState(),
                onStartOrResume = { starts++ }, onPause = {}, onReset = {})
        }
        rule.onNodeWithTag("sync_catalog_progress").assertExists()
        rule.onNodeWithTag("sync_stock_progress").assertExists()
        rule.onNodeWithTag("sync_start").performClick()
        rule.runOnIdle { assertEquals(1, starts) }
    }

    @Test fun corriendoPermitePausar() {
        var pauses = 0
        rule.setContent {
            CatalogSyncProgressPanel(CatalogSyncUiState(10,20,SyncPhase.RUNNING),
                onStartOrResume = {}, onPause = { pauses++ }, onReset = {})
        }
        rule.onNodeWithTag("sync_pause").performClick()
        rule.runOnIdle { assertEquals(1, pauses) }
    }

    @Test fun terminadoDeshabilitaInicioPeroPermiteReinicio() {
        var resets = 0
        rule.setContent {
            CatalogSyncProgressPanel(CatalogSyncUiState(100,100,SyncPhase.FINISHED),
                onStartOrResume = {}, onPause = {}, onReset = { resets++ })
        }
        rule.onNodeWithTag("sync_start").assertIsNotEnabled()
        rule.onNodeWithTag("sync_reset").performClick()
        rule.runOnIdle { assertEquals(1, resets) }
    }
}
