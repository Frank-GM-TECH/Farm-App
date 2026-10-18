package com.example.farmapp.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun ProfilePhotoCameraPicker(
    onPhotoCaptured: (Uri) -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current

    var pendingUri by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->

        val capturedUri = pendingUri?.let(Uri::parse)
        pendingUri = null

        if (success && capturedUri != null) {
            onPhotoCaptured(capturedUri)
        }
    }

    OutlinedButton(
        onClick = {
            try {
                val photoDirectory = File(
                    context.cacheDir,
                    "profile_photos"
                )

                check(
                    photoDirectory.exists() ||
                            photoDirectory.mkdirs()
                ) {
                    "No se pudo crear la carpeta temporal."
                }

                val photoFile = File.createTempFile(
                    "farmapp_camera_",
                    ".jpg",
                    photoDirectory
                )

                val photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )

                pendingUri = photoUri.toString()

                cameraLauncher.launch(photoUri)

            } catch (e: Exception) {
                pendingUri = null

                Toast.makeText(
                    context,
                    "No se pudo abrir la cámara.",
                    Toast.LENGTH_LONG
                ).show()
            }
        },
        enabled = enabled,
        modifier = Modifier.testTag("profile_take_photo")
    ) {
        Text("Tomar fotografía")
    }
}
