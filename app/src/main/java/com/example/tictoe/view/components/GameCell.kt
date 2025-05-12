package com.example.tictoe.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun GameCell(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Cell appears
    val animatedAlpha = remember { Animatable(0f) }
    val animatedScale = remember { Animatable(0.8f) }
    
    // Highlight pulsing
    val highlightAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(key1 = value) {
        if (value.isNotEmpty()) {
            launch {
                animatedAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(300)
                )
            }
            launch {
                animatedScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        } else {
            animatedAlpha.snapTo(0f)
            animatedScale.snapTo(0.8f)
        }
    }
    
    // Highlight animation effect
    LaunchedEffect(key1 = isHighlighted) {
        if (isHighlighted) {
            highlightAlpha.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            highlightAlpha.snapTo(0f)
        }
    }
    
    val cellColor = if (isHighlighted) {
        lerp(
            Color(0xFF563FAF),
            if (value == "X") Color(0xFFFFD600) else Color(0xFF4EE6FA),
            highlightAlpha.value * 0.3f
        )
    } else {
        Color(0xFF563FAF)
    }
    
    val borderColor = if (isHighlighted) {
        lerp(
            Color(0xFF6A4FC6),
            if (value == "X") Color(0xFFFFD600) else Color(0xFF4EE6FA),
            highlightAlpha.value
        )
    } else {
        Color(0xFF6A4FC6)
    }
    
    Box(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .shadow(
                elevation = if (isHighlighted) 12.dp else 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isHighlighted) {
                    (if (value == "X") Color(0xFFFFD600) else Color(0xFF4EE6FA)).copy(alpha = 0.7f)
                } else {
                    Color(0xFF6A4FC6).copy(alpha = 0.7f)
                }
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        cellColor,
                        Color(0xFF4E3A8C)
                    )
                )
            )
            .border(
                width = 2.dp, 
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                enabled = value.isEmpty(),
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .alpha(animatedAlpha.value)
                    .scale(animatedScale.value)
            ) {
                Text(
                    text = value,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (value == "X") Color(0xFFFFD600) else Color(0xFF4EE6FA)
                )
            }
        }
    }
} 