package com.example.farmapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.farmapp.auth.GoogleCredentialSignIn
import com.example.farmapp.auth.MissingGoogleClientIdException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.auth.AuthViewModel
import com.example.farmapp.ui.components.ThemeModeSelector
import com.example.farmapp.ui.navigation.FarmAppNavHost
import com.example.farmapp.ui.theme.FarmAppTheme
import com.example.farmapp.ui.theme.FarmThemeMode
import com.example.farmapp.data.preferences.AppThemePreference
import com.example.farmapp.data.preferences.UserPreferencesViewModel
import androidx.compose.foundation.layout.statusBarsPadding
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat

/** Cotejo 16: tema + navegación separados de los destinos visuales. */
@Composable
fun FarmAppRoot(
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    preferencesViewModel: UserPreferencesViewModel = viewModel(factory = UserPreferencesViewModel.Factory)
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialSignIn = remember(context) {
        GoogleCredentialSignIn(CredentialManager.create(context))
    }
    var googlePickerOpen by remember { mutableStateOf(false) }
    val onGoogleSignIn: () -> Unit = googleSignIn@{
        if (googlePickerOpen || uiState.isLoading) return@googleSignIn
        googlePickerOpen = true
        scope.launch {
            try {
                // El selector de cuenta pertenece a la UI, no al ViewModel.
                val token = credentialSignIn.requestIdToken(context)
                authViewModel.signInWithGoogleIdToken(token)
            } catch (cancelled: GetCredentialCancellationException) {
                // La persona cerró el selector; sin error y sin iniciar sesión.
            } catch (missing: MissingGoogleClientIdException) {
                authViewModel.reportGoogleCredentialError(configurationMissing = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                authViewModel.reportGoogleCredentialError()
            } finally {
                googlePickerOpen = false
            }
        }
    }
    val onSignOut: () -> Unit = {
        authViewModel.signOut()
        scope.launch {
            try {
                credentialSignIn.clearCredentialState()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // La sesión Firebase ya se cerró; el estado del gestor es auxiliar.
            }
        }
    }
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

    FarmAppTheme(mode = selectedTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                ThemeModeSelector(
                    selectedMode = selectedTheme,
                    onModeSelected = { preferencesViewModel.setTheme(AppThemePreference.valueOf(it.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !preferenceState.isLoading
                )
                if (preferenceState.hasError) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.pref_error),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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
