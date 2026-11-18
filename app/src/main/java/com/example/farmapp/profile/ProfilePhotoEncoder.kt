package com.example.farmapp.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

private const val MAX_IMAGE_SIDE = 1280
private const val MAX_IMAGE_BYTES = 5 * 1024 * 1024

/**
 * HU02: Lee la orientacion EXIF de una foto seleccionada, la aplica a los
 * pixeles y genera un JPEG de hasta 5 MiB para Firebase Storage.
 *
 * Importante: el JPEG generado ya queda orientado correctamente, sin depender
 * de que Firebase o el visor interpreten los metadatos de orientacion.
 */
suspend fun encodeProfilePhotoJpeg(
    context: Context,
    uri: Uri
): ByteArray = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver

    // AndroidX ExifInterface indica si debemos girar o reflejar la imagen.
    // Algunas imagenes no incluyen EXIF: en ese caso se asume orientacion normal.
    val orientation = try {
        resolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            exif.getRotationDegrees() to exif.isFlipped()
        } ?: (0 to false)
    } catch (e: IOException) {
        0 to false
    }
    val (rotationDegrees, isFlipped) = orientation

    // Leer dimensiones sin crear todavia un Bitmap completo.
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    val boundsInput = resolver.openInputStream(uri)
        ?: error("No se pudo abrir la imagen.")
    boundsInput.use { stream ->
        // Con inJustDecodeBounds = true, decodeStream devuelve null normalmente.
        BitmapFactory.decodeStream(stream, null, bounds)
    }
    require(bounds.outWidth > 0 && bounds.outHeight > 0) {
        "El archivo seleccionado no es una imagen valida."
    }

    // Decodificacion reducida para no ocupar demasiada memoria.
    var sampleSize = 1
    while (
        bounds.outWidth / sampleSize > MAX_IMAGE_SIDE ||
        bounds.outHeight / sampleSize > MAX_IMAGE_SIDE
    ) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    }
    val decoded = resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    } ?: error("No se pudo decodificar la fotografia.")

    var oriented: Bitmap = decoded
    var resized: Bitmap = decoded
    try {
        if (rotationDegrees != 0 || isFlipped) {
            val transform = Matrix().apply {
                // El contrato de ExifInterface aplica primero el reflejo y luego el giro.
                if (isFlipped) postScale(-1f, 1f)
                if (rotationDegrees != 0) postRotate(rotationDegrees.toFloat())
            }
            oriented = Bitmap.createBitmap(
                decoded, 0, 0, decoded.width, decoded.height, transform, true
            )
        }

        // Ajustar a 1280 px como maximo, conservando la proporcion.
        val maxDimension = maxOf(oriented.width, oriented.height)
        val scale = minOf(1f, MAX_IMAGE_SIDE.toFloat() / maxDimension)
        resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                oriented,
                (oriented.width * scale).toInt().coerceAtLeast(1),
                (oriented.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            oriented
        }

        ByteArrayOutputStream().use { output ->
            var quality = 90
            do {
                output.reset()
                check(resized.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
                    "No se pudo convertir la fotografia."
                }
                quality -= 10
            } while (output.size() > MAX_IMAGE_BYTES && quality >= 40)

            require(output.size() <= MAX_IMAGE_BYTES) {
                "La fotografia supera el limite de 5 MiB."
            }
            output.toByteArray()
        }
    } finally {
        // Reciclar cada Bitmap una sola vez, incluso si hay un error.
        if (resized !== oriented && resized !== decoded) resized.recycle()
        if (oriented !== decoded) oriented.recycle()
        decoded.recycle()
    }
}