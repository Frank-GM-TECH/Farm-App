package com.example.farmapp.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.farmapp.FarmAppApplication
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileRoute(
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext
            as FarmAppApplication

    val profileViewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory {
            initializer {

                ProfileViewModel(
                    repository = application.container.profileRepository,
                    auth = FirebaseAuth.getInstance(),
                    photoStorage = FirebaseProfilePhotoStorage(),
                    context = application.applicationContext
                )

            }
        }
    )

    val uiState by profileViewModel.uiState
        .collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        onSave = profileViewModel::saveProfile,
        onBack = onBack,
        onRetry = profileViewModel::loadProfile,
        onDismissMessage = profileViewModel::clearMessage,
        onUploadPhoto = profileViewModel::uploadProfilePhoto
    )
}
