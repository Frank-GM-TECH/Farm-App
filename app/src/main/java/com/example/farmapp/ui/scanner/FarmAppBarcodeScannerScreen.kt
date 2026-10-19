package com.example.farmapp.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * HU03 - Pantalla de escaneo con CameraX y ML Kit.
 *
 * Solicita permiso de cámara cuando el usuario
 * pulsa el botón correspondiente.
 *
 * Al reconocer un código, lo devuelve a través
 * de onBarcodeScanned.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmAppBarcodeScannerScreen(
    onBack: () -> Unit,
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            cameraPermissionGranted = granted
        }

    Scaffold(
        modifier = modifier.testTag("barcode_scanner_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Escanear producto") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Escáner de códigos de barras",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Apunta la cámara al código de barras " +
                        "de un producto para identificarlo.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (cameraPermissionGranted) {

                CameraBarcodePreview(
                    onBarcodeScanned = onBarcodeScanned,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

            } else {

                Text(
                    text = "FarmApp necesita permiso para utilizar " +
                            "la cámara y leer códigos de barras."
                )

                Button(
                    onClick = {
                        permissionLauncher.launch(
                            Manifest.permission.CAMERA
                        )
                    }
                ) {
                    Text("Permitir acceso a la cámara")
                }
            }
        }
    }
}

/**
 * Vista de cámara.
 *
 * CameraX proporciona los fotogramas y ML Kit
 * detecta los códigos de barras.
 *
 * Se cierran los recursos al salir.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraBarcodePreview(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnBarcodeScanned by rememberUpdatedState(
        onBarcodeScanned
    )

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE

            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(
        context,
        lifecycleOwner,
        previewView
    ) {
        val mainExecutor =
            ContextCompat.getMainExecutor(context)

        val analysisExecutor =
            Executors.newSingleThreadExecutor()

        val disposed = AtomicBoolean(false)
        val detected = AtomicBoolean(false)

        val scannerOptions =
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39
                )
                .build()

        val barcodeScanner =
            BarcodeScanning.getClient(scannerOptions)

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        var cameraProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({

            try {
                if (disposed.get()) {
                    return@addListener
                }

                val provider = cameraProviderFuture.get()

                if (disposed.get()) {
                    return@addListener
                }

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider =
                            previewView.surfaceProvider
                    }

                val imageAnalysis =
                    ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis
                                .STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build()

                imageAnalysis.setAnalyzer(
                    analysisExecutor
                ) { imageProxy ->

                    val mediaImage = imageProxy.image

                    if (
                        mediaImage == null ||
                        detected.get() ||
                        disposed.get()
                    ) {
                        imageProxy.close()
                    } else {

                        try {
                            val inputImage =
                                InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy
                                        .imageInfo
                                        .rotationDegrees
                                )

                            barcodeScanner
                                .process(inputImage)
                                .addOnSuccessListener { barcodes ->

                                    val code = barcodes
                                        .firstNotNullOfOrNull {
                                            it.rawValue?.takeIf(
                                                String::isNotBlank
                                            )
                                        }

                                    if (
                                        code != null &&
                                        !disposed.get() &&
                                        detected.compareAndSet(
                                            false,
                                            true
                                        )
                                    ) {
                                        mainExecutor.execute {
                                            if (!disposed.get()) {
                                                currentOnBarcodeScanned(
                                                    code
                                                )
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { error ->
                                    Log.e(
                                        "FarmAppScanner",
                                        "Error leyendo código",
                                        error
                                    )
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }

                        } catch (error: Exception) {

                            Log.e(
                                "FarmAppScanner",
                                "Error procesando fotograma",
                                error
                            )

                            imageProxy.close()
                        }
                    }
                }

                provider.unbindAll()

                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )

                cameraProvider = provider

            } catch (error: Exception) {

                Log.e(
                    "FarmAppScanner",
                    "No se pudo iniciar la cámara",
                    error
                )
            }

        }, mainExecutor)

        onDispose {
            disposed.set(true)

            cameraProvider?.unbindAll()

            barcodeScanner.close()

            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.testTag(
            "barcode_camera_preview"
        )
    )
}