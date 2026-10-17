package com.example.farmapp.data.background

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.WorkInfo
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.Constraints
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AdvancedInventoryReportWorkTest {
    @Test fun solicitudesIncluyenEtiquetasUnicasYRestriccionesAdecuadas() {
        val plan = InventoryReportWorkPlan.create()
        val first = plan.prepare
        val second = plan.save
        val tags1 = first.tags
        val tags2 = second.tags
        assertTrue(ReportWorkKeys.TAG_PREPARE in tags1)
        assertTrue(ReportWorkKeys.TAG_SAVE in tags2)
        assertTrue(ReportWorkKeys.TAG_REPORT in tags1)
        assertTrue(ReportWorkKeys.TAG_REPORT in tags2)
        assertTrue(first.workSpec.constraints.requiresBatteryNotLow())
        assertTrue(second.workSpec.constraints.requiresStorageNotLow())
        assertEquals(ReportWorkKeys.SOURCE_VALUE,
            first.workSpec.input.getString(ReportWorkKeys.SOURCE))
        assertEquals(
            tags1.first { it.startsWith(ReportWorkKeys.RUN_TAG_PREFIX) },
            tags2.first { it.startsWith(ReportWorkKeys.RUN_TAG_PREFIX) }
        )
    }

    /** Muestra el TestDriver sin consultar la base Room ni crear archivos. */
    @Test fun testDriverPermiteSimularUnaRestriccion() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        val manager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<ConstraintProbeWorker>()
            .setConstraints(Constraints.Builder().setRequiresCharging(true).build())
            .build()
        manager.enqueueUniqueWork(
            "test_probe_${UUID.randomUUID()}", ExistingWorkPolicy.KEEP, request
        ).result.get(10, TimeUnit.SECONDS)
        val driver = WorkManagerTestInitHelper.getTestDriver(context)
        assertNotNull(driver)
        driver!!.setAllConstraintsMet(request.id)
        // El planificador de pruebas ejecuta la tarea al cumplirse la restricción.
        val info = manager.getWorkInfoById(request.id).get(10, TimeUnit.SECONDS)
        assertEquals(WorkInfo.State.SUCCEEDED, info?.state)
    }
}

/** Worker aislado de prueba, no utilizado por FarmApp en producción. */
class ConstraintProbeWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result = Result.success()
}
