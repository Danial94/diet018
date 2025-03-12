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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ZimperiumScreen(zDefendManager: ZDefendManager, navController: NavController) {
    AppNavigation(zDefendManager);

//    private val zDefendManager : ZDefendManager = ZDefendManager.shared
//
//    // Dummy database connection
//    private val databaseConnectionString: String = "Server=10.10.0.27;Database=main;User Id=danial;Password=1234;"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            TrafficLikeTheme {
//                AppNavigation(zDefendManager)
//            }
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        zDefendManager.initializeZDefendApi()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        zDefendManager.deregisterZDefendApi()
//    }
}

@Composable
fun AppNavigation(zDefendManager: ZDefendManager) {
    if (zDefendManager.isLoaded.value) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = "main") {
            composable("main") { MainScreen(zDefendManager, navController) }
            composable("threats") { ThreatsScreen(zDefendManager, navController) }
            composable("policies") { PolicyScreen(zDefendManager, navController) }
            composable("troubleshoot") { TroubleshootScreen(zDefendManager, navController) }
            composable("simulate") { SimulateScreen(zDefendManager, navController) }
            composable("audit") { AuditScreen(zDefendManager, navController) }
            composable("linked") { LinkedScreen(zDefendManager, navController) }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Scan progress %: " + zDefendManager.percentage.intValue)
            for (audit in zDefendManager.auditLogs) {
                Text(text = audit)
            }
        }
    }
}

@Composable
fun MainScreen(zDefendManager: ZDefendManager, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Header(zDefendManager)
        NavigationGrid(navController)
    }
}

@Composable
fun Header(zDefendManager: ZDefendManager) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { zDefendManager.checkForUpdates() }) {
            Icon(Icons.Default.Refresh, contentDescription = "updates")
        }
        Text(
            text = "ZDefend",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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