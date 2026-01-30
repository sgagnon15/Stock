package com.sergeapps.stock.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.sergeapps.stock.vm.item.ItemsListViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.derivedStateOf
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
    val listState = rememberLazyListState()


    val shouldLoadMore = remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount

            val threshold = 3 // charge quand il reste 3 items
            lastVisibleIndex >= (totalItems - 1 - threshold)
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMore()
        }
    }

   LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles") }
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

            if (state.isLoading) {
                Text("Chargement…")
            } else if (state.error != null) {
                Text("Erreur: ${state.error}")
                Text("Va dans Paramètres pour configurer URL/Port/Clé d'API.")
            } else {
                val listVersion = remember(state.items) { System.currentTimeMillis() }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.items,
                        key = { it.id }
                    ) { row ->
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

                    // Footer "Charger plus"
                    item {
                        if (state.isLoadingMore) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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


