package com.dev.diet018

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(zDefendManager: ZDefendManager, navController: NavController) {
    val audits = zDefendManager.auditLogs
    val scrollState = rememberScrollState()

    // Auto-scroll to bottom whenever a new entry arrives
    LaunchedEffect(audits.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text       = audits.joinToString("\n").ifEmpty { "No audit events yet." },
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
}
