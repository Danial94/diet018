package com.dev.diet018

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(zDefendManager: ZDefendManager, navController: NavController) {
    val alertThreats = zDefendManager.alertThreats

    fun dismiss() {
        zDefendManager.showAlert.value = false
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Alert", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { dismiss() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Security Threat Detected",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = "${alertThreats.size} active threat${if (alertThreats.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            if (alertThreats.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "All threats have been mitigated.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(alertThreats, key = { it.id }) { threat ->
                        AlertThreatCard(threat)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { dismiss() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Dismiss", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun AlertThreatCard(threat: ThreatModel) {
    val severityColor = when (threat.severity.uppercase()) {
        "CRITICAL" -> MaterialTheme.colorScheme.error
        "HIGH"     -> MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
        "MEDIUM"   -> MaterialTheme.colorScheme.tertiary
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.GppBad,
                contentDescription = null,
                tint = severityColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = threat.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                if (threat.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = threat.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = severityColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = threat.severity,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = severityColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
