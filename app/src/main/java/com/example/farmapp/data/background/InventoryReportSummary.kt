package com.example.farmapp.data.background

import com.example.farmapp.data.room.RoomDemoItem

/** C28: pura lógica de dominio; sin Context, Worker ni acceso a Internet. */
data class InventoryReportSummary(
    val productCount: Int,
    val totalUnits: Long,
    val outOfStock: Int,
    val totalValueCents: Long
) {
    companion object {
        fun from(items: List<RoomDemoItem>): InventoryReportSummary = InventoryReportSummary(
            productCount = items.size,
            totalUnits = items.sumOf { it.stock.toLong() },
            outOfStock = items.count { it.stock == 0 },
            totalValueCents = items.sumOf { it.priceCents * it.stock.toLong() }
        )
    }
}

/** Archivo sin datos personales ni historial de ventas, limitado al Room de demostración. */
fun reportText(summary: InventoryReportSummary, generatedAtUtc: String): String = buildString {
    appendLine("FarmApp - Informe educativo de inventario")
    appendLine("Origen: laboratorio Room (datos ficticios)")
    appendLine("Fecha UTC: $generatedAtUtc")
    appendLine("Artículos: ${summary.productCount}")
    appendLine("Unidades: ${summary.totalUnits}")
    appendLine("Sin existencias: ${summary.outOfStock}")
    appendLine("Valor estimado en céntimos de sol: ${summary.totalValueCents}")
    appendLine("No representa existencias, ventas ni precios reales.")
}
