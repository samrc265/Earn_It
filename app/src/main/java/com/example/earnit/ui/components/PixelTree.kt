package com.example.earnit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.earnit.model.TreeType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PixelTree(
    stage: Int, // 0=Seed, 1=Sapling, 2=Small, 3=Medium, 4=Mature
    type: TreeType,
    seed: Long,
    health: Int = 3,
    modifier: Modifier = Modifier,
    pixelSize: Dp? = null // If null, auto-scales to fit container
) {
    // --- Colors ---
    val trunkColor = if (health == 0) Color(0xFF6D6D6D) else when(type) {
        TreeType.CACTUS -> Color(0xFF33691E) // Darker green for cactus body
        else -> Color(0xFF5D4037) // Wood brown
    }

    val leafColor = if (health == 0) Color(0xFF9E9E9E) else if (health < 3) Color(0xFFA1887F) else when(type) {
        TreeType.PINE -> Color(0xFF1B5E20)
        TreeType.CHERRY_BLOSSOM -> Color(0xFFF8BBD0)
        TreeType.CACTUS -> Color(0xFF558B2F)
        TreeType.ORANGE -> Color(0xFF2E7D32)
    }

    val fruitColor = if (health < 3) Color.Transparent else when(type) {
        TreeType.ORANGE -> Color(0xFFFF9800)
        TreeType.CHERRY_BLOSSOM -> Color(0xFFE91E63)
        TreeType.CACTUS -> Color(0xFFD81B60)
        else -> Color.Transparent
    }

    Canvas(modifier = modifier) {
        // Virtual Grid Resolution
        val vW = 64f
        val vH = 64f

        // Calculate scale to fit container while maintaining aspect ratio
        val scale = if (pixelSize != null) {
            pixelSize.toPx()
        } else {
            kotlin.math.min(size.width / vW, size.height / vH)
        }

        // Center the grid in the canvas
        val offsetX = (size.width - (vW * scale)) / 2f
        val offsetY = (size.height - (vH * scale)) / 2f

        withTransform({
            translate(left = offsetX, top = offsetY)
            // FIX: Explicitly set both scaleX and scaleY
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
            val rng = Random(seed + stage)
            val cx = 32
            val base = 60

            // Helper to draw a pixel on the virtual grid
            fun p(x: Int, y: Int, color: Color) {
                drawRect(
                    color = color,
                    topLeft = Offset(x.toFloat(), y.toFloat()),
                    size = Size(1f, 1f)
                )
            }

            if (stage == 0) {
                // Seed
                drawSeed(cx, base, trunkColor, leafColor, ::p)
                return@withTransform
            }

            if (stage == 1) {
                // Sapling
                drawSapling(cx, base, trunkColor, leafColor, ::p)
                return@withTransform
            }

            // --- STAGE 2, 3, 4 GENERATION ---

            // Generate Trunk Geometry first (Curvature, Height)
            val trunkHeight = when(stage) {
                2 -> rng.nextInt(8, 12)
                3 -> rng.nextInt(14, 20)
                else -> rng.nextInt(20, 28)
            }

            // Trunk Lean/Curve
            val curveFactor = rng.nextFloat() * 4 - 2 // -2 to +2 curve
            val trunkPath = mutableListOf<Pair<Int, Int>>()

            // Draw Trunk
            if (type != TreeType.CACTUS) {
                val trunkThickness = if (stage == 4) 3 else 2
                for (y in 0 until trunkHeight) {
                    val progress = y.toFloat() / trunkHeight
                    val dx = (sin(progress * Math.PI) * curveFactor).toInt()
                    val tx = cx + dx
                    val ty = base - y

                    trunkPath.add(tx to ty)

                    // Main trunk pixels
                    for (w in -trunkThickness/2..trunkThickness/2) {
                        p(tx + w, ty, trunkColor)
                    }

                    // Root flare at bottom
                    if (y < 2 && stage > 2) {
                        p(tx - trunkThickness, ty, trunkColor)
                        p(tx + trunkThickness + 1, ty, trunkColor)
                    }
                }
            }

            val topX = if(trunkPath.isNotEmpty()) trunkPath.last().first else cx
            val topY = if(trunkPath.isNotEmpty()) trunkPath.last().second else base

            when (type) {
                TreeType.PINE -> drawPine(stage, topX, topY, trunkHeight, leafColor, rng, ::p)
                TreeType.CHERRY_BLOSSOM -> drawBroadleaf(stage, topX, topY, trunkPath, leafColor, fruitColor, isCherry = true, rng, ::p, trunkColor)
                TreeType.ORANGE -> drawBroadleaf(stage, topX, topY, trunkPath, leafColor, fruitColor, isCherry = false, rng, ::p, trunkColor)
                TreeType.CACTUS -> drawCactus(stage, cx, base, leafColor, fruitColor, rng, ::p)
            }
        }
    }
}

// --- Drawing Sub-routines ---

fun drawSeed(cx: Int, base: Int, trunk: Color, leaf: Color, p: (Int, Int, Color) -> Unit) {
    p(cx, base, trunk)
    p(cx-1, base, trunk)
    p(cx+1, base, trunk)
    p(cx, base-1, leaf)
    p(cx, base-2, leaf)
}

fun drawSapling(cx: Int, base: Int, trunk: Color, leaf: Color, p: (Int, Int, Color) -> Unit) {
    for(i in 0..4) p(cx, base-i, trunk)
    p(cx-1, base-3, leaf)
    p(cx+1, base-3, leaf)
    p(cx, base-5, leaf)
    p(cx-1, base-4, leaf)
}

fun drawPine(
    stage: Int,
    topX: Int,
    topY: Int,
    trunkH: Int,
    leafColor: Color,
    rng: Random,
    p: (Int, Int, Color) -> Unit
) {
    // Pines have triangular tiers.
    val tiers = if (stage == 2) 3 else if (stage == 3) 5 else 7
    // Start drawing foliage from about 1/3rd up the trunk
    val startY = topY + (trunkH / 2)

    var currentY = startY

    for (t in 0 until tiers) {
        val tierWidth = (tiers - t) * (if (stage==4) 3 else 2) + rng.nextInt(2)
        val tierHeight = 3 + rng.nextInt(2)

        // Draw a triangle for this tier
        for (h in 0 until tierHeight) {
            val y = currentY - h
            val w = tierWidth - (h * (tierWidth.toFloat()/tierHeight)).toInt()
            for (x in topX - w..topX + w) {
                // Add noise to edges
                if (rng.nextFloat() > 0.15 || abs(x-topX) < w-1) {
                    p(x, y, leafColor)
                }
            }
        }
        // Move up for next tier
        currentY -= (tierHeight - 1)
    }
    // Top tip
    p(topX, currentY, leafColor)
    p(topX, currentY-1, leafColor)
}

fun drawBroadleaf(
    stage: Int,
    topX: Int,
    topY: Int,
    trunkPath: List<Pair<Int, Int>>,
    leafColor: Color,
    fruitColor: Color,
    isCherry: Boolean,
    rng: Random,
    p: (Int, Int, Color) -> Unit,
    trunkColor: Color
) {
    // 1. Draw Branches (Fractal-lite)
    val branchTips = mutableListOf<Pair<Int, Int>>()
    branchTips.add(topX to topY)

    // Add some random branches coming off the main trunk
    if (stage > 2) {
        val branchCount = if (stage == 4) 4 else 2
        for (i in 0 until branchCount) {
            val originIndex = rng.nextInt(trunkPath.size / 2, trunkPath.size)
            if (originIndex < trunkPath.size) {
                val origin = trunkPath[originIndex]
                var bx = origin.first
                var by = origin.second
                val dir = if (rng.nextBoolean()) 1 else -1
                val len = rng.nextInt(4, 10)

                for (b in 0 until len) {
                    bx += dir
                    by -= 1
                    p(bx, by, trunkColor)
                }
                branchTips.add(bx to by)
            }
        }
    }

    // 2. Draw Leaves around tips
    val clusterSize = if (stage == 4) 7 else if (stage == 3) 5 else 3

    for (tip in branchTips) {
        val cx = tip.first
        val cy = tip.second

        // Draw a noisy circle/cloud around each tip
        for (y in cy - clusterSize..cy + clusterSize) {
            for (x in cx - clusterSize..cx + clusterSize) {
                val dx = x - cx
                val dy = y - cy
                val dist = dx*dx + dy*dy

                // Circle equation with noise
                if (dist < (clusterSize * clusterSize) - rng.nextInt(5)) {
                    // Check if we should draw fruit (only on edges or sparse inside)
                    val isFruit = stage == 4 && rng.nextFloat() > (if(isCherry) 0.90 else 0.95)
                    p(x, y, if (isFruit) fruitColor else leafColor)
                }
            }
        }
    }
}

fun drawCactus(
    stage: Int,
    cx: Int,
    base: Int,
    bodyColor: Color,
    flowerColor: Color,
    rng: Random,
    p: (Int, Int, Color) -> Unit
) {
    val h = if (stage == 2) 15 else if (stage == 3) 25 else 35
    val w = 4

    // Main Body
    for (x in cx - w/2..cx + w/2) {
        for (y in base - h..base) {
            p(x, y, bodyColor)
            // Ribs/Texture
            if (x == cx - 1 || x == cx + 1) {
                p(x, y, bodyColor.copy(alpha = 0.8f)) // Shadow/Texture
            }
            // Spikes
            if (rng.nextFloat() > 0.95) {
                val side = if (x < cx) -1 else 1
                p(x + side, y, Color.LightGray)
            }
        }
    }

    // Arms
    if (stage > 2) {
        val arms = if (stage == 3) 1 else 2
        for (i in 0 until arms) {
            val dir = if (i == 0) -1 else 1
            val armY = base - rng.nextInt(10, h - 5)
            val armLen = rng.nextInt(4, 8)
            val armH = rng.nextInt(5, 12)

            // Horizontal part
            for (dx in 0..armLen) {
                val tx = cx + (w/2 * dir) + (dx * dir)
                for (ty in armY..armY+w-1) p(tx, ty, bodyColor)
            }
            // Vertical part
            val elbowX = cx + (w/2 * dir) + (armLen * dir)
            for (dy in 0..armH) {
                val ty = armY - dy
                for (dx in 0 until w) p(elbowX + (dx*dir), ty, bodyColor)
            }

            // Flower on arm
            if (stage == 4) {
                val tipY = armY - armH
                p(elbowX + dir, tipY - 1, flowerColor)
                p(elbowX + dir*2, tipY - 1, flowerColor)
                p(elbowX + dir, tipY - 2, flowerColor)
            }
        }
    }

    // Main Flower
    if (stage == 4) {
        val topY = base - h
        for (x in cx-2..cx+2) p(x, topY-1, flowerColor)
        for (x in cx-1..cx+1) p(x, topY-2, flowerColor)
        p(cx, topY-3, flowerColor)
    }
}