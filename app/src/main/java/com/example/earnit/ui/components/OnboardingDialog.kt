package com.example.earnit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun OnboardingDialog(onDismiss: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to Earn It",
            description = "Gamify your life! Complete tasks to earn XP and level up your productivity.",
            icon = Icons.AutoMirrored.Filled.List
        ),
        OnboardingPage(
            title = "Progress & Rewards",
            description = "Every 100 XP you earn converts into 1 Reward Point (★). Use these points to redeem real-life treats!",
            icon = Icons.Default.Star
        ),
        OnboardingPage(
            title = "Grow Your Plant",
            description = "Consistency is key. Complete 2/3rds of your Daily Quests to water your plant. Don't let it wilt!",
            icon = Icons.Default.Face
        ),
        OnboardingPage(
            title = "Collect Your Forest",
            description = "Keep a fully grown tree alive for 3 days to add it to your permanent collection. Good luck!",
            icon = Icons.Default.Edit // Using edit/log as placeholder for forest if needed
        )
    )

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth().height(400.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Icon(
                        imageVector = pages[currentPage].icon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = pages[currentPage].title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = pages[currentPage].description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page Indicator
                    Row {
                        pages.forEachIndexed { index, _ ->
                            val color = if (index == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            Surface(
                                modifier = Modifier.padding(4.dp).size(10.dp),
                                shape = CircleShape,
                                color = color
                            ) {}
                        }
                    }

                    // Button
                    Button(
                        onClick = {
                            if (currentPage < pages.size - 1) {
                                currentPage++
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Text(if (currentPage == pages.size - 1) "Get Started" else "Next")
                    }
                }
            }
        }
    }
}

data class OnboardingPage(val title: String, val description: String, val icon: ImageVector)