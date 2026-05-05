package com.dev.diet018

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun LoadingScreen(zDefendManager: ZDefendManager, navController: NavController) {
    val isLoaded by zDefendManager.isLoaded
    val percentage by zDefendManager.percentage

    // Navigate to main screen as soon as the scan reaches 100 %
    LaunchedEffect(isLoaded) {
        if (isLoaded) {
            navController.navigate("zimperium") {
                popUpTo("loading") { inclusive = true }
            }
        }
    }

    // Smooth progress ring animation
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "scan_progress"
    )

    // Pulsing "Scanning…" label
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Branding
            Text(
                text = "ZDefend",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Mobile Threat Defense",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Circular progress ring with percentage in the centre
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(180.dp),
                    strokeWidth = 10.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "complete",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pulsing status text
            Text(
                text = "Scanning device…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = textAlpha)
            )
        }
    }
}

