package com.dev.diet018

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.diet018.ui.theme.Diet018Theme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Auth
        auth = Firebase.auth

        super.onCreate(savedInstanceState)
        setContent {
            Diet018Theme {
                navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(auth, navController) }
                    composable("register") { RegisterScreen(auth, navController) }
                    composable("dashboard") { DashboardScreen(auth, navController) }
                }

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    navController.navigate("dashboard")
                }
            }
        }
    }
}

