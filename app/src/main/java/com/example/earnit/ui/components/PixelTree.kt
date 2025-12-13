package com.example.earnit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PixelTree(
    stage: Int, // 0-4
    health: Int, // 0-3
    themeIndex: Int, // 0=Purple, 1=Ocean, 2=Nature, 3=Sunset
    modifier: Modifier = Modifier,
    pixelSize: Dp = 8.dp
) {
    // Define Colors based on Theme and Health
    val trunkColor = if (health == 0) Color(0xFF4E4E4E) else Color(0xFF5D4037) // Grey if dead
    
    val leafColor = getLeafColor(themeIndex, health)

    Canvas(modifier = modifier) {
        val px = pixelSize.toPx()
        
        // Helper to draw a single pixel
        fun p(x: Int, y: Int, color: Color) {
            drawRect(
                color = color,
                topLeft = Offset(x * px, y * px),
                size = Size(px, px)
            )
        }

        // Center X roughly at index 10 (for a 20-wide grid)
        val cx = 10 
        val base = 20 // Ground level Y

        when(stage) {
            0 -> { // Seed
                p(cx, base, trunkColor)
                p(cx, base-1, leafColor)
            }
            1 -> { // Sprout
                p(cx, base, trunkColor)
                p(cx, base-1, trunkColor)
                p(cx-1, base-2, leafColor)
                p(cx+1, base-2, leafColor)
            }
            2 -> { // Sapling
                // Stem
                for(i in 0..3) p(cx, base-i, trunkColor)
                // Leaves
                p(cx-1, base-2, leafColor)
                p(cx-2, base-3, leafColor)
                p(cx+1, base-2, leafColor)
                p(cx+2, base-3, leafColor)
                p(cx, base-4, leafColor)
            }
            3 -> { // Medium
                // Trunk
                for(i in 0..5) p(cx, base-i, trunkColor)
                p(cx-1, base, trunkColor)
                p(cx+1, base, trunkColor)
                
                // Foliage Box
                for(x in cx-3..cx+3) {
                    for(y in base-8..base-4) {
                        if ((x+y)%2 == 0) p(x, y, leafColor) else p(x, y, leafColor.copy(alpha=0.8f))
                    }
                }
            }
            4 -> { // Tree (Matured)
                // Trunk
                for(i in 0..6) {
                    p(cx, base-i, trunkColor)
                    p(cx-1, base-i, trunkColor)
                }
                // Roots
                p(cx+1, base, trunkColor)
                p(cx-2, base, trunkColor)

                // Foliage Shape (Circle-ish)
                val radius = 5
                val centerY = base - 10
                for(x in cx-radius..cx+radius) {
                    for(y in centerY-radius..centerY+radius) {
                        // Simple distance check for circle approximation
                        val dx = x - cx
                        val dy = y - centerY
                        if (dx*dx + dy*dy <= radius*radius + 2) {
                             // Add some noise/shading
                             val color = if ((x*y)%3 == 0) leafColor.copy(alpha = 0.8f) else leafColor
                             p(x, y, color)
                        }
                    }
                }
                
                // Add specific "fruits" or flowers based on theme
                if (health > 1) {
                    val fruitColor = getFruitColor(themeIndex)
                    p(cx-2, centerY-2, fruitColor)
                    p(cx+2, centerY+1, fruitColor)
                    p(cx, centerY-4, fruitColor)
                }
            }
        }
    }
}

fun getLeafColor(themeIndex: Int, health: Int): Color {
    // Dead
    if (health == 0) return Color(0xFF686868)
    // Wilted (Brownish overlay)
    if (health < 3) return Color(0xFF8D6E63)

    return when(themeIndex) {
        0 -> Color(0xFF9C27B0) // Purple (Jacaranda)
        1 -> Color(0xFF00BCD4) // Ocean (Blue Spruce/Fantasy)
        2 -> Color(0xFF388E3C) // Nature (Green Oak)
        3 -> Color(0xFFE91E63) // Sunset (Cherry Blossom)
        else -> Color.Green
    }
}

fun getFruitColor(themeIndex: Int): Color {
    return when(themeIndex) {
        0 -> Color(0xFFE1BEE7) // Light Purple
        1 -> Color(0xFFB2EBF2) // Light Blue
        2 -> Color(0xFFC8E6C9) // Light Green
        3 -> Color(0xFFF8BBD0) // Pink
        else -> Color.Yellow
    }
}