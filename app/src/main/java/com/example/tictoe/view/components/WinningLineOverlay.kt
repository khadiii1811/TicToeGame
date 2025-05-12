package com.example.tictoe.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.example.tictoe.viewmodel.LineType
import com.example.tictoe.viewmodel.WinningLine

@Composable
fun WinningLineOverlay(line: WinningLine) {
    val lineAnimation = remember { Animatable(0f) }
    val lineColorAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(key1 = line) {
        lineAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
        lineColorAnimation.snapTo(0f)
        lineColorAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    
    val lineColor = lerp(
        Color(0xFFFFD600), 
        Color(0xFF4EE6FA),
        lineColorAnimation.value
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / 3
        val lineWidth = 10.dp.toPx()
        
        // Calculate start and end points
        val start = when (line.type) {
            LineType.ROW -> Offset(
                0f,
                (line.startRow * cellSize) + (cellSize / 2)
            )
            LineType.COLUMN -> Offset(
                (line.startCol * cellSize) + (cellSize / 2),
                0f
            )
            LineType.DIAGONAL_DOWN -> Offset(0f, 0f)
            LineType.DIAGONAL_UP -> Offset(0f, size.height)
        }
        
        val end = when (line.type) {
            LineType.ROW -> Offset(
                size.width * lineAnimation.value,
                (line.startRow * cellSize) + (cellSize / 2)
            )
            LineType.COLUMN -> Offset(
                (line.startCol * cellSize) + (cellSize / 2),
                size.height * lineAnimation.value
            )
            LineType.DIAGONAL_DOWN -> Offset(
                size.width * lineAnimation.value,
                size.height * lineAnimation.value
            )
            LineType.DIAGONAL_UP -> Offset(
                size.width * lineAnimation.value,
                size.height - (size.height * lineAnimation.value)
            )
        }
        
        // Draw line
        drawLine(
            color = lineColor,
            start = start,
            end = end,
            strokeWidth = lineWidth,
            cap = StrokeCap.Round
        )
    }
} 