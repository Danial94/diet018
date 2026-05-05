package com.dev.diet018

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(zDefendManager: ZDefendManager, navController: NavController) {
    val statusLogs = zDefendManager.statusLogs
    val listState  = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (statusLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text      = "No status events yet",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state               = listState,
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier            = Modifier.fillMaxSize()
                ) {
                    items(statusLogs) { log -> StatusEventRow(log) }
                }
            }
            LaunchedEffect(statusLogs.size) {
                if (statusLogs.isNotEmpty()) listState.animateScrollToItem(statusLogs.size - 1)
            }
        }
    }
}

@Composable
fun StatusEventRow(log: String) {
    val isThreat  = log.startsWith("New Threats")
    val icon      = if (isThreat) Icons.Filled.Warning else Icons.Filled.VerifiedUser
    val iconColor = if (isThreat) Color(0xFFC62828) else Color(0xFF2E7D32)
    val lines     = log.trim().lines()
    val title     = lines.firstOrNull() ?: log
    val subtitle  = lines.drop(1).joinToString(" · ").trim()

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                if (subtitle.isNotEmpty()) {
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }
        }
    }
}