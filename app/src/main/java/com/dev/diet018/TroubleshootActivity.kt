package com.dev.diet018

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val TroubleshootTabs = listOf("Logs", "Details")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TroubleshootScreen(zDefendManager: ZDefendManager, navController: NavController) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Troubleshoot", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { zDefendManager.refreshTroubleshoot() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                TroubleshootTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick  = { selectedTabIndex = index },
                        text     = {
                            Text(
                                text       = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0    -> LogViewer(zDefendManager.troubleshootLogs.value)
                else -> DetailsViewer(zDefendManager.troubleshootDetailsList)
            }
        }
    }
}

// ── Logs tab: raw monospace viewer ───────────────────────────────────────────

@Composable
fun LogViewer(text: String) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text       = text.ifEmpty { "No data available." },
            fontFamily = FontFamily.Monospace,
            fontSize   = 12.sp,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier   = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp)
        )
    }
}

// ── Details tab: searchable key/value list ────────────────────────────────────

@Composable
fun DetailsViewer(items: List<Pair<String, String>>) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter {
            it.first.contains(query, ignoreCase = true) ||
            it.second.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // ── Search bar ──────────────────────────────────────────────────────
        OutlinedTextField(
            value         = query,
            onValueChange = { query = it },
            placeholder   = { Text("Filter entries…", fontSize = 13.sp) },
            leadingIcon   = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon  = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            modifier   = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // ── Entry count ─────────────────────────────────────────────────────
        Row(
            modifier            = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Text(
                text  = if (items.isEmpty()) "No data — tap ↻ to refresh" else "${filtered.size} of ${items.size} entries",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── List ────────────────────────────────────────────────────────────
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text  = if (items.isEmpty()) "No data available.\nTap ↻ to refresh." else "No results for \"$query\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.first }) { (key, value) ->
                    DetailRow(key, value)
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun DetailRow(key: String, value: String) {
    Surface(
        shape    = RoundedCornerShape(8.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Key label
            Text(
                text     = key,
                style    = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color    = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            // Value — monospace, full text, line-wrap friendly
            Text(
                text       = value.ifBlank { "—" },
                style      = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
