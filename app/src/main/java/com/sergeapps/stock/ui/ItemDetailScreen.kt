package com.sergeapps.stock.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sergeapps.stock.vm.ItemDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.sergeapps.stock.data.StockImageLoaderFactory
import androidx.compose.foundation.layout.size


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    viewModel: ItemDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showDeletePhotoDialog by remember { mutableStateOf(false) }
//    var lastAutoUploadedUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            // 1) Met à jour le state avec la photo choisie (pour l’aperçu immédiat)
            viewModel.onPickPhoto(uri)

            // 2) Upload automatique
            coroutineScope.launch {
                delay(80)
                viewModel.uploadPickedPhoto(context)
            }
        }
    )

    LaunchedEffect(itemId) {
        viewModel.load(itemId)
    }

    if (showDeletePhotoDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePhotoDialog = false },
            title = { Text(text = "Supprimer la photo ?") },
            text = { Text(text = "Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeletePhotoDialog = false
                        viewModel.deletePhoto()
                    }
                ) {
                    Text(text = "Supprimer")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeletePhotoDialog = false }
                ) {
                    Text(text = "Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Détail article",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "←")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (state.isLoading) {
                Text(text = "Chargement…")
                return@Column
            }

            if (state.error != null) {
                Text(text = "Erreur: ${state.error}")
                return@Column
            }

            val imageModel: Any? = when {
                state.localSelectedPhotoUri != null -> state.localSelectedPhotoUri
                !state.imageUrl.isNullOrBlank() -> state.imageUrl
                else -> null
            }

            PhotoHeroCard(
                imageModel = imageModel,
                photoVersion = state.photoVersion,
                hasRemotePhoto = !state.imageUrl.isNullOrBlank(),
                isUploading = state.isUploadingPhoto,
                apiKey = state.apiKey,                 // ✅
                onPickPhoto = { pickImageLauncher.launch("image/*") },
                onAskDeletePhoto = { showDeletePhotoDialog = true }
            )

            ItemHeaderCard(
                title = "${state.itemNumber} — ${state.description}"
            )

            ItemFieldsCard(
                vendor = state.vendor,
                manufacturer = state.manufacturer,
                uom = state.uom,
                barcode = state.barcode,
                minLevel = state.minLevel,
                maxLevel = state.maxLevel
            )

            if (state.vendorUrl.isNotBlank()) {
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.vendorUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isUploadingPhoto
                ) {
                    Text(text = "Ouvrir le lien vendeur")
                }
            }
        }
    }
}

@Composable
private fun PhotoHeroCard(
    imageModel: Any?,
    hasRemotePhoto: Boolean,
    photoVersion: Long,
    isUploading: Boolean,
    apiKey: String,
    onPickPhoto: () -> Unit,
    onAskDeletePhoto: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val context = LocalContext.current
    var isMenuOpen by remember { mutableStateOf(false) }

    val imageLoader = remember(apiKey) {
        StockImageLoaderFactory.create(context, apiKey)
    }

    val finalModel: Any? = when (imageModel) {
        is String -> {
//            val busted = "${imageModel}?v=$photoVersion"
            val url = imageModel
            val busted = if (url.contains("?")) "$url&v=$photoVersion" else "$url?v=$photoVersion"

            ImageRequest.Builder(context)
                .data(busted)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                // ⚠️ ne PAS désactiver le réseau
                .listener(
                    onSuccess = { _, _ ->
                        android.util.Log.d("COIL_PHOTO", "OK $busted")
                    },
                    onError = { _, result ->
                        android.util.Log.e("COIL_PHOTO", "ERR $busted", result.throwable)
                    }
                )
                .build()
        }
        else -> imageModel
    }

    Card(
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(shape)
            ) {
                if (finalModel != null) {
                    AsyncImage(
                        model = finalModel,
                        imageLoader = imageLoader,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onPickPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ajouter une photo",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (hasRemotePhoto) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(
                            onClick = { isMenuOpen = true },
                            enabled = !isUploading
                        ) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = isMenuOpen,
                            onDismissRequest = { isMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Supprimer la photo") },
                                onClick = {
                                    isMenuOpen = false
                                    onAskDeletePhoto()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }

                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(10.dp)
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPickPhoto,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (hasRemotePhoto) "Remplacer la photo" else "Choisir une photo")
                }

                Text(
                    text = "L’upload démarre automatiquement après la sélection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun ItemHeaderCard(
    title: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Informations",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ItemFieldsCard(
    vendor: String,
    manufacturer: String,
    uom: String,
    barcode: String,
    minLevel: String,
    maxLevel: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LabeledValueRow(label = "Vendeur", value = vendor)
            LabeledValueRow(label = "Fabricant", value = manufacturer)

            Divider()

            LabeledValueRow(label = "UOM", value = uom)
            LabeledValueRow(label = "Code barre", value = barcode.ifBlank { "—" })

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    label = "Min",
                    value = minLevel.ifBlank { "—" },
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Max",
                    value = maxLevel.ifBlank { "—" },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LabeledValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.62f)
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
