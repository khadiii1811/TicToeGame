package com.example.tictoe.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Bảng màu hiện đại cho ứng dụng
 */
object AppColors {
    // Màu chính
    val PrimaryDark = Color(0xFF1A0E48)
    val Primary = Color(0xFF2D1980)
    val PrimaryLight = Color(0xFF4326B2)
    
    // Màu phụ
    val AccentYellow = Color(0xFFFFD54F)
    val AccentPurple = Color(0xFF7C4DFF)
    val AccentPink = Color(0xFFFF4081)
    
    // Màu nền và surface
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1F1736)
    val SurfaceLight = Color(0xFF2F2154)
    
    // Màu nội dung
    val OnPrimary = Color.White
    val OnSurface = Color.White
    val OnAccent = Color(0xFF121212)
    
    // Gradient chính của ứng dụng
    val MainGradient = Brush.verticalGradient(
        colors = listOf(
            PrimaryDark,
            Primary,
            PrimaryLight
        )
    )
    
    // Gradient cho button chính
    val AccentGradient = Brush.horizontalGradient(
        colors = listOf(
            AccentYellow,
            AccentPink.copy(alpha = 0.8f)
        )
    )
    
    // Cards và các phần tử UI
    val CardBackground = SurfaceLight.copy(alpha = 0.7f)
    val CardBorder = AccentPurple.copy(alpha = 0.3f)
    
    // State colors
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFFF5252)
    val Warning = Color(0xFFFFB74D)
    val Info = Color(0xFF64B5F6)
} 