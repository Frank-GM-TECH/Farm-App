package com.example.farmapp.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * HU02 - Gestión de perfiles.
 *
 * Permite:
 * - Consultar y editar datos personales.
 * - Mostrar el rol del usuario.
 * - Seleccionar fotografía desde galería.
 * - Tomar fotografía con la cámara.
 * - Mostrar vista previa.
 * - Subir fotografía a Firebase Storage.
 * - Recuperar fotografía guardada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onDismissMessage: () -> Unit,
    onUploadPhoto: (Uri) -> Unit = {},
    modifier: Modifier = Modifier
) {

    val profile = uiState.profile

    // URI de una foto seleccionada o tomada con cámara.
    var selectedPhotoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // Campos editables.
    var name by rememberSaveable(profile?.displayName) {
        mutableStateOf(profile?.displayName.orEmpty())
    }

    var phone by rememberSaveable(profile?.phone) {
        mutableStateOf(profile?.phone.orEmpty())
    }

    var address by rememberSaveable(profile?.address) {
        mutableStateOf(profile?.address.orEmpty())
    }

    // Tras una subida exitosa, mostrar la foto guardada.
    LaunchedEffect(uiState.photoUploadSuccess) {
        if (uiState.photoUploadSuccess) {
            selectedPhotoUri = null
        }
    }

    // Decodificar la fotografía recuperada desde Storage.
    val savedPhoto = remember(uiState.savedPhotoBytes) {
        uiState.savedPhotoBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )?.asImageBitmap()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi perfil")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("profile_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Información personal",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Consulta y actualiza tus datos de FarmApp.",
                style = MaterialTheme.typography.bodyMedium
            )

            // Carga inicial del perfil.
            if (uiState.isLoading && profile == null) {
                CircularProgressIndicator()
                Text("Cargando perfil...")
            }

            // Errores generales.
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )

                TextButton(
                    onClick = onDismissMessage
                ) {
                    Text("Cerrar mensaje")
                }
            }

            // Reintentar carga.
            if (profile == null && !uiState.isLoading) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.testTag("profile_retry")
                ) {
                    Text("Reintentar")
                }
            }

            if (profile != null) {

                // =================================
                // DATOS PERSONALES
                // =================================

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text("Nombre completo")
                    },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = {
                        Text("Teléfono")
                    },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_phone")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = {
                        Text("Dirección de despacho")
                    },
                    minLines = 2,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_address")
                )

                HorizontalDivider()

                Text(
                    text = "Rol asignado: ${profile.role}",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "El rol no puede modificarse desde el perfil.",
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider()

                // =================================
                // FOTOGRAFÍA DE PERFIL
                // =================================

                Text(
                    text = "Fotografía de perfil",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Selecciona una imagen de tu galería o toma una fotografía.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // =================================
                // VISTA PREVIA
                // =================================

                when {

                    // Imagen recién seleccionada o capturada.
                    selectedPhotoUri != null -> {

                        AsyncImage(
                            model = selectedPhotoUri,
                            contentDescription =
                                "Vista previa de fotografía",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .testTag("profile_photo_preview")
                        )

                        Text(
                            "Fotografía lista para subir a Firebase."
                        )
                    }

                    // Imagen recuperada desde Firebase.
                    savedPhoto != null -> {

                        Image(
                            bitmap = savedPhoto,
                            contentDescription =
                                "Fotografía de perfil guardada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .testTag("profile_saved_photo")
                        )

                        Text(
                            "Fotografía guardada en Firebase Storage"
                        )
                    }

                    // Descargando desde Firebase.
                    uiState.isLoadingPhoto -> {

                        CircularProgressIndicator(
                            modifier = Modifier.testTag(
                                "profile_photo_loading"
                            )
                        )

                        Text("Cargando fotografía guardada...")
                    }

                    else -> {
                        Text(
                            "Todavía no se muestra una fotografía guardada."
                        )
                    }
                }

                // Error al descargar foto.
                uiState.photoLoadError?.let { message ->

                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(onClick = onRetry) {
                        Text("Reintentar cargar fotografía")
                    }
                }

                // =================================
                // OPCIÓN 1: GALERÍA
                // =================================

                ProfilePhotoPicker(
                    onPhotoSelected = { uri ->
                        onDismissMessage()
                        selectedPhotoUri = uri
                    },
                    enabled = !uiState.isSaving &&
                            !uiState.isUploadingPhoto
                )

                // =================================
                // OPCIÓN 2: CÁMARA
                // =================================

                ProfilePhotoCameraPicker(
                    onPhotoCaptured = { uri ->
                        onDismissMessage()
                        selectedPhotoUri = uri
                    },
                    enabled = !uiState.isSaving &&
                            !uiState.isUploadingPhoto
                )

                // =================================
                // SUBIR FOTO A FIREBASE
                // =================================

                if (selectedPhotoUri != null) {

                    Button(
                        onClick = {
                            selectedPhotoUri?.let(onUploadPhoto)
                        },
                        enabled = !uiState.isUploadingPhoto &&
                                !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_upload_photo")
                    ) {

                        if (uiState.isUploadingPhoto) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text("Subir fotografía")
                        }
                    }
                }

                // Confirmación de subida.
                if (uiState.photoUploadSuccess) {

                    Text(
                        text = "Fotografía guardada correctamente en Firebase.",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag(
                            "profile_photo_success"
                        )
                    )
                }

                HorizontalDivider()

                // =================================
                // GUARDAR DATOS PERSONALES
                // =================================

                if (uiState.saveSuccess) {

                    Text(
                        text = "Perfil actualizado correctamente.",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = {
                        onSave(
                            name,
                            phone,
                            address
                        )
                    },
                    enabled = !uiState.isSaving &&
                            !uiState.isUploadingPhoto &&
                            name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_save_button")
                ) {

                    if (uiState.isSaving) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text("Guardar cambios")
                    }
                }
            }
        }
    }
}
