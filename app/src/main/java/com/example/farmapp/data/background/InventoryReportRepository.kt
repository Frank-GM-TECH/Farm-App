package com.example.farmapp.data.background

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.UUID
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ReportPhase { IDLE, QUEUED, PREPARING, SAVING, COMPLETED, FAILED, CANCELLED }

data class ReportWorkUiState(
    val phase: ReportPhase = ReportPhase.IDLE,
    val fileName: String? = null
)

interface InventoryReportRepository {
    val status: Flow<ReportWorkUiState>
    fun enqueueReport()
    fun cancelReport()
}

/** WorkManager se inyecta desde el contenedor, no se crea dentro de Compose. */
class WorkManagerInventoryReportRepository(context: Context) : InventoryReportRepository {
    private val manager = WorkManager.getInstance(context.applicationContext)

    override val status: Flow<ReportWorkUiState> = manager
        .getWorkInfosForUniqueWorkFlow(ReportWorkKeys.UNIQUE_NAME)
        .map(::readReportStatus)

    override fun enqueueReport() {
        val plan = InventoryReportWorkPlan.create()
        // KEEP: si hay una cadena pendiente, no se reinicia accidentalmente.
        // Cuando termina, un nuevo clic puede crear otro informe.
        manager.beginUniqueWork(
            ReportWorkKeys.UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            plan.prepare
        ).then(plan.save).enqueue()
    }

    override fun cancelReport() {
        manager.cancelUniqueWork(ReportWorkKeys.UNIQUE_NAME)
    }
}

/**
 * Peticiones verificables sin conectarse a Room ni ejecutar un Worker.
 * No se requiere Internet porque los datos se encuentran en el dispositivo.
 */
internal data class ReportWorkPlan(
    val prepare: OneTimeWorkRequest,
    val save: OneTimeWorkRequest
)

internal object InventoryReportWorkPlan {
    fun create(): ReportWorkPlan {
        // Prefijo + marca de tiempo permite distinguir ejecuciones sucesivas.
        val runTag = ReportWorkKeys.RUN_TAG_PREFIX +
            System.currentTimeMillis().toString().padStart(13, '0') + "_" + UUID.randomUUID()
        val prepare = OneTimeWorkRequestBuilder<PrepareInventoryReportWorker>()
            .setInputData(workDataOf(ReportWorkKeys.SOURCE to ReportWorkKeys.SOURCE_VALUE))
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .addTag(ReportWorkKeys.TAG_REPORT)
            .addTag(ReportWorkKeys.TAG_PREPARE)
            .addTag(runTag)
            .build()
        val save = OneTimeWorkRequestBuilder<SaveInventoryReportWorker>()
            .setConstraints(Constraints.Builder().setRequiresStorageNotLow(true).build())
            .addTag(ReportWorkKeys.TAG_REPORT)
            .addTag(ReportWorkKeys.TAG_SAVE)
            .addTag(runTag)
            .build()
        return ReportWorkPlan(prepare, save)
    }
}

/**
 * Si existen trabajos de ejecuciones anteriores, se toma solamente la más
 * reciente. No se presupone un orden concreto en la lista de WorkInfo.
 */
internal fun mostRecentReportRunTag(tagSets: List<Set<String>>): String? =
    tagSets.asSequence().flatMap { it.asSequence() }
        .filter { it.startsWith(ReportWorkKeys.RUN_TAG_PREFIX) }
        .maxOrNull()

/** Conversión de WorkInfo al estado visual, sin depender del orden de la lista. */
fun readReportStatus(infos: List<WorkInfo>): ReportWorkUiState {
    if (infos.isEmpty()) return ReportWorkUiState()
    val latestTag = mostRecentReportRunTag(infos.map { it.tags })
    val current = latestTag?.let { tag -> infos.filter { tag in it.tags } } ?: infos
    val prepare = current.firstOrNull { ReportWorkKeys.TAG_PREPARE in it.tags }
    val save = current.firstOrNull { ReportWorkKeys.TAG_SAVE in it.tags }
    val states = listOfNotNull(prepare?.state, save?.state)
    if (WorkInfo.State.FAILED in states) return ReportWorkUiState(ReportPhase.FAILED)
    if (WorkInfo.State.CANCELLED in states) return ReportWorkUiState(ReportPhase.CANCELLED)
    if (save?.state == WorkInfo.State.SUCCEEDED) {
        return ReportWorkUiState(
            ReportPhase.COMPLETED,
            save.outputData.getString(ReportWorkKeys.FILE_NAME)
        )
    }
    if (save?.state == WorkInfo.State.RUNNING) return ReportWorkUiState(ReportPhase.SAVING)
    if (prepare?.state == WorkInfo.State.RUNNING) return ReportWorkUiState(ReportPhase.PREPARING)
    // Si el trabajo anterior ya terminó pero el segundo no ha empezado,
    // el usuario verá espera, no "preparando".
    return ReportWorkUiState(ReportPhase.QUEUED)
}
