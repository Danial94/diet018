package com.dev.diet018

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ── Severity colour ───────────────────────────────────────────────────────────

private fun severityColor(severity: String): Color = when (severity.uppercase()) {
    "CRITICAL" -> Color(0xFFB71C1C)
    "HIGH"     -> Color(0xFFE65100)
    "MEDIUM"   -> Color(0xFFF57F17)
    "LOW"      -> Color(0xFF1565C0)
    else       -> Color(0xFF546E7A)
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatsScreen(zDefendManager: ZDefendManager, navController: NavController) {
    LaunchedEffect(Unit) { zDefendManager.getThreats() }
    val threats = zDefendManager.threats

    val detectedCount = threats.count { !it.status }
    val clearCount    = threats.count {  it.status  }

    var filter by remember { mutableStateOf("All") }
    val displayed = remember(filter, threats.size) {
        when (filter) {
            "Detected" -> threats.filter { !it.status }
            "Clear"    -> threats.filter {  it.status  }
            else       -> threats.toList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Threats", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // ── Count strip ───────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThreatCountCard("Detected", detectedCount, Color(0xFFC62828), Modifier.weight(1f))
                ThreatCountCard("Clear",    clearCount,    Color(0xFF2E7D32), Modifier.weight(1f))
                ThreatCountCard("Total",    threats.size,  MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
            }

            // ── Filter chips ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All"      to threats.size,
                    "Detected" to detectedCount,
                    "Clear"    to clearCount
                ).forEach { (label, count) ->
                    FilterChip(
                        selected = filter == label,
                        onClick  = { filter = label },
                        label    = { Text("$label ($count)", style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            if (threats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text      = "No threat data available",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(displayed, key = { it.id }) { threat ->
                        ThreatRow(threat)
                    }
                }
            }
        }
    }
}

// ── Count card ────────────────────────────────────────────────────────────────

@Composable
fun ThreatCountCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Threat row (expandable) ───────────────────────────────────────────────────

@Composable
fun ThreatRow(threat: ThreatModel) {
    // isMitigated=false → WARNING (active threat, red)
    // isMitigated=true  → VERIFIED USER / shield-check (cleared, green)
    val isDetected  = !threat.status
    val stateColor  = if (isDetected) Color(0xFFC62828) else Color(0xFF2E7D32)
    val stateIcon   = if (isDetected) Icons.Filled.Warning else Icons.Filled.VerifiedUser
    val stateLabel  = if (isDetected) "Detected" else "Clear"
    val sevColor    = severityColor(threat.severity)

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick   = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp)) {

            // ── Header ────────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = stateIcon,
                    contentDescription = stateLabel,
                    tint               = stateColor,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = threat.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = sevColor.copy(alpha = 0.12f)) {
                    Text(
                        text       = threat.severity,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = sevColor,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // ── Expanded details ──────────────────────────────────────────────
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))

                // Status pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = "Status  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Surface(shape = RoundedCornerShape(4.dp), color = stateColor.copy(alpha = 0.10f)) {
                        Text(
                            text       = stateLabel,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = stateColor,
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (threat.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "Description",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(text = threat.description, style = MaterialTheme.typography.bodySmall)
                }

                if (threat.resolution.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "Resolution",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(text = threat.resolution, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}