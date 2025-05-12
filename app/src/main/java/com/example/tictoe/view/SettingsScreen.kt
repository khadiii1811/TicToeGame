package com.example.tictoe.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictoe.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.tictoe.viewmodel.SettingsViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.tictoe.ui.components.*
import com.example.tictoe.ui.theme.AppColors
import com.example.tictoe.ui.theme.GlowingBackgroundEffect

// Define ParticleData class at file level
private data class SettingsParticleData(
    val xPos: Float, 
    val yPos: Float, 
    val particleSize: Float, 
    val alphaValue: Float, 
    val xSpeed: Float, 
    val ySpeed: Float
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel
) {
    // Collect state from ViewModel
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    val aiDifficulty by viewModel.aiDifficulty.collectAsState()
    val username by viewModel.username.collectAsState()
    
    // State for username edit
    var isEditingUsername by remember { mutableStateOf(false) }
    var editedUsername by remember { mutableStateOf(username) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = AppColors.MainGradient
            )
    ) {
        // Add glowing background effect
        GlowingBackgroundEffect(modifier = Modifier.fillMaxSize())
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with back button
            AppHeader(
                title = "Settings",
                onBackClick = onBack
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Settings Cards
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Game Settings",
                        color = AppColors.AccentYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sound Toggle
                    SettingsToggleItem(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Sound",
                        isChecked = isSoundEnabled,
                        onToggle = { viewModel.toggleSound() }
                    )
                    
                    Divider(color = AppColors.CardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    
                    // Vibration Toggle
                    SettingsToggleItem(
                        icon = Icons.Default.Vibration,
                        title = "Vibration",
                        isChecked = isVibrationEnabled,
                        onToggle = { viewModel.toggleVibration() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI Difficulty Settings
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "AI Difficulty",
                        color = AppColors.AccentYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (aiDifficulty) {
                                1 -> "Easy"
                                2 -> "Medium"
                                3 -> "Hard"
                                else -> "Medium"
                            },
                            color = AppColors.OnSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Difficulty slider
                        Slider(
                            value = aiDifficulty.toFloat(),
                            onValueChange = { viewModel.setAiDifficulty(it.toInt()) },
                            valueRange = 1f..3f,
                            steps = 1,
                            colors = SliderDefaults.colors(
                                thumbColor = AppColors.AccentYellow,
                                activeTrackColor = AppColors.AccentYellow,
                                inactiveTrackColor = AppColors.CardBorder
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Difficulty labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Easy",
                                color = if (aiDifficulty == 1) AppColors.AccentYellow else AppColors.OnSurface.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Medium",
                                color = if (aiDifficulty == 2) AppColors.AccentYellow else AppColors.OnSurface.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Hard",
                                color = if (aiDifficulty == 3) AppColors.AccentYellow else AppColors.OnSurface.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Profile Settings
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "User Profile",
                        color = AppColors.AccentYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User avatar
                        UserAvatar(size = 60)
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Username field
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Username",
                                color = AppColors.OnSurface.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            
                            if (isEditingUsername) {
                                TextField(
                                    value = editedUsername,
                                    onValueChange = { editedUsername = it },
                                    colors = TextFieldDefaults.textFieldColors(
                                        backgroundColor = Color.Transparent,
                                        textColor = AppColors.OnSurface,
                                        cursorColor = AppColors.AccentYellow,
                                        focusedIndicatorColor = AppColors.AccentYellow,
                                        unfocusedIndicatorColor = AppColors.CardBorder
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = username,
                                    color = AppColors.OnSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Edit/Save button
                        IconButton(
                            onClick = {
                                if (isEditingUsername) {
                                    if (editedUsername.isNotBlank()) {
                                        viewModel.setUsername(editedUsername)
                                    }
                                    isEditingUsername = false
                                } else {
                                    isEditingUsername = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isEditingUsername) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditingUsername) "Save" else "Edit",
                                tint = AppColors.AccentYellow
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // About section
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "About",
                        color = AppColors.AccentYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Tic Tac Toe",
                        color = AppColors.OnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Version 1.0.0",
                        color = AppColors.OnSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "© 2023 Tic Tac Toe Team",
                        color = AppColors.OnSurface.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onToggle
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isChecked) AppColors.AccentYellow.copy(alpha = 0.2f) else AppColors.SurfaceLight,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isChecked) AppColors.AccentYellow else AppColors.OnSurface.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                color = AppColors.OnSurface,
                fontSize = 16.sp
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.AccentYellow,
                checkedTrackColor = AppColors.AccentYellow.copy(alpha = 0.5f),
                uncheckedThumbColor = AppColors.OnSurface.copy(alpha = 0.7f),
                uncheckedTrackColor = AppColors.CardBorder
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(viewModel = SettingsViewModel())
} 