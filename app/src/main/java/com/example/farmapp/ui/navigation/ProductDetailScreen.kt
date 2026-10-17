package com.example.farmapp.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.catalog.DemoProduct
import com.example.farmapp.ui.catalog.AnimatedProductDemoCard

/** Destino independiente del cotejo 16: sigue siendo válido en celulares. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: DemoProduct?,
    onBack: () -> Unit,
    onShare: (DemoProduct) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("product_detail_screen"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_product_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack, modifier = Modifier.testTag("back_from_product")) {
                        Text(stringResource(R.string.nav_back))
                    }
                }
            )
        }
    ) { padding ->
        ProductDetailBody(
            product = product,
            onShare = onShare,
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}

/** La misma ficha se reutiliza en el panel lateral de una tablet. */
@Composable
fun ProductDetailBody(
    product: DemoProduct?,
    onShare: (DemoProduct) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
            .padding(16.dp).testTag("product_detail_body"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (product == null) {
            Text(stringResource(R.string.nav_unknown_product))
        } else {
            Text(
                product.name,
                modifier = Modifier.semantics { heading() }.testTag("product_detail_title")
            )
            AnimatedProductDemoCard(product = product)
            Text(stringResource(R.string.nav_demo_product_notice))
            Button(
                onClick = { onShare(product) },
                modifier = Modifier.fillMaxWidth().testTag("share_product")
            ) {
                Text(stringResource(R.string.nav_share_product))
            }
        }
    }
}
