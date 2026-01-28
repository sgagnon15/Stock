package com.sergeapps.stock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sergeapps.stock.vm.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
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
                value = state.baseUrl,
                onValueChange = viewModel::setBaseUrl,
                label = { Text("URL (ex: https://homeapi.ddns.net)") },
                singleLine = true
            )
            OutlinedTextField(
                value = state.port,
                onValueChange = viewModel::setPort,
                label = { Text("Port (ex: 443)") },
                singleLine = true
            )
            OutlinedTextField(
                value = state.deviceId,
                onValueChange = viewModel::setDeviceId,
                label = { Text("ID (optionnel)") },
                singleLine = true
            )
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = viewModel::setApiKey,
                label = { Text("Clé d'API (X-API-Key)") },
                singleLine = true
            )

            Button(onClick = viewModel::save) { Text("Enregistrer") }

            state.savedMessage?.let { Text(it) }

            Text(
                "Notes: ton projet MIT utilise des endpoints comme /info, /itemList, /nbpagesitem, /item, /stock, /location, etc. " +
                    "On va migrer écran par écran en gardant la même API."
            )
        }
    }
}
