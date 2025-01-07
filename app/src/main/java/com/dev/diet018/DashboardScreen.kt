package com.dev.diet018

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(auth: FirebaseAuth, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    TextButton(onClick = { logout(auth, navController) }) {
                        Text("Logout", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome to Diet018",
                fontSize = 24.sp
            )
        }
    }
}

fun logout(auth: FirebaseAuth, navController: NavController) {
    auth.signOut()
    navController.navigate("login")
}