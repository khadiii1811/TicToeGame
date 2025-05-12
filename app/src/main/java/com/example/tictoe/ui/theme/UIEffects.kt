package com.example.tictoe.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hiệu ứng và animation cho UI
 */

// Border với hiệu ứng gradient
fun Modifier.gradientBorder(
    width: Dp = 2.dp,
    brush: Brush = Brush.linearGradient(
        listOf(
            AppColors.AccentYellow,
            AppColors.AccentPurple,
            AppColors.AccentPink
        )
    ),
    shape: Shape = RoundedCornerShape(16.dp)
) = composed {
    this.then(
        Modifier
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    brush = brush,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .padding(width)
    )
}

// Hiệu ứng shimmer loading
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            AppColors.Surface.copy(alpha = 0.2f),
            AppColors.Surface.copy(alpha = 0.5f),
            AppColors.Surface.copy(alpha = 0.2f)
        ),
        start = Offset(-translateAnim, 0f),
        end = Offset(translateAnim, 0f),
        tileMode = TileMode.Clamp
    )

    this.then(background(shimmerBrush))
}

// Hiệu ứng nhấp nháy
@Composable
fun PulseEffect(
    modifier: Modifier = Modifier,
    pulseFraction: Float = 1.2f,
    durationMillis: Int = 600,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseFraction,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier.scale(scale)) {
        content()
    }
}

// Hiệu ứng nền đẹp với các hình tròn bay lượn
@Composable
fun GlowingBackgroundEffect(
    modifier: Modifier = Modifier,
    mainColor: Color = AppColors.Primary
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier
            .drawBehind {
                rotate(rotation) {
                    drawCircle(
                        color = mainColor.copy(alpha = 0.15f),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * 0.2f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = AppColors.AccentPurple.copy(alpha = 0.1f),
                        radius = size.width * 0.4f,
                        center = Offset(size.width * 0.8f, size.height * 0.3f)
                    )
                    drawCircle(
                        color = AppColors.AccentPink.copy(alpha = 0.08f),
                        radius = size.width * 0.5f,
                        center = Offset(size.width * 0.5f, size.height * 0.8f)
                    )
                }
            }
    )
} 