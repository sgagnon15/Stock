package com.sergeapps.stock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sergeapps.stock.vm.ItemsListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsListScreen(
    onBack: () -> Unit,
    viewModel: ItemsListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = state.filter,
                onValueChange = { viewModel.setFilter(it) },
                label = { Text("Filtre") },
                singleLine = true
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { viewModel.prevPage() }, enabled = state.page > 1) { Text("◀") }
                Text("Page ${state.page} / ${state.totalPages}")
                Button(onClick = { viewModel.nextPage() }, enabled = state.page < state.totalPages) { Text("▶") }
                Button(onClick = { viewModel.refresh() }) { Text("Rafraîchir") }
            }

            if (state.isLoading) {
                Text("Chargement…")
            } else if (state.error != null) {
                Text("Erreur: ${state.error}")
                Text("Va dans Paramètres pour configurer URL/Port/Clé d'API.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.items.forEach { row ->
                        Text("${row.itemNumber}  —  ${row.description}")
                    }
                }
            }
        }
    }
}
