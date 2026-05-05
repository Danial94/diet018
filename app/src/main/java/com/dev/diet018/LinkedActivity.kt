package com.dev.diet018

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedScreen(zDefendManager: ZDefendManager, navController: NavController) {
    val textState = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Linked Functions", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { zDefendManager.preregisterLinkedFunction() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-register defaults")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = textState.value,
                onValueChange = { textState.value = it },
                label         = { Text("Function label") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick  = { zDefendManager.registerLinkedFunction(textState.value); textState.value = "" },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp)
                ) { Text("Register") }

                OutlinedButton(
                    onClick  = { zDefendManager.deregisterAllLinkedFunction() },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp)
                ) { Text("Unregister All") }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))

            if (zDefendManager.linkedObjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text      = "No linked functions registered",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding      = PaddingValues(bottom = 16.dp)
                ) {
                    items(zDefendManager.linkedObjects) { linked ->
                        LinkedRow(linked, onDeregister = { zDefendManager.deregisterLinkedFunction(linked.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun LinkedRow(linked: LinkedModel, onDeregister: () -> Unit = {}) {
    val hasThreats = linked.threats.isNotEmpty()
    val eventColor = when {
        linked.eventType.contains("DETECT", ignoreCase = true)  -> MaterialTheme.colorScheme.error
        linked.eventType.contains("MITIGAT", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {

            // Label row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Filled.Link,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = linked.label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.weight(1f)
                )
                // Per-card deregister
                IconButton(onClick = onDeregister, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector        = Icons.Filled.LinkOff,
                        contentDescription = "Deregister",
                        tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier           = Modifier.size(16.dp)
                    )
                }
            }

            // Badges row — below the label
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (linked.eventType.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(6.dp), color = eventColor.copy(alpha = 0.10f)) {
                        Text(
                            text       = linked.eventType,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = eventColor,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (hasThreats) MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                ) {
                    Text(
                        text     = "${linked.threats.count()} threats",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = if (hasThreats) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (hasThreats) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(6.dp))
                linked.threats.forEach { threat ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(
                            text       = threat.name,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier   = Modifier.weight(1f)
                        )
                        Text(
                            text  = threat.severity,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}