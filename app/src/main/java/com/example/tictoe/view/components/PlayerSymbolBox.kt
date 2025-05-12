package com.example.tictoe.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerSymbolBox(
    symbol: String,
    isActive: Boolean,
    symbolColor: Color,
    borderColor: Color
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale = if (isActive) {
        pulseAnim.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        ).value
    } else 1f
    
    val shadowAlpha = if (isActive) {
        pulseAnim.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shadowAlpha"
        ).value
    } else 0.2f
    
    Box(
        modifier = Modifier
            .size(68.dp)
            .shadow(
                elevation = if (isActive) 12.dp else 4.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = symbolColor.copy(alpha = shadowAlpha)
            )
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4B357A),
                        Color(0xFF3F2C77)
                    )
                )
            )
            .border(
                width = if (isActive) 2.5.dp else 0.dp,
                color = if (isActive) borderColor else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = symbolColor
        )
    }
} 