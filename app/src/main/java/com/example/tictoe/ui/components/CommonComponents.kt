package com.example.tictoe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictoe.ui.theme.AppColors
/**
 * Header với nút back 
 */
@Composable
fun AppHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Back button với animation nhẹ
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    spotColor = AppColors.AccentPurple.copy(alpha = 0.5f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.SurfaceLight,
                            AppColors.Surface
                        )
                    ),
                    shape = CircleShape
                )
                .align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.AccentYellow
            )
        }
        
        // Title với hiệu ứng shadow
        Text(
            text = title,
            color = AppColors.OnSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Button hiện đại với hiệu ứng gradient
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    brush: Brush = AppColors.AccentGradient,
    textColor: Color = Color.Black,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = AppColors.AccentPurple.copy(alpha = 0.5f)
            ),
        contentPadding = PaddingValues(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) brush else Brush.linearGradient(
                        colors = listOf(
                            Color.Gray.copy(alpha = 0.5f),
                            Color.Gray.copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Outlined button với phong cách hiện đại
 */
@Composable
fun ModernOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = AppColors.AccentPurple.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    AppColors.AccentYellow.copy(alpha = 0.7f),
                    AppColors.AccentPurple.copy(alpha = 0.7f)
                )
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = AppColors.Surface.copy(alpha = 0.5f),
            contentColor = AppColors.OnSurface
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.AccentYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Card hiện đại với hiệu ứng
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = AppColors.AccentPurple.copy(alpha = 0.5f)
            ),
        backgroundColor = AppColors.CardBackground,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.2f),
                    AppColors.AccentPurple.copy(alpha = 0.2f)
                )
            )
        ),
        elevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.SurfaceLight.copy(alpha = 0.7f),
                            AppColors.SurfaceLight.copy(alpha = 0.4f)
                        ),
                        radius = 800f
                    )
                )
        ) {
            content()
        }
    }
}

/**
 * Avatar người dùng đẹp
 */
@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    size: Int = 64
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                spotColor = AppColors.AccentPurple.copy(alpha = 0.3f)
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppColors.AccentPurple.copy(alpha = 0.4f),
                        AppColors.Primary
                    )
                ),
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "User Avatar",
            tint = Color.White,
            modifier = Modifier.size((size * 0.6).dp)
        )
    }
} 