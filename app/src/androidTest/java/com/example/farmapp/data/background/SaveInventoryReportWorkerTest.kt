package com.example.farmapp.data.background

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import android.content.Context
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.Test
import java.io.File

/** Prueba de escritura real en el directorio PRIVADO de la app, sin modificar Room. */
@RunWith(AndroidJUnit4::class)
class SaveInventoryReportWorkerTest {
    @Test fun reporteSeGuardaUsandoOutputDataDelWorkerAnterior() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<SaveInventoryReportWorker>(context)
            .setInputData(workDataOf(
                ReportWorkKeys.COUNT to 2,
                ReportWorkKeys.UNITS to 4L,
                ReportWorkKeys.ZERO_STOCK to 1,
                ReportWorkKeys.VALUE_CENTS to 1500L
            ))
            .build()
        val result = kotlinx.coroutines.runBlocking { worker.doWork() }
        assertTrue(result is ListenableWorker.Result.Success)
        val fileName = (result as ListenableWorker.Result.Success).outputData
            .getString(ReportWorkKeys.FILE_NAME)!!
        val file = File(File(context.filesDir, "reportes_demo"), fileName)
        assertTrue(file.exists())
        assertTrue(file.readText().contains("Unidades: 4"))
        file.delete()
    }
}
