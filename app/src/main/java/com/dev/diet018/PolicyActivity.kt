package com.dev.diet018

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Policy
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
fun PolicyScreen(zDefendManager: ZDefendManager, navController: NavController) {
    LaunchedEffect(Unit) { zDefendManager.getPolicies() }
    val policies = zDefendManager.policies

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Policies", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (policies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(
                    text      = "No policies loaded",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(policies) { policy -> PolicyRow(policy) }
            }
        }
    }
}

@Composable
fun PolicyRow(policy: PolicyModel) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Filled.Policy,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = policy.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = policy.downloadDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
                Text(
                    text  = policy.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
                Text(
                    text       = policy.type,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary,
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}