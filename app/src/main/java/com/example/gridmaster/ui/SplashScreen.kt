package com.example.gridmaster.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridmaster.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    // Animation State
    val scale = remember { Animatable(0f) }

    // Effect: Animate and then navigate
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    android.view.animation.OvershootInterpolator(2f).getInterpolation(it)
                }
            )
        )
        delay(1500) // Wait for 1.5 seconds
        onAnimationFinished()
    }

    // UI Design
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Professional Gradient Background
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D47A1), // Deep Blue
                        Color(0xFF1976D2), // Primary Blue
                        Color(0xFF42A5F5)  // Light Blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // The Logo (Using the XML we just created)
            Image(
                painter = painterResource(id = R.drawable.ic_grid_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // The App Name
            Text(
                text = "GridMaster",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(scale.value)
            )

            Text(
                text = "OPTCL Field Assistant",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

