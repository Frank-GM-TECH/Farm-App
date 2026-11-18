
package com.example.farmapp.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,

    // Estado de subida de fotografías.
    val isUploadingPhoto: Boolean = false,
    val photoUploadSuccess: Boolean = false,

    // Fotografía recuperada desde Firebase Storage.
    val savedPhotoBytes: ByteArray? = null,
    val isLoadingPhoto: Boolean = false,
    val photoLoadError: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val auth: FirebaseAuth,
    private val photoStorage: FirebaseProfilePhotoStorage,
    context: Context
) : ViewModel() {

    private val appContext = context.applicationContext

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    // ========================================
    // CARGAR PERFIL Y FOTOGRAFÍA GUARDADA
    // ========================================

    fun loadProfile() {

        val user = auth.currentUser ?: run {
            _uiState.value = ProfileUiState(
                errorMessage = "Debes iniciar sesión."
            )
            return
        }

        val userId = user.uid

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingPhoto = false,
                    errorMessage = null,
                    photoLoadError = null,
                    savedPhotoBytes = null,
                    saveSuccess = false
                )
            }

            try {

                // 1. Recuperar el documento del perfil.
                val profile = repository.createProfileIfMissing(
                    userId = userId,
                    displayName = user.displayName.orEmpty()
                )

                check(auth.currentUser?.uid == userId) {
                    "La sesión cambió durante la carga."
                }

                _uiState.update {
                    it.copy(
                        profile = profile,
                        isLoading = false
                    )
                }

                // 2. Descargar la fotografía si tiene una ruta.
                if (profile.photoUrl.isNotBlank()) {

                    _uiState.update {
                        it.copy(
                            isLoadingPhoto = true,
                            photoLoadError = null
                        )
                    }

                    try {

                        val photoBytes = photoStorage.readAvatar()

                        check(auth.currentUser?.uid == userId) {
                            "La sesión cambió durante la descarga."
                        }

                        _uiState.update {
                            it.copy(
                                savedPhotoBytes = photoBytes,
                                isLoadingPhoto = false
                            )
                        }

                    } catch (e: CancellationException) {
                        throw e

                    } catch (e: Exception) {

                        Log.e(
                            "FarmAppPhoto",
                            "Error al recuperar fotografía",
                            e
                        )

                        _uiState.update {
                            it.copy(
                                isLoadingPhoto = false,
                                photoLoadError =
                                    "No se pudo cargar la fotografía."
                            )
                        }
                    }
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "FarmAppProfile",
                    "Error al cargar perfil",
                    e
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingPhoto = false,
                        errorMessage =
                            "No se pudo cargar el perfil."
                    )
                }
            }
        }
    }

    // ========================================
    // GUARDAR DATOS PERSONALES
    // ========================================

    fun saveProfile(
        displayName: String,
        phone: String,
        address: String
    ) {

        val userId = auth.currentUser?.uid ?: run {
            _uiState.update {
                it.copy(
                    errorMessage = "Debes iniciar sesión."
                )
            }
            return
        }

        if (
            _uiState.value.isSaving ||
            _uiState.value.isUploadingPhoto
        ) return

        if (_uiState.value.profile == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "Primero carga tu perfil."
                )
            }
            return
        }

        if (displayName.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Ingresa tu nombre."
                )
            }
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    saveSuccess = false
                )
            }

            try {

                repository.updateProfile(
                    userId = userId,
                    displayName = displayName,
                    phone = phone,
                    address = address
                )

                check(auth.currentUser?.uid == userId) {
                    "La sesión cambió durante el guardado."
                }

                _uiState.update {
                    it.copy(
                        profile = it.profile?.copy(
                            displayName = displayName.trim(),
                            phone = phone.trim(),
                            address = address.trim()
                        ),
                        isSaving = false,
                        saveSuccess = true
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "FarmAppProfile",
                    "Error al actualizar perfil",
                    e
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage =
                            "No se pudieron guardar los cambios."
                    )
                }
            }
        }
    }

    // ========================================
    // SUBIR FOTOGRAFÍA DE PERFIL
    // ========================================

    fun uploadProfilePhoto(uri: Uri) {

        val userId = auth.currentUser?.uid ?: run {
            _uiState.update {
                it.copy(
                    errorMessage = "Debes iniciar sesión."
                )
            }
            return
        }

        if (
            _uiState.value.isUploadingPhoto ||
            _uiState.value.isSaving
        ) return

        if (_uiState.value.profile == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "Primero carga tu perfil."
                )
            }
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isUploadingPhoto = true,
                    photoUploadSuccess = false,
                    errorMessage = null,
                    photoLoadError = null
                )
            }

            try {

                // 1. Convertir la foto seleccionada a JPEG.
                val jpegBytes = encodeProfilePhotoJpeg(
                    context = appContext,
                    uri = uri
                )

                check(auth.currentUser?.uid == userId) {
                    "La sesión cambió antes de subir."
                }

                // 2. Subir a Firebase Storage.
                val storagePath = photoStorage.uploadAvatar(
                    jpegBytes
                )

                check(auth.currentUser?.uid == userId) {
                    "La sesión cambió durante la subida."
                }

                // 3. Registrar la ruta en Firestore.
                repository.updatePhotoUrl(
                    userId = userId,
                    photoUrl = storagePath
                )

                check(auth.currentUser?.uid == userId) {
                    "La sesión cambió al guardar la ruta."
                }

                // 4. Mostrar inmediatamente la foto nueva.
                _uiState.update {
                    it.copy(
                        profile = it.profile?.copy(
                            photoUrl = storagePath
                        ),
                        savedPhotoBytes = jpegBytes,
                        isLoadingPhoto = false,
                        isUploadingPhoto = false,
                        photoUploadSuccess = true
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "FarmAppPhoto",
                    "Error al subir o guardar fotografía",
                    e
                )

                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        photoUploadSuccess = false,
                        errorMessage =
                            "No se pudo guardar la fotografía."
                    )
                }
            }
        }
    }

    // ========================================
    // LIMPIAR MENSAJES
    // ========================================

    fun clearMessage() {

        _uiState.update {
            it.copy(
                errorMessage = null,
                photoLoadError = null,
                saveSuccess = false,
                photoUploadSuccess = false
            )
        }
    }
}