package com.example.farmapp.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun ProfilePhotoPicker(
    onPhotoSelected: (Uri) -> Unit,
    enabled: Boolean = true
) {
    val currentOnPhotoSelected by rememberUpdatedState(
        onPhotoSelected
    )

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            currentOnPhotoSelected(uri)
        }
    }

    OutlinedButton(
        onClick = {
            photoPicker.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts
                        .PickVisualMedia
                        .ImageOnly
                )
            )
        },
        enabled = enabled
    ) {
        Text("Seleccionar foto de la galería")
    }
}
