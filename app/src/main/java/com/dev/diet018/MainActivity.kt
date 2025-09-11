package com.dev.diet018

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.diet018.ui.theme.Diet018Theme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var navController: NavHostController
    private lateinit var zDefendManager: ZDefendManager

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        db = Firebase.firestore
        zDefendManager = ZDefendManager.shared

        super.onCreate(savedInstanceState)
        setContent {
            Diet018Theme {
                val showAlert by zDefendManager.showAlert
                val alertMsg by zDefendManager.alertMessage

                if (showAlert) {
                    AlertDialog(
                        onDismissRequest = { zDefendManager.showAlert.value = false },
                        title = { Text("Security Alert") },
                        text = { Text(alertMsg) },
                        confirmButton = {
                            TextButton(onClick = { zDefendManager.showAlert.value = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(auth, navController) }
                    composable("register") { RegisterScreen(auth, navController) }
                    composable("dashboard") { DashboardScreen(auth, db, navController) }
                    composable("zimperium") { ZimperiumScreen(auth, zDefendManager, navController) }
                    composable("main") { MainScreen(auth, zDefendManager, navController) }
                    composable("threats") { ThreatsScreen(zDefendManager, navController) }
                    composable("policies") { PolicyScreen(zDefendManager, navController) }
                    composable("troubleshoot") { TroubleshootScreen(zDefendManager, navController) }
                    composable("simulate") { SimulateScreen(zDefendManager, navController) }
                    composable("audit") { AuditScreen(zDefendManager, navController) }
                    composable("linked") { LinkedScreen(zDefendManager, navController) }
                    composable("status") { StatusScreen(zDefendManager, navController) }
                }

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    navController.navigate("zimperium")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        zDefendManager.initializeZDefendApi()
    }

    override fun onDestroy() {
        super.onDestroy()
        zDefendManager.deregisterZDefendApi()
    }
}