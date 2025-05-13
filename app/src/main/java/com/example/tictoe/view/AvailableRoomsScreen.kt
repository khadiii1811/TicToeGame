package com.example.tictoe.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tictoe.model.OnlineGameRepository
import com.example.tictoe.ui.components.*
import com.example.tictoe.ui.theme.AppColors
import com.example.tictoe.ui.theme.GlowingBackgroundEffect

/**
 * Màn hình hiển thị danh sách phòng có sẵn để tham gia
 */
@Composable
fun AvailableRoomsScreen(
    onBack: () -> Unit = {},
    onCreateRoom: () -> Unit = {},
    onJoinRoom: (String) -> Unit = {},
    repository: OnlineGameRepository? = null
) {
    // Danh sách các host được phát hiện
    val discoveredHosts by repository?.discoveredHosts?.collectAsState() ?: remember { mutableStateOf(emptyList<String>()) }
    val connectionState by repository?.connectionState?.collectAsState() ?: remember { mutableStateOf(null) }
    var isScanning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Bắt đầu quét khi màn hình được hiển thị
    LaunchedEffect(Unit) {
        repository?.discoverHosts()
        isScanning = true
        
        // Giả lập thời gian quét
        delay(5000)
        isScanning = false
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
                title = "Available Rooms",
                onBackClick = onBack
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Create room button
            GradientButton(
                text = "Create New Room",
                onClick = onCreateRoom,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Add
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Refresh button
            ModernOutlinedButton(
                text = "Refresh Room List",
                onClick = { 
                    isScanning = true
                    repository?.discoverHosts()
                    coroutineScope.launch {
                        delay(5000)
                        isScanning = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Refresh
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status text
            if (isScanning) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AppColors.AccentYellow,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scanning for available rooms...",
                        color = AppColors.OnSurface,
                        fontSize = 14.sp
                    )
                }
            } else if (discoveredHosts.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AppColors.Info,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "No rooms found",
                            color = AppColors.OnSurface,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "Create a new room or refresh the list",
                            color = AppColors.OnSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Room list
            if (discoveredHosts.isNotEmpty()) {
                Text(
                    text = "Available Rooms",
                    color = AppColors.OnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Start
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(discoveredHosts) { host ->
                        RoomItem(
                            hostIp = host,
                            onJoin = { onJoinRoom(host) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RoomItem(
    hostIp: String,
    onJoin: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Host info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Host icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppColors.AccentPurple.copy(alpha = 0.7f),
                                    AppColors.Primary
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Host details
                Column {
                    Text(
                        text = "Game Room",
                        color = AppColors.OnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = hostIp,
                        color = AppColors.OnSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Join button
            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppColors.AccentYellow,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Join",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AvailableRoomsScreenPreview() {
    AvailableRoomsScreen()
} 