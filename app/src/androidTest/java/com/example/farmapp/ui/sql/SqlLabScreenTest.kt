package com.example.farmapp.ui.sql

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.farmapp.data.sql.SqlLabProduct
import com.example.farmapp.data.sql.SqlLabQuery
import com.example.farmapp.data.sql.SqlLabResult
import com.example.farmapp.data.sql.SqlLabUiState
import com.example.farmapp.ui.theme.FarmAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Sin base de datos ni red: estado recibido y callbacks proporcionados por la prueba. */
class SqlLabScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun muestraConsultaYProductos() {
        composeRule.setContent {
            FarmAppTheme {
                SqlLabScreen(
                    state = SqlLabUiState(result = SqlLabResult.Products(listOf(
                        SqlLabProduct("FA-001", "Alcohol en gel", "Higiene", 12)
                    ))),
                    onBack = {}, onQuery = {}, onInsert = {}, onUpdate = {}, onDelete = {}, onRestore = {}
                )
            }
        }
        composeRule.onNodeWithTag("sql_lab_screen").assertExists()
        composeRule.onNodeWithTag("sql_statement").assertExists()
        composeRule.onNodeWithTag("sql_list").performScrollToNode(hasTestTag("sql_item_FA-001"))
        composeRule.onNodeWithTag("sql_item_FA-001").assertExists()
    }

    @Test fun pulsarChipEmiteConsultaSinSQLDirecta() {
        var selected = SqlLabQuery.ALL
        composeRule.setContent {
            FarmAppTheme {
                SqlLabScreen(SqlLabUiState(), {}, { selected = it }, {}, {}, {}, {})
            }
        }
        composeRule.onNodeWithTag("sql_query_NAME_LIKE").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(SqlLabQuery.NAME_LIKE, selected) }
    }

    @Test fun insertarAccionaCallbackPeroNoModificaDatosSinViewModel() {
        var calls = 0
        composeRule.setContent {
            FarmAppTheme {
                SqlLabScreen(SqlLabUiState(), {}, {}, { calls++ }, {}, {}, {})
            }
        }
        composeRule.onNodeWithTag("sql_insert").performClick()
        composeRule.runOnIdle { assertEquals(1, calls) }
    }
}
