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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsListScreen(
    onBack: () -> Unit,
    onOpenItem: (Int) -> Unit,
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
                onValueChange = { newValue ->
                    viewModel.onFilterChanged(newValue)
                },
                label = { Text("Filtre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (state.filter.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onFilterChanged("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Effacer le filtre"
                            )
                        }
                    }
                }
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
                    val listVersion = remember(state.items) { System.currentTimeMillis() }

                    state.items.forEach { row ->
                        ItemRow(
                            itemId = row.id,
                            itemNumber = row.itemNumber,
                            description = row.description,
                            vendor = row.vendor,
                            manufacturer = row.manufacturer,
                            imageUrl = row.imageUrl,
                            listVersion = listVersion,
                            onClick = onOpenItem
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ItemRow(
    itemId: Int,
    itemNumber: String,
    description: String,
    vendor: String,
    manufacturer: String,
    imageUrl: String?,
    listVersion: Long,
    onClick: (Int) -> Unit
) {
    Card(
        onClick = { onClick(itemId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val model = imageUrl?.let { url ->
                // Cache normal, mais on change l’URL quand la liste est refreshée
                if (url.contains("?")) "$url&v=$listVersion" else "$url?v=$listVersion"
            }

            AsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$itemNumber — $description",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = listOf(vendor, manufacturer)
                        .filter { it.isNotBlank() }
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


