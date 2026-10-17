package com.example.farmapp.data.background

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdvancedInventoryReportWorkerTest {
    @Test fun prepararRechazaFuenteInvalida() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<PrepareInventoryReportWorker>(context)
            .setInputData(workDataOf(ReportWorkKeys.SOURCE to "origen_incorrecto"))
            .build()
        assertTrue(worker.doWork() is ListenableWorker.Result.Failure)
    }

    @Test fun guardarRechazaResumenConStockNegativo() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<SaveInventoryReportWorker>(context)
            .setInputData(workDataOf(
                ReportWorkKeys.COUNT to 2,
                ReportWorkKeys.UNITS to -3L,
                ReportWorkKeys.ZERO_STOCK to 0,
                ReportWorkKeys.VALUE_CENTS to 100L
            ))
            .build()
        assertTrue(worker.doWork() is ListenableWorker.Result.Failure)
    }

    @Test fun guardarRechazaCantidadAgotadosMayorAlTotal() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<SaveInventoryReportWorker>(context)
            .setInputData(workDataOf(
                ReportWorkKeys.COUNT to 1,
                ReportWorkKeys.UNITS to 0L,
                ReportWorkKeys.ZERO_STOCK to 2,
                ReportWorkKeys.VALUE_CENTS to 0L
            ))
            .build()
        assertTrue(worker.doWork() is ListenableWorker.Result.Failure)
    }
}
