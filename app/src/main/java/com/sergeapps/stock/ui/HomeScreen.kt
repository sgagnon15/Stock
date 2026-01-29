package com.sergeapps.stock.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.tensorflow.lite.support.label.Category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Settings


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenItems: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenLocations: () -> Unit,
    onOpenClassifications: () -> Unit,
    onOpenSpecifications: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestion des stocks") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Sections",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeTile(
                    title = "Articles",
                    subtitle = "Liste, détails, photos",
                    icon = Icons.Filled.List,
                    onClick = onOpenItems,
                    modifier = Modifier.weight(1f)
                )

                HomeTile(
                    title = "Inventaire",
                    subtitle = "Quantités / mouvements",
                    icon = Icons.Filled.Storage,
                    onClick = onOpenInventory,
                    modifier = Modifier.weight(1f)
                )            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeTile(
                    title = "Emplacements",
                    subtitle = "Sites, pièces, bacs",
                    icon = Icons.Filled.Place,
                    onClick = onOpenLocations,
                    modifier = Modifier.weight(1f)
                )

                HomeTile(
                    title = "Classifications",
                    subtitle = "Catégories / tags",
                    icon = Icons.Filled.Label,
                    onClick = onOpenClassifications,
                    modifier = Modifier.weight(1f)
                )            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeTile(
                    title = "Spécifications",
                    subtitle = "Attributs techniques",
                    icon = Icons.Filled.Home, // temporaire
                    onClick = onOpenSpecifications,
                    modifier = Modifier.weight(1f)
                )

                HomeTile(
                    title = "Paramètres",
                    subtitle = "URL / Port / API Key",
                    icon = Icons.Filled.Settings,
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f)
                )            }
        }
    }
}

@Composable
private fun HomeTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(108.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
