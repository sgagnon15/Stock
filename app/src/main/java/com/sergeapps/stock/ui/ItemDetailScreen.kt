package com.sergeapps.stock.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenuBox
import com.sergeapps.stock.data.ItemDetailDto
import com.sergeapps.stock.vm.VendorUi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.SolidColor
import com.sergeapps.stock.vm.ManufUi
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    viewModel: ItemDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    var showDeletePhotoDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            viewModel.onPickPhoto(uri)
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

    Scaffold { padding ->

        val state by viewModel.state.collectAsState()

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chargement…")
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Erreur : ${state.error}")
                }
            }

            state.itemDetail != null -> {
                val item = state.itemDetail!!

                val imageModel: Any? = when {
                    state.localSelectedPhotoUri != null -> state.localSelectedPhotoUri
                    !state.imageUrl.isNullOrBlank() -> state.imageUrl
                    else -> null
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        PhotoHeroCard(
                            imageModel = imageModel,
                            photoVersion = state.photoVersion,
                            hasRemotePhoto = !state.imageUrl.isNullOrBlank(),
                            isUploading = state.isUploadingPhoto,
                            apiKey = state.apiKey,
                            onPickPhoto = { pickImageLauncher.launch("image/*") },
                            onAskDeletePhoto = { showDeletePhotoDialog = true }
                        )
                    }

                    item {
                        ItemHeaderCard(
                            title = "${item!!.itemNumber} — ${item.description}"
                        )
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                VendorPicker(
                                    vendorText = state.vendorText,
                                    isLoading = state.isVendorLoading,
                                    options = state.vendorOptions,
                                    onTextChanged = viewModel::onVendorTextChanged,
                                    onSelected = viewModel::onVendorSelected,
                                    onOpen = viewModel::onVendorOpen
                                )

                                ManufacturerPicker(
                                    manufacturerText = state.manufacturerText,
                                    isLoading = state.isManufacturerLoading,
                                    options = state.manufacturerOptions,
                                    onTextChanged = viewModel::onManufacturerTextChanged,
                                    onSelected = viewModel::onManufacturerSelected,
                                    onOpen = viewModel::onManufacturerOpen
                                )

                                DetailRow(label = "UOM", value = item.uom.orDash())
                                DetailRow(label = "Code barre", value = item.barcode.orDash())
                                DetailRow(label = "No. modèle", value = item.modelNum.orDash())
                            }
                        }
                    }
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
            LabeledValueRow(label = "Fournisseur", value = vendor)
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

@Composable
fun ItemDetailInfoSection(
    item: ItemDetailDto,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DetailRow(label = "UOM", value = item.uom.orDash())
        DetailRow(label = "CAB", value = item.barcode.orDash())
        DetailRow(label = "Fournisseur", value = item.vendor.orDash())
        DetailRow(label = "Manufactutier", value = item.manufacturer.orDash())
        DetailRow(label = "No. modèle", value = item.modelNum.orDash())

        DetailRow(label = "Coût moyen", value = item.avgCost.orDash())
        DetailRow(label = "Qté Min", value = item.minLevel.orDash())
        DetailRow(label = "Qté Max", value = item.maxLevel.orDash())

        DetailRow(label = "SKU", value = item.sku.orDash())
//        DetailRow(label = "Vendor URL", value = item.vendorUrl.orDash())

        DetailRow(label = "Classification", value = item.classId?.toString().orDash())
        DetailRow(label = "Date de création", value = formatIsoDate(item.creationDate))
    }
}


@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun String?.orDash(): String {
    return if (this.isNullOrBlank()) "—" else this
}

private fun formatIsoDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        formatter.withZone(ZoneId.systemDefault()).format(instant)
    } catch (_: Exception) {
        iso
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorPicker(
    vendorText: String,
    isLoading: Boolean,
    options: List<VendorUi>,
    onTextChanged: (String) -> Unit,
    onSelected: (String) -> Unit,
    onOpen: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var suppressFocusOnce by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun closeImeAndClearFocus() {
        expanded = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .heightIn(min = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ LABEL – IDENTIQUE À DetailRow
            Text(
                text = "Fournisseur",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(120.dp)
            )

//            Spacer(modifier = Modifier.width(12.dp))

            // ✅ VALEUR – même zone que les autres champs
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = vendorText,
                    onValueChange = onTextChanged,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { closeImeAndClearFocus() }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .focusProperties {
                            if (suppressFocusOnce) canFocus = false
                        }
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 8.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .focusable(false)
                            .clickable {
                                suppressFocusOnce = true
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (!expanded) onOpen()
                                expanded = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "Choisir un fournisseur"
                        )
                    }
        }
            }
        }

        // 🔽 MENU
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!isLoading && options.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Aucun résultat") },
                            onClick = { expanded = false },
                            enabled = false
                        )
                    } else {
                        options.forEach { vendor ->
                            val isSelected = vendor.name == vendorText

                            DropdownMenuItem(
                                text = { Text(vendor.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = {
                                    onSelected(vendor.name)
                                    closeImeAndClearFocus()
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(suppressFocusOnce) {
        if (suppressFocusOnce) {
            withFrameNanos { }
            suppressFocusOnce = false
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManufacturerPicker(
    manufacturerText: String,
    isLoading: Boolean,
    options: List<ManufUi>,
    onTextChanged: (String) -> Unit,
    onSelected: (String) -> Unit,
    onOpen: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var suppressFocusOnce by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun closeImeAndClearFocus() {
        expanded = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .heightIn(min = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Fabricant",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(120.dp)
            )

//            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = manufacturerText,
                    onValueChange = onTextChanged,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { closeImeAndClearFocus() }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                        .focusProperties {
                            if (suppressFocusOnce) canFocus = false
                        }
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 8.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .focusable(false)
                            .clickable {
                                suppressFocusOnce = true
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (!expanded) onOpen()
                                expanded = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "Choisir un fabricant"
                        )
                    }
                }
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!isLoading && options.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Aucun résultat") },
                            onClick = { expanded = false },
                            enabled = false
                        )
                    } else {
                        options.forEach { manuf ->
                            val isSelected = manuf.name == manufacturerText

                            DropdownMenuItem(
                                text = { Text(manuf.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = {
                                    onSelected(manuf.name)
                                    closeImeAndClearFocus()
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        LaunchedEffect(suppressFocusOnce) {
            if (suppressFocusOnce) {
                withFrameNanos { }
                suppressFocusOnce = false
            }
        }

        LaunchedEffect(expanded) {
            if (expanded) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }
    }
}

