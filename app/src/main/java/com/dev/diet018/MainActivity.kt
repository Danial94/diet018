package com.dev.diet018

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.diet018.ui.theme.Diet018Theme

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var zDefendManager: ZDefendManager

    override fun onCreate(savedInstanceState: Bundle?) {
        zDefendManager = ZDefendManager.shared

        super.onCreate(savedInstanceState)
        setContent {
            Diet018Theme {
                val showAlert by zDefendManager.showAlert

                navController = rememberNavController()

                // Navigate to the full alert screen whenever a linked-function alert fires
                LaunchedEffect(showAlert) {
                    if (showAlert) {
                        navController.navigate("alert_screen")
                    }
                }

                NavHost(navController = navController, startDestination = "loading") {
                    composable("loading") { LoadingScreen(zDefendManager, navController) }
                    composable("zimperium") { ZimperiumScreen(zDefendManager, navController) }
                    composable("main") { MainScreen(zDefendManager, navController) }
                    composable("threats") { ThreatsScreen(zDefendManager, navController) }
                    composable("policies") { PolicyScreen(zDefendManager, navController) }
                    composable("troubleshoot") { TroubleshootScreen(zDefendManager, navController) }
                    composable("simulate") { SimulateScreen(zDefendManager, navController) }
                    composable("audit") { AuditScreen(zDefendManager, navController) }
                    composable("linked") { LinkedScreen(zDefendManager, navController) }
                    composable("status") { StatusScreen(zDefendManager, navController) }
                    composable("alert_screen") { AlertScreen(zDefendManager, navController) }
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