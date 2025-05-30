package com.example.tictoe.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.example.tictoe.ui.components.*
import com.example.tictoe.ui.theme.AppColors
import com.example.tictoe.ui.theme.GlowingBackgroundEffect
import com.example.tictoe.viewmodel.LANViewModel

/** Screen for creating a new game room */
@Composable
fun CreateRoomScreen(
        onBack: () -> Unit = {},
        onMatchFound: (String) -> Unit = {},
        lanViewModel: LANViewModel
) {
    var roomName by remember { mutableStateOf("") }
    val isHosting by lanViewModel.isHosting.collectAsState()
    val isConnected by lanViewModel.isConnected.collectAsState()
    val opponentName by lanViewModel.opponentName.collectAsState()
    val localContext = LocalContext.current

    // Handle match found
    LaunchedEffect(isConnected) {
        if (!isConnected)
            return@LaunchedEffect
        if ((opponentName?.isNotEmpty() == false))
            return@LaunchedEffect
        onMatchFound(opponentName ?: "") // Navigate to game screen
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
            AppHeader(title = "Create New Room", onBackClick = onBack)

            Spacer(modifier = Modifier.height(40.dp))

            // Room information form
            Card(
                    backgroundColor = AppColors.Surface,
                    shape = RoundedCornerShape(16.dp),
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
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
                            colors =
                                    TextFieldDefaults.outlinedTextFieldColors(
                                            textColor = Color.White,
                                            focusedBorderColor = AppColors.AccentYellow,
                                            focusedLabelColor = AppColors.AccentYellow,
                                            cursorColor = AppColors.AccentYellow,
                                            unfocusedBorderColor = Color.Gray,
                                            unfocusedLabelColor = Color.Gray
                                    ),
                            modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Create Room Button
            GradientButton(
                    text = "Create Room",
                    onClick = {
                        if (roomName.isNotBlank() && !isHosting) {
                            lanViewModel.hostGame(localContext, roomName)
                        }
                    },
                    enabled = roomName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Check
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Loading overlay
        if (isHosting) {
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
                                text = "The room is created. Waiting for opponent...",
                                color = Color.White,
                                fontSize = 16.sp
                        )
                        Button(onClick = { lanViewModel.stopHosting() }) { Text("Cancel") }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CreateRoomScreenPreview() {
    val lanViewModel = remember { LANViewModel() }
    CreateRoomScreen(lanViewModel = lanViewModel)
}
