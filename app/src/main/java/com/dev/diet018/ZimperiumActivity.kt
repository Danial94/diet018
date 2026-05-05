package com.dev.diet018

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ─── Model ────────────────────────────────────────────────────────────────────

private data class NavItem(val label: String, val route: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem("Threats",      "threats",      Icons.Filled.BugReport),
    NavItem("Policies",     "policies",     Icons.Filled.Policy),
    NavItem("Troubleshoot", "troubleshoot", Icons.Filled.Build),
    NavItem("Simulate",     "simulate",     Icons.Filled.Science),
    NavItem("Audit",        "audit",        Icons.Filled.History),
    NavItem("Linked",       "linked",       Icons.Filled.Link),
    NavItem("Status",       "status",       Icons.Filled.Speed),
)

// ─── Entry-point screens ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZimperiumScreen(zDefendManager: ZDefendManager, navController: NavController) {
    MainScreen(zDefendManager, navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(zDefendManager: ZDefendManager, navController: NavController) {

    // ── Fix: call side-effectful functions outside composition to prevent
    //         the auditLogs → recompose → auditLogs infinite loop.
    LaunchedEffect(Unit) {
        zDefendManager.getThreats()
        zDefendManager.getPolicies()
    }

    val threats       = zDefendManager.threats
    val policies      = zDefendManager.policies
    val linkedObjects = zDefendManager.linkedObjects
    val deviceRisk    by zDefendManager.deviceRisk
    val sdkVersion    by zDefendManager.sdkVersion
    val lastScanTime  by zDefendManager.lastScanTime

    val activeCount    = threats.count { !it.status }
    val mitigatedCount = threats.count { it.status }
    val isSecure       = activeCount == 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZDefend", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = {
                        zDefendManager.checkForUpdates()
                        zDefendManager.getThreats()
                        zDefendManager.getPolicies()
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { StatusBanner(isSecure, activeCount, mitigatedCount, deviceRisk, sdkVersion, lastScanTime) }
            item { StatsRow(active = activeCount, policies = policies.size, linked = linkedObjects.size) }
            item {
                Text(
                    text  = "TOOLS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    letterSpacing = 2.sp
                )
            }
            item { ToolsGrid(navController) }
        }
    }
}

// ─── Status banner ────────────────────────────────────────────────────────────

@Composable
fun StatusBanner(isSecure: Boolean, active: Int, mitigated: Int, deviceRisk: String, sdkVersion: String, lastScanTime: String) {
    val statusColor = if (isSecure) Color(0xFF2E7D32) else Color(0xFFB71C1C)
    val label       = if (isSecure) "Secure" else "At Risk"
    val sub         = if (isSecure) "No active threats detected"
                      else "$active active threat${if (active != 1) "s" else ""} found"

    val riskColor = when (deviceRisk) {
        "CRITICAL" -> Color(0xFFB71C1C)
        "HIGH"     -> Color(0xFFE65100)
        "MEDIUM"   -> Color(0xFFF57F17)
        "LOW"      -> Color(0xFF1565C0)
        "SECURE"   -> Color(0xFF2E7D32)
        else       -> Color(0xFF546E7A)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // ── Main status row ───────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = label,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = statusColor
                    )
                    Text(
                        text  = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                    if (mitigated > 0) {
                        Text(
                            text  = "$mitigated resolved",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32).copy(alpha = 0.65f)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector        = if (isSecure) Icons.Filled.VerifiedUser else Icons.Filled.Warning,
                        contentDescription = null,
                        tint               = statusColor.copy(alpha = 0.20f),
                        modifier           = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    // Device risk badge
                    Surface(shape = RoundedCornerShape(6.dp), color = riskColor.copy(alpha = 0.12f)) {
                        Text(
                            text       = deviceRisk,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = riskColor,
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // ── Footer: SDK version + last scan time ──────────────────────────
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "SDK $sdkVersion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
                Text(
                    text  = "Last scan: $lastScanTime",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            }
        }
    }
}

// ─── Stats row ────────────────────────────────────────────────────────────────

@Composable
fun StatsRow(active: Int, policies: Int, linked: Int) {
    val activeColor = if (active > 0) Color(0xFFB71C1C) else Color(0xFF2E7D32)
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniStat("$active",   "Active",   activeColor,                               Modifier.weight(1f))
        MiniStat("$policies", "Policies", MaterialTheme.colorScheme.onSurface,       Modifier.weight(1f))
        MiniStat("$linked",   "Linked",   MaterialTheme.colorScheme.onSurface,       Modifier.weight(1f))
    }
}

@Composable
fun MiniStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(2.dp))
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Tools grid ───────────────────────────────────────────────────────────────

@Composable
fun ToolsGrid(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        navItems.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { item ->
                    ToolCard(item, Modifier.weight(1f)) { navController.navigate(item.route) }
                }
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ToolCard(item: NavItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick   = onClick
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = item.label,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text       = item.label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─── end of file ──────────────────────────────────────────────────────────────
