package com.example.tictoe.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictoe.LAN.RoomInfo
import com.example.tictoe.ui.components.*
import com.example.tictoe.ui.theme.AppColors
import com.example.tictoe.ui.theme.GlowingBackgroundEffect
import com.example.tictoe.viewmodel.LANViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Màn hình hiển thị danh sách phòng có sẵn để tham gia */
@Composable
fun AvailableRoomsScreen(
        onBack: () -> Unit = {},
        onMatchFound: (String) -> Unit = {},
        viewModel: LANViewModel
) {
    val rooms by viewModel.availableRooms.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isDiscovering by viewModel.isDiscovering.collectAsState()
    val isJoining by viewModel.isJoining.collectAsState()
    val opponentName by viewModel.opponentName.collectAsState()
    val chosenRoom by viewModel.myRoom.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var refreshClicked by remember { mutableStateOf(false) }

    // Auto-refresh every 10 seconds
    LaunchedEffect(refreshClicked, isJoining) {
        if (isJoining) return@LaunchedEffect // Immediately exit if joining
        viewModel.startDiscovery(context)
        refreshClicked = false // Reset after starting discovery
        while (true) {
            delay(10_000)
            viewModel.stopDiscovery()
            delay(500) // Allow unregister to settle
            viewModel.startDiscovery(context)
        }
    }

    // If connected, trigger the callback (navigate to game)
    LaunchedEffect(isConnected) {
        if (isConnected && (opponentName?.isNotEmpty() == true)) {
            onMatchFound(opponentName ?: "") // Navigate to game screen
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppColors.MainGradient)) {
        // Add glowing background effect
        GlowingBackgroundEffect(modifier = Modifier.fillMaxSize())

        Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header with back button
            AppHeader(title = "Available Rooms", onBackClick = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            // Refresh button
            ModernOutlinedButton(
                    text = "Refresh Room List",
                    onClick = {
                        viewModel.stopDiscovery()
                        coroutineScope.launch {
                            delay(500)
                            viewModel.startDiscovery(context)
                        }
                        refreshClicked = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Refresh
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status text
            if (isDiscovering) {
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
                Spacer(modifier = Modifier.height(8.dp))
            } else if (rooms.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Room list
            if (rooms.isNotEmpty()) {
                LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rooms) { room ->
                        RoomItem(
                                room = room,
                                onJoin = {
                                    viewModel.joinRoom(room)
                                    viewModel.setOpponentName(room.hostName)
                                }
                        )
                    }
                }
            }
        }

        // Loading overlay
        if (isJoining) {
            Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
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
                                text = "Joining ${chosenRoom?.roomName} room...",
                                color = Color.White,
                                fontSize = 16.sp
                        )
                        Button(onClick = { viewModel.disconnect() }) { Text("Cancel") }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomItem(room: RoomInfo, onJoin: () -> Unit) {
    val isActive = remember { room.status.equals("playing", ignoreCase = true) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Room info
            Column {
                // Display room name
                Text(
                        text = room.roomName,
                        color = AppColors.OnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Display host name
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
            }

            // Status/Join button
            if (isActive) {
                Box(
                        modifier =
                                Modifier.background(
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
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = AppColors.AccentYellow,
                                        contentColor = Color.Black
                                ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                ) { Text(text = "Join", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AvailableRoomsScreenPreview() {
    val viewModel = remember { LANViewModel() }
    AvailableRoomsScreen(viewModel = viewModel)
}
