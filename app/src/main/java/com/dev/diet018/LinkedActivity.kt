package com.dev.diet018

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedScreen(zDefendManager: ZDefendManager, navController: NavController) {
    val textState = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Linked") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { zDefendManager.preregisterLinkedFunction() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "updates")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                label = { Text("Enter Function Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    zDefendManager.registerLinkedFunction(textState.value)
                    textState.value = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            TextButton(
                onClick = {
                    zDefendManager.deregisterAllLinkedFunction()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unregister")
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(zDefendManager.linkedObjects) { linked ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = linked.label, fontSize = 18.sp)
                                Text(text = "${linked.threats.count()}", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            linked.threats.forEach { threat ->
                                Text(text = threat.name, fontSize = 16.sp)
                                Text(text = threat.description, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}