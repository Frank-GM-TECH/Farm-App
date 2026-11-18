package com.example.farmapp.ui.catalog

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag

@Composable
fun FarmAppVoiceSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar productos...",
    testTag: String = "voice_product_search",
    showSearchIcon: Boolean = false,
    onVoiceResult: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val spokenText = result.data
                ?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                ?.firstOrNull()
                ?.trim()

            if (!spokenText.isNullOrBlank()) {

                if (onVoiceResult != null) {
                    onVoiceResult(spokenText)
                } else {
                    onValueChange(spokenText)
                }
            }
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        placeholder = {
            Text(placeholder)
        },
        leadingIcon = if (showSearchIcon) {
            {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
        } else {
            null
        },
        trailingIcon = {
            IconButton(
                onClick = {

                    val intent = Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                    ).apply {

                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )

                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE,
                            "es-PE"
                        )

                        putExtra(
                            RecognizerIntent.EXTRA_PROMPT,
                            "Di el producto que buscas"
                        )
                    }

                    try {
                        speechLauncher.launch(intent)

                    } catch (e: ActivityNotFoundException) {

                        Toast.makeText(
                            context,
                            "Reconocimiento de voz no disponible",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Buscar por voz",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}