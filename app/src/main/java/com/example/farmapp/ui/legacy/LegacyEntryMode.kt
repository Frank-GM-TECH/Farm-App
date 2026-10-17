package com.example.farmapp.ui.legacy

/** C30. ID 0 crea un artículo; los IDs positivos abren su edición. */
enum class LegacyEntryMode { NEW, EDIT }

fun legacyEntryModeFor(id: Long): LegacyEntryMode {
    require(id >= 0L) { "El ID no puede ser negativo" }
    return if (id == 0L) LegacyEntryMode.NEW else LegacyEntryMode.EDIT
}
