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
fun SettingsScreen(onBack: () -> Unit) {
    // Thiết lập trạng thái
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableIntStateOf(0) }
    var selectedDifficulty by remember { mutableIntStateOf(1) }
    var userName by remember { mutableStateOf("Player1") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF392A6A),
                        Color(0xFF352663),
                        Color(0xFF2F1F65)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color(0xFF4E3A8C).copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Tic Tac Toe",
                        color = Color(0xFF4EE6FA),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // GAME OPTIONS section
            SectionHeader(text = "GAME OPTIONS")
            
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                backgroundColor = Color(0xFF392A6A),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0xFF4EE6FA).copy(alpha = 0.2f)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Sound option
                    SimpleSettingItem(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Sound",
                        trailing = {
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { soundEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4EE6FA).copy(alpha = 0.7f),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                    
                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    // Vibration option
                    SimpleSettingItem(
                        icon = Icons.Default.Vibration,
                        title = "Vibration",
                        trailing = {
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = { vibrationEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4EE6FA).copy(alpha = 0.7f),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
                    
                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    
                    // Theme option
                    SimpleSettingItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFF4E3A8C).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable { }
                            ) {
                                Text(
                                    text = "Dark",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF4EE6FA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // GAMEPLAY section
            SectionHeader(text = "GAMEPLAY")
            
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                backgroundColor = Color(0xFF392A6A),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0xFF4EE6FA).copy(alpha = 0.2f)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bot icon
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "Bot Difficulty",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Difficulty radio buttons
                    SimpleDifficultyRadioGroup(
                        selected = selectedDifficulty,
                        onSelected = { selectedDifficulty = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // USER PROFILE section
            SectionHeader(text = "USER PROFILE")
            
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                backgroundColor = Color(0xFF392A6A),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0xFF4EE6FA).copy(alpha = 0.2f)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar circle with gradient background
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4E3A8C),
                                        Color(0xFF7B6FC6)
                                    )
                                )
                            )
                            .border(2.dp, Color(0xFF4EE6FA).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = userName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color(0xFF4EE6FA),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun SimpleSettingItem(
    icon: ImageVector,
    title: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        trailing()
    }
}

@Composable
fun SimpleDifficultyRadioGroup(selected: Int, onSelected: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Easy
        RadioButtonWithText(
            selected = selected == 0,
            onClick = { onSelected(0) },
            text = "Easy",
            color = Color(0xFFB0A9D1)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Medium
        RadioButtonWithText(
            selected = selected == 1,
            onClick = { onSelected(1) },
            text = "Medium",
            color = Color(0xFF4EE6FA)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Hard
        RadioButtonWithText(
            selected = selected == 2,
            onClick = { onSelected(2) },
            text = "Hard",
            color = Color(0xFFFF5252)
        )
    }
}

@Composable
fun RadioButtonWithText(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = color,
                unselectedColor = color.copy(alpha = 0.5f)
            ),
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = text,
            color = if (selected) color else color.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(onBack = {})
} 