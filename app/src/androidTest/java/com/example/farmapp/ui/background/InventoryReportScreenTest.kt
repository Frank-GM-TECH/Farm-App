package com.example.farmapp.ui.background

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.farmapp.data.background.ReportPhase
import com.example.farmapp.data.background.ReportWorkUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InventoryReportScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test fun exito_permiteProgramarUnInforme() {
        var calls = 0
        rule.setContent { FarmAppTheme { InventoryReportScreen(ReportWorkUiState(), {}, { calls++ }, {}) } }
        rule.onNodeWithTag("work_start").performClick()
        assertEquals(1, calls)
    }
    @Test fun limite_duranteLaEjecucionSoloPermiteCancelar() {
        var calls = 0
        rule.setContent { FarmAppTheme {
            InventoryReportScreen(ReportWorkUiState(ReportPhase.PREPARING), {}, {}, { calls++ })
        } }
        rule.onNodeWithTag("work_start").assertIsNotEnabled()
        rule.onNodeWithTag("work_cancel").assertIsEnabled().performClick()
        assertEquals(1, calls)
    }
    @Test fun exito_muestraArchivoAlFinal() {
        rule.setContent { FarmAppTheme {
            InventoryReportScreen(ReportWorkUiState(ReportPhase.COMPLETED, "informe_demo.txt"), {}, {}, {})
        } }
        rule.onNodeWithTag("work_file_name").assertIsDisplayed()
    }
    @Test fun trabajoEnEsperaMuestraRestriccionesYPermiteCancelar() {
        rule.setContent { FarmAppTheme {
            InventoryReportScreen(ReportWorkUiState(ReportPhase.QUEUED), {}, {}, {})
        } }
        rule.onNodeWithTag("work_constraints_notice").assertIsDisplayed()
        rule.onNodeWithTag("work_cancel").assertIsEnabled()
    }

}
