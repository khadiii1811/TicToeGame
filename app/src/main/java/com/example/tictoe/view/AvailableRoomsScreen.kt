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
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tictoe.model.ConnectionState
import com.example.tictoe.model.OnlineGameRepository
import com.example.tictoe.model.RoomInfo
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
    val availableRooms by repository?.availableRooms?.collectAsState() ?: remember { mutableStateOf(emptyList<RoomInfo>()) }
    
    var isScanning by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // IP thủ công và dialog
    var manualIpAddress by remember { mutableStateOf("") }
    var showIpInputDialog by remember { mutableStateOf(false) }
    
    // Lấy địa chỉ IP hiện tại
    val localIpAddress = remember { repository?.getCurrentIpAddress() ?: "IP not available" }
    
    // Variable for create room dialog
    var newRoomName by remember { mutableStateOf("") }
    var maxPlayers by remember { mutableStateOf("2") }
    
    // Bắt đầu quét khi màn hình được hiển thị
    LaunchedEffect(Unit) {
        repository?.discoverHosts()
        isScanning = true
        
        // Giả lập thời gian quét
        delay(5000)
        isScanning = false
    }
    
    // Effect to refresh rooms periodically
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected || 
            connectionState is ConnectionState.Hosting) {
            // Request rooms list immediately
            repository?.refreshRooms()
            
            // Then start periodic refresh
            while (true) {
                delay(10000) // Update every 10 seconds
                repository?.refreshRooms()
            }
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
                title = "Available Rooms",
                onBackClick = onBack
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Create room button
            GradientButton(
                text = "Create New Room",
                onClick = { showCreateRoomDialog = true },
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
                    repository?.refreshRooms()
                    coroutineScope.launch {
                        delay(3000)
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
            } else if (availableRooms.isEmpty()) {
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
            if (availableRooms.isNotEmpty()) {
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
                    items(availableRooms) { room ->
                        RoomItem(
                            room = room,
                            onJoin = { 
                                repository?.joinRoom(room.id)
                                onJoinRoom(room.hostName)
                            }
                        )
                    }
                }
            } else if (discoveredHosts.isNotEmpty()) {
                Text(
                    text = "Available Servers",
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
                        ServerItem(
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
        
        // Create room dialog
        if (showCreateRoomDialog) {
            Dialog(onDismissRequest = { showCreateRoomDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = AppColors.Surface,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Create New Room",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Room name field
                        OutlinedTextField(
                            value = newRoomName,
                            onValueChange = { newRoomName = it },
                            label = { Text("Room Name") },
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
                        
                        // Max players field
                        OutlinedTextField(
                            value = maxPlayers,
                            onValueChange = { 
                                // Validate input to be a number between 2 and 10
                                val parsedValue = it.toIntOrNull()
                                if (it.isEmpty() || (parsedValue != null && parsedValue in 2..10)) {
                                    maxPlayers = it
                                }
                            },
                            label = { Text("Max Players (2-10)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        
                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { showCreateRoomDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.DarkGray,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Button(
                                onClick = {
                                    if (newRoomName.isNotEmpty() && maxPlayers.isNotEmpty()) {
                                        val maxPlayersNum = maxPlayers.toIntOrNull() ?: 2
                                        repository?.createRoom(newRoomName, maxPlayersNum)
                                        showCreateRoomDialog = false
                                        
                                        // Refresh the rooms list
                                        coroutineScope.launch {
                                            delay(500)
                                            repository?.refreshRooms()
                                        }
                                    }
                                },
                                enabled = newRoomName.isNotEmpty() && maxPlayers.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = AppColors.AccentYellow,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Create")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerItem(
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
                        text = if (isOwnRoom) "Your Game Server" else "Game Server",
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
                        text = "Connect",
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

@Composable
private fun RoomItem(
    room: RoomInfo,
    onJoin: () -> Unit
) {
    // Check if room is full or active
    val isFull = remember { room.playerCount >= room.maxPlayers }
    val isActive = remember { room.status == "PLAYING" }
    
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
            // Room info
            Column {
                Text(
                    text = room.name,
                    color = AppColors.OnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AppColors.OnSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Host: ${room.hostName}",
                        color = AppColors.OnSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = AppColors.OnSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.playerCount}/${room.maxPlayers} players",
                        color = if (isFull) AppColors.Error else AppColors.OnSurface.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Status/Join button
            if (isFull) {
                Box(
                    modifier = Modifier
                        .background(
                            color = AppColors.Error.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Full",
                        color = AppColors.Error,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            } else if (isActive) {
                Box(
                    modifier = Modifier
                        .background(
                            color = AppColors.Info.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "In Game",
                        color = AppColors.Info,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            } else {
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
}

@Preview(showBackground = true)
@Composable
fun AvailableRoomsScreenPreview() {
    AvailableRoomsScreen()
} 