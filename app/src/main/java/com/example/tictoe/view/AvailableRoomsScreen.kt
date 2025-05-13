package com.example.tictoe.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tictoe.model.ConnectionState
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
    
    // IP thủ công và dialog
    var manualIpAddress by remember { mutableStateOf("") }
    var showIpInputDialog by remember { mutableStateOf(false) }
    
    // Lấy địa chỉ IP hiện tại
    val localIpAddress = remember { repository?.getCurrentIpAddress() ?: "IP not available" }
    
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
                text = "Create Room & Host Game",
                onClick = onCreateRoom,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Add
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Connect with IP button
            ModernOutlinedButton(
                text = "Connect Using IP Address",
                onClick = { showIpInputDialog = true },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Link
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
            
            // Hiển thị IP của người chơi để dễ dàng chia sẻ
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Device IP",
                        fontWeight = FontWeight.Bold,
                        color = AppColors.OnSurface,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localIpAddress,
                        fontSize = 20.sp,
                        color = AppColors.AccentYellow,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share this with friends to let them join your game",
                        fontSize = 14.sp,
                        color = AppColors.OnSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
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
                            text = "Create a new room or enter an IP manually",
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
                            localIpAddress = localIpAddress,
                            onJoin = { onJoinRoom(host) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Manual IP input dialog
        if (showIpInputDialog) {
            AlertDialog(
                onDismissRequest = { showIpInputDialog = false },
                title = {
                    Text(
                        text = "Enter IP Address",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter the IP address of the host player:",
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = manualIpAddress,
                            onValueChange = { manualIpAddress = it },
                            placeholder = { Text("Ex: 192.168.1.100") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.White.copy(alpha = 0.1f),
                                textColor = Color.White,
                                cursorColor = AppColors.AccentYellow,
                                focusedIndicatorColor = AppColors.AccentYellow
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualIpAddress.isNotEmpty()) {
                                onJoinRoom(manualIpAddress)
                                showIpInputDialog = false
                            }
                        },
                        enabled = manualIpAddress.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = AppColors.AccentYellow,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Connect")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showIpInputDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.DarkGray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                backgroundColor = AppColors.Surface,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun RoomItem(
    hostIp: String,
    localIpAddress: String,
    onJoin: () -> Unit
) {
    // Kiểm tra xem đây có phải là phòng của chính mình không
    val isOwnRoom = remember { hostIp == localIpAddress }
    
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
                                    if (isOwnRoom) AppColors.AccentYellow.copy(alpha = 0.7f) else AppColors.AccentPurple.copy(alpha = 0.7f),
                                    if (isOwnRoom) AppColors.AccentYellow else AppColors.Primary
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOwnRoom) Icons.Default.Person else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (isOwnRoom) Color.Black else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Host details
                Column {
                    Text(
                        text = if (isOwnRoom) "Your Game Room" else "Game Room",
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
            
            // Join button - không hiển thị nút Join nếu đây là phòng của chính mình
            if (!isOwnRoom) {
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
            } else {
                // Hiển thị "Current Host" nếu là phòng của chính mình
                Box(
                    modifier = Modifier
                        .background(
                            color = AppColors.AccentYellow.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Current Host",
                        color = AppColors.AccentYellow,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AvailableRoomsScreenPreview() {
    AvailableRoomsScreen()
} 