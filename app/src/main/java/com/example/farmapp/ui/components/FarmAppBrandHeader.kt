package com.example.farmapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.farmapp.R
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * Cotejo 01: @Composable, Text, Row, Box y Column.
 * Cotejo 02: Image, painterResource, contentDescription y stringResource.
 */
@Composable
fun FarmAppBrandHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            // La imagen describe un símbolo relevante, por lo que TalkBack recibe una descripción.
            Image(
                painter = painterResource(id = R.drawable.ic_farmapp_logo),
                contentDescription = stringResource(id = R.string.cd_farmapp_logo),
                modifier = Modifier.size(44.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = stringResource(id = R.string.brand_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Se puede inspeccionar el ícono, Image y los textos en Preview sin iniciar sesión. */
@Preview(showBackground = true, name = "FarmApp - Imágenes y recursos")
@Composable
private fun FarmAppBrandHeaderPreview() {
    FarmAppTheme {
        Surface {
            FarmAppBrandHeader(modifier = Modifier.padding(20.dp))
        }
    }
}
