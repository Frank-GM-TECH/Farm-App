package com.example.farmapp.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogAnalyzerTest {
    private val analyzer = CatalogAnalyzer(DemoCatalogData.repository)

    @Test fun listaDemoTieneSeisProductosUnicos() {
        val items = DemoCatalogData.products
        assertEquals(6, items.size)
        assertEquals(6, items.map { it.sku }.toSet().size)
    }
    @Test fun busquedaIgnoraMayusculasYEspacios() {
        assertEquals("FA-006", analyzer.search("  TERMÓMETRO ").products.single().sku)
    }
    @Test fun filtraPorCategoria() {
        val result = analyzer.search(category = DemoCategory.PRIMEROS_AUXILIOS)
        assertEquals(2, result.products.size)
        assertTrue(result.products.all { it.category == DemoCategory.PRIMEROS_AUXILIOS })
    }
    @Test fun filtraPorNombreYCategoriaJuntos() {
        assertEquals(1, analyzer.search("gasas", DemoCategory.PRIMEROS_AUXILIOS).products.size)
        assertTrue(analyzer.search("gasas", DemoCategory.HIGIENE).products.isEmpty())
    }
    @Test fun ordenaAlfabeticamente() {
        val names = analyzer.search().products.map { it.name.lowercase() }
        assertEquals(names.sorted(), names)
    }
    @Test fun agrupaEnTresCategorias() {
        val result = analyzer.search()
        assertEquals(3, result.categories.size)
        assertEquals(3, result.grouped.size)
    }
    @Test fun sumaExistenciasConFold() {
        assertEquals(34, analyzer.search().totalUnits)
        assertEquals(19, analyzer.search(category = DemoCategory.HIGIENE).totalUnits)
    }
    @Test fun mapaContieneSumasPorCategoria() {
        val result = analyzer.search()
        assertEquals(19, result.unitsByCategory[DemoCategory.HIGIENE])
        assertEquals(7, result.unitsByCategory[DemoCategory.PRIMEROS_AUXILIOS])
        assertEquals(8, result.unitsByCategory[DemoCategory.EQUIPOS])
    }
    @Test fun calculaValorMonetarioSinDecimalesFlotantes() {
        val expected = DemoCatalogData.products.sumOf { it.stock.toLong() * it.priceCents }
        assertEquals(expected, analyzer.search().totalInventoryValueCents)
    }
    @Test fun distingueAgotadoYBajoStock() {
        assertFalse(DemoCatalogData.products.first { it.sku == "FA-003" }.isAvailable())
        assertEquals(StockStatus.AGOTADO, DemoStockPolicy.status(0))
        assertEquals(StockStatus.BAJO, DemoStockPolicy.status(5))
        assertEquals(StockStatus.DISPONIBLE, DemoStockPolicy.status(6))
    }
    @Test fun normalizaSkuConCompanionObject() {
        assertEquals("FA-099", DemoProduct.normalizedSku(" fa-099 "))
    }
    @Test(expected = IllegalArgumentException::class)
    fun rechazaStockNegativo() {
        DemoProduct("X", "Demo", DemoCategory.HIGIENE, 50, -1)
    }
    @Test fun busquedaVaciaDevuelveTodoYBusquedasSinCoincidenciaVacias() {
        assertEquals(6, analyzer.search("  ").products.size)
        assertTrue(analyzer.search("no existe").products.isEmpty())
    }
}
