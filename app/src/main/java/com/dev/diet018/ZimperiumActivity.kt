@file:OptIn(ExperimentalMaterial3Api::class)

package com.dev.diet018

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ZimperiumScreen(auth: FirebaseAuth, zDefendManager: ZDefendManager, navController: NavController) {
    if (zDefendManager.isLoaded.value) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ZDefend") },
                    actions = {
                        IconButton(onClick = { zDefendManager.checkForUpdates() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "updates")
                        }
                        TextButton(onClick = { logout(auth, navController) }) {
                            Text("Logout", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavigationGrid(navController)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Scan progress %: " + zDefendManager.percentage.intValue)
            for (audit in zDefendManager.auditLogs) {
                Text(text = audit, fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(auth: FirebaseAuth, zDefendManager: ZDefendManager, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZDefend") },
                actions = {
                    IconButton(onClick = { zDefendManager.checkForUpdates() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "updates")
                    }
                    TextButton(onClick = { logout(auth, navController) }) {
                        Text("Logout", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            NavigationGrid(navController)
        }
    }
}

@Composable
fun NavigationGrid(navController: NavController) {
    val navList = listOf(
        Pair("Threats", "threats"),
        Pair("Policies", "policies"),
        Pair("Troubleshoot", "troubleshoot"),
        Pair("Simulate", "simulate"),
        Pair("Audit", "audit"),
        Pair("Linked", "linked")
    )

    LazyColumn {
        items(navList) { nav ->
            NavigationCard(text = nav.first) {
                navController.navigate(nav.second)
            }
        }
    }
}

@Composable
fun NavigationCard(text: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Blue),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

fun logout(auth: FirebaseAuth, navController: NavController) {
    auth.signOut()
    navController.navigate("login")
}