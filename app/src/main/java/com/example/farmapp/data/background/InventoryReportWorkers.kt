package com.example.farmapp.data.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import androidx.work.workDataOf
import com.example.farmapp.FarmAppApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Claves pequeñas; WorkManager Data NO debe usarse para archivos ni grandes listas. */
object ReportWorkKeys {
    const val UNIQUE_NAME = "farmapp_room_report_demo"
    const val TAG_REPORT = "farmapp_report_demo"
    const val RUN_TAG_PREFIX = "farmapp_report_run_"
    const val TAG_PREPARE = "farmapp_report_prepare"
    const val TAG_SAVE = "farmapp_report_save"
    const val SOURCE = "source"
    const val SOURCE_VALUE = "room_demo"
    const val COUNT = "report_count"
    const val UNITS = "report_units"
    const val ZERO_STOCK = "report_zero_stock"
    const val VALUE_CENTS = "report_value_cents"
    const val FILE_NAME = "report_filename"
}

/** Primera etapa: recibe un argumento de entrada y obtiene un resumen de la base Room. */
class PrepareInventoryReportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (inputData.getString(ReportWorkKeys.SOURCE) != ReportWorkKeys.SOURCE_VALUE) {
            return Result.failure()
        }
        return try {
            val app = applicationContext as FarmAppApplication
            val records = app.container.roomDemoRepository.items.first()
            val summary = InventoryReportSummary.from(records)
            Result.success(workDataOf(
                ReportWorkKeys.COUNT to summary.productCount,
                ReportWorkKeys.UNITS to summary.totalUnits,
                ReportWorkKeys.ZERO_STOCK to summary.outOfStock,
                ReportWorkKeys.VALUE_CENTS to summary.totalValueCents
            ))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Result.failure()
        }
    }
}

/** Segunda etapa: recibe el output de la primera y lo guarda en almacenamiento privado. */
class SaveInventoryReportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val count = inputData.getInt(ReportWorkKeys.COUNT, -1)
        val units = inputData.getLong(ReportWorkKeys.UNITS, -1L)
        val zero = inputData.getInt(ReportWorkKeys.ZERO_STOCK, -1)
        val value = inputData.getLong(ReportWorkKeys.VALUE_CENTS, -1L)
        if (count < 0 || units < 0L || zero < 0 || value < 0L || zero > count) {
            return Result.failure()
        }

        val summary = InventoryReportSummary(count, units, zero, value)
        var temp: File? = null
        return try {
            val destinationDir = File(applicationContext.filesDir, "reportes_demo")
            if (!destinationDir.exists() && !destinationDir.mkdirs()) return Result.failure()
            val filename = "informe_room_${System.currentTimeMillis()}.txt"
            val destination = File(destinationDir, filename)
            temp = File.createTempFile("reporte_en_curso_", ".tmp", destinationDir)
            val utc = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())
            temp.writeText(reportText(summary, utc), Charsets.UTF_8)
            if (isStopped) return Result.failure()
            if (!temp.renameTo(destination)) throw IOException("No se pudo completar el archivo")
            Result.success(workDataOf(ReportWorkKeys.FILE_NAME to filename))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: IOException) {
            Result.failure()
        } finally {
            temp?.takeIf { it.exists() }?.delete()
        }
    }
}
