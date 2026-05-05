package com.dev.diet018

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ── Preset simulate scenarios ─────────────────────────────────────────────────

private data class SimulatePreset(val title: String, val description: String, val id: Int, val icon: ImageVector)

private val simulatePresets = listOf(
    SimulatePreset("USB Debugging",     "Device has USB debugging enabled",           44, Icons.Filled.DeveloperMode),
    SimulatePreset("App on Emulator",   "App is running inside an emulator",           6, Icons.Filled.Computer),
    SimulatePreset("Screen Sharing",    "Active screen recording or sharing detected", 51, Icons.AutoMirrored.Filled.ScreenShare),
    SimulatePreset("Malware",           "Malicious application detected on device",   17, Icons.Filled.BugReport),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulateScreen(zDefendManager: ZDefendManager, navController: NavController) {
    var customId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulate", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding      = PaddingValues(vertical = 20.dp)
        ) {
            // ── Preset threats ────────────────────────────────────────────────
            item {
                Text(
                    text          = "PRESET THREATS",
                    style         = MaterialTheme.typography.labelMedium,
                    color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
                )
            }

            simulatePresets.forEach { preset ->
                item {
                    SimulateActionCard(
                        title       = preset.title,
                        description = "${preset.description}  ·  ID ${preset.id}",
                        icon        = preset.icon,
                        buttonLabel = "Trigger",
                        onClick     = { zDefendManager.simulateThreats(preset.id) }
                    )
                }
            }

            // ── Custom ID ─────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text          = "CUSTOM THREAT ID",
                    style         = MaterialTheme.typography.labelMedium,
                    color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
                )
            }
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value         = customId,
                            onValueChange = { customId = it.filter { c -> c.isDigit() } },
                            label         = { Text("Threat ID") },
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape         = RoundedCornerShape(10.dp),
                            modifier      = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick  = { if (customId.isNotEmpty()) zDefendManager.simulateThreats(customId.toInt()) },
                            enabled  = customId.isNotEmpty(),
                            shape    = RoundedCornerShape(8.dp)
                        ) { Text("Trigger") }
                    }
                }
            }

            // ── Mitigate ──────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text          = "ACTIONS",
                    style         = MaterialTheme.typography.labelMedium,
                    color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
                )
            }
            item {
                SimulateActionCard(
                    title       = "Mitigate All",
                    description = "Clear all simulated threats from the device",
                    icon        = Icons.Filled.Shield,
                    buttonLabel = "Mitigate",
                    onClick     = { zDefendManager.mitigateSimulatedThreats() }
                )
            }
        }
    }
}

@Composable
fun SimulateActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
            Spacer(Modifier.width(12.dp))
            FilledTonalButton(onClick = onClick, shape = RoundedCornerShape(8.dp)) {
                Text(buttonLabel, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}