package com.example.farmapp.catalog

/**
 * Cotejo 18. Política de contenido, independiente de Navigation Compose.
 * El ancho recibido es el realmente disponible para el contenido (descontado
 * el drawer/rail, si existe). Evita dos paneles estrechos en tablets pequeñas.
 */
enum class CatalogContentLayout { SINGLE_PANE, LIST_DETAIL }

const val MIN_TWO_PANE_CONTENT_DP = 720f

fun catalogContentLayoutFor(availableWidthDp: Float): CatalogContentLayout =
    if (availableWidthDp.isFinite() && availableWidthDp >= MIN_TWO_PANE_CONTENT_DP)
        CatalogContentLayout.LIST_DETAIL
    else CatalogContentLayout.SINGLE_PANE
