package com.example.earnit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.earnit.viewmodel.MainViewModel

@Composable
fun RewardsScreen(viewModel: MainViewModel) {
    val score by viewModel.score.collectAsStateWithLifecycle()

    // --- Logic: Battle Pass Style ---
    val rewardPoints = score / 100
    val progressToNext = (score % 100) / 100f

    // --- Animations ---

    // Smooth progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = progressToNext,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    // Counting animation for the points text
    val animatedPoints by animateIntAsState(
        targetValue = rewardPoints,
        animationSpec = tween(durationMillis = 800),
        label = "PointsCounter"
    )

    // Star Scale Animation (Pop effect when points change)
    val infiniteTransition = rememberInfiniteTransition(label = "StarAmbient")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    // Background Rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "Rotation"
    )

    // FIX: Capture the color here (in Composable scope) so we can use it inside Canvas (DrawScope)
    val glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Hero Section: The Star
                Box(contentAlignment = Alignment.Center) {
                    // Rotating Glow Effect
                    Canvas(modifier = Modifier.size(180.dp).rotate(rotation)) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    glowColor, // Used the captured variable here
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 40f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700), // Gold
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Points Counter (The Focus)
                Text(
                    text = "$animatedPoints",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    "Reward Points",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Battle Pass Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Next Point",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            "${(score % 100)} / 100 XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}