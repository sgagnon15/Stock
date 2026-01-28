package com.sergeapps.stock.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sergeapps.stock.vm.ItemDetailViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    viewModel: ItemDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onPickPhoto(uri)
            }
        }
    )


    LaunchedEffect(itemId) {
        viewModel.load(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail article") },
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
            if (state.isLoading) {
                Text("Chargement…")
                return@Column
            }

            if (state.error != null) {
                Text("Erreur: ${state.error}")
                return@Column
            }

            if (state.imageUrl.isNullOrBlank()) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Text(
                        text = "Aucune image",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            ItemPhotoSection(
                photoUrl = state.imageUrl,
                localSelectedPhotoUri = state.localSelectedPhotoUri,
                isUploading = state.isUploadingPhoto,
                onPickPhoto = { pickImageLauncher.launch("image/*") },
                onUploadPhoto = { viewModel.uploadPickedPhoto(context) },
                onDeletePhoto = { viewModel.deletePhoto() }
            )

            OutlinedCard {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("${state.itemNumber} — ${state.description}")
                    Text("Vendeur: ${state.vendor}")
                    Text("Fabricant: ${state.manufacturer}")
                    Text("UOM: ${state.uom}")
                    Text("Code barre: ${state.barcode}")
                    Text("Min: ${state.minLevel}   Max: ${state.maxLevel}")
                }
            }

            if (state.vendorUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.vendorUrl))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Ouvrir le lien vendeur")
                }
            }
        }
    }
}

@Composable
private fun ItemPhotoSection(
    photoUrl: String?,
    localSelectedPhotoUri: Uri?,
    isUploading: Boolean,
    onPickPhoto: () -> Unit,
    onUploadPhoto: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        val imageModel: Any? = when {
            localSelectedPhotoUri != null -> localSelectedPhotoUri
            !photoUrl.isNullOrBlank() -> photoUrl
            else -> null
        }

        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPickPhoto, enabled = !isUploading) {
                Text(if (photoUrl.isNullOrBlank()) "Ajouter photo" else "Remplacer photo")
            }

            Button(
                onClick = onUploadPhoto,
                enabled = localSelectedPhotoUri != null && !isUploading
            ) {
                Text("Uploader")
            }

            if (!photoUrl.isNullOrBlank()) {
                Button(onClick = onDeletePhoto, enabled = !isUploading) {
                    Text("Supprimer")
                }
            }
        }
    }
}
