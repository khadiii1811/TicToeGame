package com.example.tictoe.view.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

fun DrawScope.drawTitleGlow(color: Color) {
    drawCircle(
        color = color,
        radius = 100f,
        center = Offset(size.width / 2, size.height / 2),
        style = Stroke(width = 50f)
    )
} 