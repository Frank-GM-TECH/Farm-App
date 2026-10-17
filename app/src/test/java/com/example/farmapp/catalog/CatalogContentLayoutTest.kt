package com.example.farmapp.catalog

import org.junit.Assert.assertEquals
import org.junit.Test

/** Casos de límite medidos en el espacio REAL de contenido, no ancho total. */
class CatalogContentLayoutTest {
    @Test fun telefonoUsaUnPanel() =
        assertEquals(CatalogContentLayout.SINGLE_PANE, catalogContentLayoutFor(390f))

    @Test fun tabletConDrawerNoFuerzaDosPanelesEstrechos() =
        assertEquals(CatalogContentLayout.SINGLE_PANE, catalogContentLayoutFor(600f))

    @Test fun bajoUmbralUnPanel() =
        assertEquals(CatalogContentLayout.SINGLE_PANE, catalogContentLayoutFor(719.9f))

    @Test fun enUmbralDosPaneles() =
        assertEquals(CatalogContentLayout.LIST_DETAIL, catalogContentLayoutFor(720f))

    @Test fun tabletAnchaDosPaneles() =
        assertEquals(CatalogContentLayout.LIST_DETAIL, catalogContentLayoutFor(1040f))

    @Test fun anchoNoFinitoNoGeneraLayoutInvalido() =
        assertEquals(CatalogContentLayout.SINGLE_PANE, catalogContentLayoutFor(Float.POSITIVE_INFINITY))
}
