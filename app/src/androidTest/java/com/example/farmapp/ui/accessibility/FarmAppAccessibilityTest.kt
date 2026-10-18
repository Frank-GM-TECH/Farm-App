package com.example.farmapp.ui.accessibility

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.onNodeWithText
import com.example.farmapp.R
import com.example.farmapp.auth.AuthUiState
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.ui.auth.AuthScreen
import com.example.farmapp.ui.catalog.AnimatedProductDemoCard
import com.example.farmapp.ui.catalog.CatalogLearningPreview
import com.example.farmapp.ui.theme.FarmAppTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

/** Cotejo 12: comprueba el árbol semántico Compose. Requiere emulador Android. */
class FarmAppAccessibilityTest {
    @get:Rule val rule = createComposeRule()

    @Test fun errorDeAutenticacionSeAnunciaComoRegionViva() {
        rule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(errorMessageRes = R.string.auth_error_invalid_email),
                    onSignIn = { _, _ -> }, onRegister = { _, _ -> },
                    onResetPassword = {}, onDismissMessage = {}
                )
            }
        }
        rule.onNodeWithTag("auth_error_message")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test fun contrasenaTieneAccionConDescripcionContextual() {
        rule.setContent {
            FarmAppTheme {
                AuthScreen(
                    uiState = AuthUiState(),
                    onSignIn = { _, _ -> }, onRegister = { _, _ -> },
                    onResetPassword = {}, onDismissMessage = {}
                )
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithTag("auth_password_visibility")
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription,
                listOf(context.getString(R.string.a11y_show_password))
            ))
        rule.onNodeWithTag("auth_password_visibility").performClick()
        rule.onNodeWithTag("auth_password_visibility")
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription,
                listOf(context.getString(R.string.a11y_hide_password))
            ))
    }

    @Test fun expandirTarjetaActualizaDescripcionDeEstado() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.setContent { FarmAppTheme { AnimatedProductDemoCard(DemoCatalogData.products.first()) } }
        rule.onNodeWithTag("catalog_toggle_FA-001")
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                context.getString(R.string.a11y_details_collapsed)
            ))
        rule.onNodeWithTag("catalog_toggle_FA-001").performClick()
        rule.onNodeWithTag("catalog_toggle_FA-001")
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                context.getString(R.string.a11y_details_expanded)
            ))
    }

    @Test fun elCatalogoTieneEncabezadosSemanticos() {
        rule.setContent {
            FarmAppTheme { CatalogLearningPreview(modifier = Modifier.fillMaxSize()) }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        rule.onNodeWithText(context.getString(R.string.catalog_demo_title))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }
}
