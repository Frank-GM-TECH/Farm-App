package com.example.farmapp.ui.catalog

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.catalog.DemoCatalogData
import com.example.farmapp.catalog.DemoCategory
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.catalog.DemoStockPolicy
import com.example.farmapp.catalog.StockStatus
import com.example.farmapp.catalog.isAvailable
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * Cotejo 11: uso real de animateContentSize + spring en cada Card del catálogo.
 * El estado expanded pertenece a cada tarjeta y se vincula a su SKU estable.
 * Conserva el resumen existente; solo agrega detalles educativos opcionales.
 */
@Composable
fun AnimatedProductDemoCard(
    product: DemoProduct,
    modifier: Modifier = Modifier,
    onOpenProduct: ((DemoProduct) -> Unit)? = null
) {
    var expanded by rememberSaveable(product.sku) { mutableStateOf(false) }
    val stockText = when (DemoStockPolicy.status(product.stock)) {
        StockStatus.AGOTADO -> stringResource(R.string.catalog_stock_empty)
        StockStatus.BAJO -> stringResource(R.string.catalog_stock_low)
        StockStatus.DISPONIBLE -> stringResource(R.string.catalog_stock_available)
    }

    val expansionState = stringResource(
        if (expanded) R.string.a11y_details_expanded else R.string.a11y_details_collapsed
    )
    Card(
        modifier = modifier.fillMaxWidth().testTag("catalog_product_${product.sku}"),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Datos ya presentes antes del cotejo 11: se mantienen visibles.
            Text(product.name, style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.catalog_sku_value, product.sku))
            Text(stringResource(R.string.catalog_price_value, product.priceCents / 100.0))
            Text(stringResource(R.string.catalog_stock_value, product.stock, stockText))
            if (!product.isAvailable()) {
                Text(
                    stringResource(R.string.catalog_unavailable_note),
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("catalog_toggle_${product.sku}")
                    .semantics { stateDescription = expansionState }
            ) {
                Text(stringResource(if (expanded) R.string.catalog_hide_details else R.string.catalog_show_details))
            }

            if (onOpenProduct != null) {
                OutlinedButton(
                    onClick = { onOpenProduct(product) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("navigate_to_product_${product.sku}")
                ) {
                    Text(stringResource(R.string.nav_view_product))
                }
            }

            // La composición cambia de tamaño y animateContentSize la interpola.
            if (expanded) {
                Column(
                    modifier = Modifier.testTag("catalog_details_${product.sku}"),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(stringResource(R.string.catalog_detail_category, categoryText(product.category)))
                    Text(stringResource(R.string.catalog_detail_stock_status, stockText))
                    Text(
                        stringResource(R.string.catalog_detail_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Un único mapeo de enums a strings; reutilizado por chips y tarjetas. */
@Composable
internal fun categoryText(category: DemoCategory): String = when (category) {
    DemoCategory.HIGIENE -> stringResource(R.string.catalog_category_hygiene)
    DemoCategory.PRIMEROS_AUXILIOS -> stringResource(R.string.catalog_category_first_aid)
    DemoCategory.EQUIPOS -> stringResource(R.string.catalog_category_equipment)
}

@Preview(name = "Cotejo 11 - Tarjeta animada", showBackground = true, widthDp = 360)
@Composable
private fun AnimatedProductDemoCardPreview() {
    FarmAppTheme {
        AnimatedProductDemoCard(
            product = DemoCatalogData.products.first(),
            modifier = Modifier.padding(12.dp)
        )
    }
}
