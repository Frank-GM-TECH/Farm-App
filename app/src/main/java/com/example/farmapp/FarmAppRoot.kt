package com.example.farmapp

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.auth.AuthViewModel
import com.example.farmapp.auth.GoogleCredentialSignIn
import com.example.farmapp.auth.MissingGoogleClientIdException
import com.example.farmapp.data.preferences.AppThemePreference
import com.example.farmapp.data.preferences.UserPreferencesViewModel
import com.example.farmapp.ui.components.ThemeModeSelector
import com.example.farmapp.ui.navigation.FarmAppNavHost
import com.example.farmapp.ui.theme.FarmAppTheme
import com.example.farmapp.ui.theme.FarmThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val GOOGLE_SIGN_IN_TAG = "FarmAppGoogleSignIn"

@Composable
fun FarmAppRoot(
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory
    ),
    preferencesViewModel: UserPreferencesViewModel = viewModel(
        factory = UserPreferencesViewModel.Factory
    )
) {

    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Gestor oficial de credenciales de Android.
    val credentialSignIn = remember(context) {
        GoogleCredentialSignIn(
            CredentialManager.create(context)
        )
    }

    var googlePickerOpen by remember {
        mutableStateOf(false)
    }

    // =========================================
    // INICIO DE SESIÓN CON GOOGLE
    // =========================================

    val onGoogleSignIn: () -> Unit = googleSignIn@{

        if (googlePickerOpen || uiState.isLoading) {
            Log.w(
                GOOGLE_SIGN_IN_TAG,
                "Se ignoró la solicitud: ya existe un proceso de autenticación."
            )
            return@googleSignIn
        }

        googlePickerOpen = true

        Log.d(
            GOOGLE_SIGN_IN_TAG,
            "Se inició el proceso de inicio de sesión con Google."
        )

        scope.launch {

            try {
                // La limpieza de Credential Manager solo corresponde al cierre
                // de sesión. Hacerla antes de cada selección puede invalidar
                // el estado del proveedor o impedir que se abra el selector.

                Log.d(
                    GOOGLE_SIGN_IN_TAG,
                    "Solicitando credenciales mediante Credential Manager..."
                )

                // Solicita el ID Token de Google.
                val token = credentialSignIn.requestIdToken(context)

                // Nunca registrar el contenido del token.
                Log.d(
                    GOOGLE_SIGN_IN_TAG,
                    "Google devolvió un ID token; no se registrará su contenido."
                )

                Log.d(
                    GOOGLE_SIGN_IN_TAG,
                    "Entregando el token al AuthViewModel..."
                )

                // FirebaseAuthRepository realizará la autenticación.
                authViewModel.signInWithGoogleIdToken(token)

                Log.d(
                    GOOGLE_SIGN_IN_TAG,
                    "Token entregado al AuthViewModel; Firebase procesará la sesión."
                )

            } catch (cancelled: GetCredentialCancellationException) {

                Log.i(
                    GOOGLE_SIGN_IN_TAG,
                    "El usuario canceló o cerró el selector de Google."
                )

            } catch (missing: MissingGoogleClientIdException) {

                Log.e(
                    GOOGLE_SIGN_IN_TAG,
                    "No se encontró default_web_client_id. Revisa la configuración de Google.",
                    missing
                )

                authViewModel.reportGoogleCredentialError(
                    configurationMissing = true
                )

            } catch (cancelled: CancellationException) {

                Log.w(
                    GOOGLE_SIGN_IN_TAG,
                    "Se canceló la tarea de inicio de sesión."
                )

                throw cancelled

            } catch (credentialError: GetCredentialException) {

                // Error seguro del proveedor, útil al investigar OAuth en
                // una instalación firmada por Google Play.
                Log.e(
                    GOOGLE_SIGN_IN_TAG,
                    "Credential Manager falló: tipo=${credentialError.type}, " +
                        "clase=${credentialError.javaClass.simpleName}"
                )
                authViewModel.reportGoogleCredentialError()

            } catch (error: Exception) {

                // Evitar volcar mensajes o excepciones que puedan contener
                // datos privados de la cuenta seleccionada.
                Log.e(
                    GOOGLE_SIGN_IN_TAG,
                    "Error inesperado de Google Sign-In: ${error.javaClass.name}"
                )

                authViewModel.reportGoogleCredentialError()

            } finally {

                googlePickerOpen = false

                Log.d(
                    GOOGLE_SIGN_IN_TAG,
                    "Finalizó el proceso de solicitud de credenciales."
                )
            }
        }
    }

    // =========================================
    // CIERRE DE SESIÓN
    // =========================================

    val onSignOut: () -> Unit = {

        Log.d(
            GOOGLE_SIGN_IN_TAG,
            "Cerrando sesión de Firebase."
        )

        authViewModel.signOut()

        scope.launch {

            try {

                credentialSignIn.clearCredentialState()

                Log.d(
                    GOOGLE_SIGN_IN_TAG,
                    "Estado de credenciales limpiado correctamente."
                )

            } catch (cancelled: CancellationException) {

                throw cancelled

            } catch (error: Exception) {

                Log.w(
                    GOOGLE_SIGN_IN_TAG,
                    "No se pudo limpiar el estado de Credential Manager.",
                    error
                )
            }
        }
    }

    // =========================================
    // PREFERENCIAS DEL USUARIO
    // =========================================

    val preferenceState by preferencesViewModel.uiState.collectAsStateWithLifecycle()

    val selectedTheme = FarmThemeMode.entries.firstOrNull {
        it.name == preferenceState.preferences.theme.name
    } ?: FarmThemeMode.SYSTEM

    val systemDarkTheme = isSystemInDarkTheme()

    val darkThemeEnabled = when (selectedTheme) {
        FarmThemeMode.SYSTEM -> systemDarkTheme
        FarmThemeMode.LIGHT -> false
        FarmThemeMode.DARK -> true
    }

    // =========================================
    // BARRAS DEL SISTEMA
    // =========================================

    val activity = context as? Activity

    SideEffect {

        activity?.window?.let { window ->

            WindowCompat.getInsetsController(
                window,
                window.decorView
            ).apply {

                isAppearanceLightStatusBars = !darkThemeEnabled
                isAppearanceLightNavigationBars = !darkThemeEnabled
            }
        }
    }

    // =========================================
    // TEMA Y NAVEGACIÓN
    // =========================================

    FarmAppTheme(mode = selectedTheme) {

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {

                ThemeModeSelector(
                    selectedMode = selectedTheme,
                    onModeSelected = {
                        preferencesViewModel.setTheme(
                            AppThemePreference.valueOf(it.name)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !preferenceState.isLoading
                )

                if (preferenceState.hasError) {

                    Text(
                        text = androidx.compose.ui.res.stringResource(
                            R.string.pref_error
                        ),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 4.dp
                        )
                    )
                }

                FarmAppNavHost(
                    uiState = uiState,
                    windowWidthSizeClass = windowWidthSizeClass,

                    onSignIn = authViewModel::signIn,
                    onRegister = authViewModel::register,
                    onResetPassword = authViewModel::resetPassword,
                    onDismissMessage = authViewModel::clearMessages,

                    onSignOut = onSignOut,

                    onGoogleSignIn = onGoogleSignIn,
                    isGooglePickerOpen = googlePickerOpen,

                    galleryLayout = preferenceState.preferences.galleryLayout,
                    galleryLayoutReady = !preferenceState.isLoading,
                    onGalleryLayoutSelected = preferencesViewModel::setGalleryLayout,

                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}