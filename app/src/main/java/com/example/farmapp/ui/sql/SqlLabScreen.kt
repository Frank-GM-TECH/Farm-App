package com.example.farmapp.ui.sql

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.farmapp.R
import com.example.farmapp.data.sql.SqlLabFeedback
import com.example.farmapp.data.sql.SqlLabProduct
import com.example.farmapp.data.sql.SqlLabQuery
import com.example.farmapp.data.sql.SqlLabResult
import com.example.farmapp.data.sql.SqlLabUiState
import com.example.farmapp.data.sql.SqlLabViewModel
import com.example.farmapp.ui.theme.FarmAppTheme

/** Pantalla educativa local: SELECT, WHERE, LIKE, DISTINCT, GROUP BY, ORDER BY, LIMIT y CRUD. */
@Composable
fun SqlLabRoute(
    onBack: () -> Unit,
    viewModel: SqlLabViewModel = viewModel(factory = SqlLabViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SqlLabScreen(
        state = state,
        onBack = onBack,
        onQuery = viewModel::choose,
        onInsert = viewModel::insert,
        onUpdate = viewModel::update,
        onDelete = viewModel::delete,
        onRestore = viewModel::restore
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SqlLabScreen(
    state: SqlLabUiState,
    onBack: () -> Unit,
    onQuery: (SqlLabQuery) -> Unit,
    onInsert: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    Scaffold(
        modifier = Modifier.testTag("sql_lab_screen"),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.sql_title)) }, navigationIcon = {
                TextButton(onClick = onBack) { Text(stringResource(R.string.nav_back)) }
            })
        }
    ) { padding ->
        // Un solo contenedor desplazable: cabecera y filas en el mismo LazyColumn.
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("sql_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(stringResource(R.string.sql_disclaimer), style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SqlLabQuery.entries.forEach { query ->
                        FilterChip(
                            selected = query == state.query,
                            enabled = !state.loading,
                            onClick = { onQuery(query) },
                            label = { Text(sqlQueryLabel(query)) },
                            modifier = Modifier.testTag("sql_query_${query.name}")
                        )
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.sql_statement_heading), modifier = Modifier.semantics { heading() })
                        Text(state.query.statement, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("sql_statement"))
                        if (state.query.arguments.isNotEmpty()) {
                            Text(stringResource(R.string.sql_bound_args, state.query.arguments.joinToString()), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            item {
                Text(stringResource(R.string.sql_crud_heading), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                Text(stringResource(R.string.sql_crud_note), style = MaterialTheme.typography.bodySmall)
            }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onInsert, enabled = !state.loading, modifier = Modifier.heightIn(min = 48.dp).testTag("sql_insert")) { Text("INSERT") }
                    OutlinedButton(onClick = onUpdate, enabled = !state.loading, modifier = Modifier.heightIn(min = 48.dp).testTag("sql_update")) { Text("UPDATE") }
                    OutlinedButton(onClick = onDelete, enabled = !state.loading, modifier = Modifier.heightIn(min = 48.dp).testTag("sql_delete")) { Text("DELETE") }
                    Button(onClick = onRestore, enabled = !state.loading, modifier = Modifier.heightIn(min = 48.dp).testTag("sql_restore")) {
                        Text(stringResource(R.string.sql_restore))
                    }
                }
            }
            item {
                val feedbackRes = when (state.feedback) {
                    SqlLabFeedback.NONE -> null
                    SqlLabFeedback.INSERTED -> R.string.sql_feedback_inserted
                    SqlLabFeedback.EXISTS -> R.string.sql_feedback_exists
                    SqlLabFeedback.UPDATED -> R.string.sql_feedback_updated
                    SqlLabFeedback.NOT_FOUND -> R.string.sql_feedback_not_found
                    SqlLabFeedback.DELETED -> R.string.sql_feedback_deleted
                    SqlLabFeedback.RESTORED -> R.string.sql_feedback_restored
                }
                when {
                    state.loading -> CircularProgressIndicator(Modifier.testTag("sql_loading"))
                    state.hasError -> Text(stringResource(R.string.sql_error), modifier = Modifier.testTag("sql_error"))
                    feedbackRes != null -> Text(stringResource(feedbackRes), modifier = Modifier.testTag("sql_feedback"))
                }
            }
            when (val result = state.result) {
                is SqlLabResult.Products -> {
                    item { Text(stringResource(R.string.sql_result_count, result.items.size), modifier = Modifier.semantics { heading() }) }
                    items(result.items, key = { it.sku }) { product ->
                        SqlProductRow(product)
                    }
                }
                is SqlLabResult.Categories -> {
                    item { Text(stringResource(R.string.sql_result_count, result.items.size), modifier = Modifier.semantics { heading() }) }
                    items(result.items, key = { it }) { category ->
                        Card(Modifier.fillMaxWidth()) { Text(category, Modifier.padding(14.dp)) }
                    }
                }
                is SqlLabResult.Groups -> {
                    item { Text(stringResource(R.string.sql_result_count, result.items.size), modifier = Modifier.semantics { heading() }) }
                    items(result.items, key = { it.category }) { group ->
                        Card(Modifier.fillMaxWidth()) { Text(
                            stringResource(R.string.sql_group_row, group.category, group.products, group.totalUnits),
                            Modifier.padding(14.dp)
                        ) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SqlProductRow(product: SqlLabProduct) {
    Card(Modifier.fillMaxWidth().testTag("sql_item_${product.sku}")) {
        Column(Modifier.padding(14.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleSmall)
            Text(product.sku + " · " + product.category, style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.sql_stock, product.stock), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun sqlQueryLabel(query: SqlLabQuery): String = stringResource(when (query) {
    SqlLabQuery.ALL -> R.string.sql_query_all
    SqlLabQuery.LOW_STOCK -> R.string.sql_query_low
    SqlLabQuery.NAME_LIKE -> R.string.sql_query_like
    SqlLabQuery.DISTINCT_CATEGORIES -> R.string.sql_query_distinct
    SqlLabQuery.GROUP_BY_CATEGORY -> R.string.sql_query_group
})

@Preview(showBackground = true)
@Composable
private fun SqlLabPreview() {
    FarmAppTheme {
        SqlLabScreen(
            state = SqlLabUiState(result = SqlLabResult.Products(listOf(
                SqlLabProduct("FA-001", "Alcohol en gel", "Higiene", 12)
            ))),
            onBack = {}, onQuery = {}, onInsert = {}, onUpdate = {}, onDelete = {}, onRestore = {}
        )
    }
}
