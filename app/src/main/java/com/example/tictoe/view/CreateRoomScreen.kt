package com.example.tictoe.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.tictoe.model.OnlineGameRepository
import com.example.tictoe.ui.components.*
import com.example.tictoe.ui.theme.AppColors
import com.example.tictoe.ui.theme.GlowingBackgroundEffect

/**
 * Screen for creating a new game room
 */
@Composable
fun CreateRoomScreen(
    onBack: () -> Unit = {},
    onRoomCreated: () -> Unit = {},
    repository: OnlineGameRepository? = null
) {
    var roomName by remember { mutableStateOf("") }
    var maxPlayers by remember { mutableStateOf("2") }
    var isCreating by remember { mutableStateOf(false) }
    
    // Add manual IP address field
    var manualIpAddress by remember { mutableStateOf("") }
    var useManualIp by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // IP address for sharing
    val localIpAddress = remember { repository?.getCurrentIpAddress() ?: "IP not available" }
    
    // Set the manual IP in the repository when the checkbox is toggled
    LaunchedEffect(useManualIp, manualIpAddress) {
        if (useManualIp && manualIpAddress.isNotEmpty()) {
            repository?.setManualHostIp(manualIpAddress)
        } else if (!useManualIp) {
            repository?.setManualHostIp(null)
        }
    }

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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header with back button
            AppHeader(
                title = "Create New Room",
                onBackClick = onBack
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Room information form
            Card(
                backgroundColor = AppColors.Surface,
                shape = RoundedCornerShape(16.dp),
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Room Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Room name
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text(text = "Room Name") },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = AppColors.AccentYellow,
                            focusedLabelColor = AppColors.AccentYellow,
                            cursorColor = AppColors.AccentYellow,
                            unfocusedBorderColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Max players
                    OutlinedTextField(
                        value = maxPlayers,
                        onValueChange = { maxPlayers = it },
                        label = { Text(text = "Max Players") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = AppColors.AccentYellow,
                            focusedLabelColor = AppColors.AccentYellow,
                            cursorColor = AppColors.AccentYellow,
                            unfocusedBorderColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Server IP Address Section
                    Text(
                        text = "Server Address",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Auto-detected IP Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-detected IP: ",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                        
                        Text(
                            text = localIpAddress,
                            fontSize = 14.sp,
                            color = if (useManualIp) Color.Gray else AppColors.AccentYellow,
                            fontWeight = if (useManualIp) FontWeight.Normal else FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Manual IP Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useManualIp,
                            onCheckedChange = { useManualIp = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppColors.AccentYellow,
                                uncheckedColor = Color.Gray
                            )
                        )
                        
                        Text(
                            text = "Use manual IP address",
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.clickable { useManualIp = !useManualIp }
                        )
                    }
                    
                    // Manual IP input field (only shown when checkbox is checked)
                    AnimatedVisibility(visible = useManualIp) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = manualIpAddress,
                                onValueChange = { manualIpAddress = it },
                                label = { Text(text = "Enter Host IP Address") },
                                placeholder = { Text(text = "e.g. 192.168.1.2") },
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = Color.White,
                                    focusedBorderColor = AppColors.AccentYellow,
                                    focusedLabelColor = AppColors.AccentYellow,
                                    cursorColor = AppColors.AccentYellow,
                                    unfocusedBorderColor = Color.Gray,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Use your actual WiFi IP (e.g. 192.168.1.2) when hosting from an emulator",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Add helper text
            Text(
                text = "If running on an emulator, enter your computer's WiFi IP address (e.g. 192.168.1.2)",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create Room Button
            GradientButton(
                text = if (isCreating) "Creating Room..." else "Create Room",
                onClick = {
                    if (roomName.isNotEmpty() && maxPlayers.isNotEmpty() && !isCreating) {
                        isCreating = true
                        val maxPlayersNum = maxPlayers.toIntOrNull() ?: 2
                        
                        // Set the manual IP if checked before creating room
                        if (useManualIp && manualIpAddress.isNotEmpty()) {
                            repository?.setManualHostIp(manualIpAddress)
                        } else {
                            repository?.setManualHostIp(null)
                        }
                        
                        coroutineScope.launch {
                            repository?.createRoom(roomName, maxPlayersNum)
                            isCreating = false
                            onRoomCreated()
                        }
                    }
                },
                enabled = roomName.isNotEmpty() && maxPlayers.isNotEmpty() && !isCreating && 
                         (!useManualIp || (useManualIp && manualIpAddress.isNotEmpty())),
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Check
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Loading overlay
        if (isCreating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    backgroundColor = AppColors.Surface,
                    shape = RoundedCornerShape(16.dp),
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AppColors.AccentYellow)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Creating your room...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
} 