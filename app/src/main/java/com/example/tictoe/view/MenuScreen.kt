package com.example.tictoe.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictoe.R
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MenuScreen(
    onOnlineClick: () -> Unit = {},
    onVsBotClick: () -> Unit = {},
    onVsPlayerClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E124D),
                        Color(0xFF2D1863),
                        Color(0xFF3A256A)
                    )
                )
            )
    ) {
        // Decor top left
        Box(
            modifier = Modifier
                .size(180.dp)
                .alpha(0.12f)
                .offset(x = (-60).dp, y = (-60).dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                .align(Alignment.TopStart)
        ) {
            // Thay Image bằng Box có background màu sắc
        }
        // Decor bottom right
        Box(
            modifier = Modifier
                .size(220.dp)
                .alpha(0.10f)
                .offset(x = 80.dp, y = 80.dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                .align(Alignment.BottomEnd)
        ) {
            // Thay Image bằng Box có background màu sắc
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD600),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color(0xFF000000).copy(alpha = 0.25f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(36.dp),
                elevation = 18.dp,
                backgroundColor = Color(0xFF3B256A),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // User Info
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = 6.dp,
                        backgroundColor = Color(0xFF332058),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4C3780))
                                    .padding(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Welcome,",
                                    color = Color(0xFFB0A9D1),
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Player1",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Menu Buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuButton(
                            text = "Online",
                            icon = Icons.Default.Public,
                            background = Color(0xFFFFD600),
                            textColor = Color(0xFF2D1863),
                            onClick = onOnlineClick
                        )
                        MenuButton(
                            text = "Vs Bot",
                            icon = Icons.Default.SmartToy,
                            background = Color(0xFF5B4B8A),
                            textColor = Color.White,
                            onClick = onVsBotClick
                        )
                        MenuButton(
                            text = "Vs Player",
                            icon = Icons.Default.Person,
                            background = Color(0xFF5B4B8A),
                            textColor = Color.White,
                            onClick = onVsPlayerClick
                        )
                        MenuButton(
                            text = "LeaderBoard",
                            icon = Icons.Default.EmojiEvents,
                            background = Color(0xFF5B4B8A),
                            textColor = Color.White,
                            onClick = onLeaderboardClick
                        )
                        MenuButton(
                            text = stringResource(R.string.settings),
                            icon = Icons.Default.Settings,
                            background = Color(0xFF5B4B8A),
                            textColor = Color.White,
                            onClick = onSettingsClick
                        )
                    }
                    
                    // Hint text with subtle animation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.SwipeLeft,
                                contentDescription = null,
                                tint = Color(0xFFB0A9D1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Swipe left to see game board",
                                color = Color(0xFFB0A9D1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    background: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        elevation = 4.dp,
        backgroundColor = background
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = background),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Thêm box rỗng bên phải để cân bằng với icon bên trái
                Spacer(modifier = Modifier.width(60.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    MenuScreen()
}