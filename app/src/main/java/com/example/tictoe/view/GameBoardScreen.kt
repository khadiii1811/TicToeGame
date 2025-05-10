package com.example.tictoe.view

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictoe.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Define ParticleData class at file level instead of inside composable function
private data class ParticleData(
    val xPos: Float, 
    val yPos: Float, 
    val particleSize: Float, 
    val alphaValue: Float, 
    val xSpeed: Float, 
    val ySpeed: Float
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameBoardScreen(
    onBackClick: () -> Unit = {},
    onPlayAgain: () -> Unit = {}
) {
    // State for the game
    var currentTurn by remember { mutableStateOf("X") } // X or O
    var gameWinner by remember { mutableStateOf("") } // X, O or "Draw"
    var gameBoard by remember { mutableStateOf(Array(3) { Array(3) { "" } }) }
    
    // Game stats (these would normally be persisted in a real app)
    var wins by remember { mutableIntStateOf(8) }
    var draws by remember { mutableIntStateOf(2) }
    var losses by remember { mutableIntStateOf(0) }
    var winRate by remember { mutableIntStateOf(80) } // Percentage
    
    // Animation states
    val coroutineScope = rememberCoroutineScope()
    val boardScale = remember { Animatable(0.8f) }
    val titleOffsetY = remember { Animatable(-100f) }
    
    // Floating particles animation
    val particles = remember { 
        List(16) { 
            mutableStateOf(
                ParticleData(
                    xPos = Random.nextFloat() * 400 - 20,
                    yPos = Random.nextFloat() * 800 - 20, 
                    particleSize = Random.nextFloat() * 5 + 3,
                    alphaValue = Random.nextFloat() * 0.15f + 0.05f,
                    xSpeed = Random.nextFloat() * 1f - 0.5f,
                    ySpeed = Random.nextFloat() * 0.4f - 0.2f
                )
            )
        }
    }
    
    LaunchedEffect(key1 = Unit) {
        boardScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = EaseOutBack)
        )
        titleOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(1000, easing = EaseOutBack)
        )
        
        // Animate particles
        coroutineScope.launch {
            while(true) {
                particles.forEach { state -> 
                    val particle = state.value
                    // Move particle and check if out of bounds
                    val newX = particle.xPos + particle.xSpeed
                    val newY = particle.yPos + particle.ySpeed
                    
                    // If particle goes out of bounds, reset it
                    state.value = if (newX < -50 || newX > 500 || newY < -50 || newY > 1000) {
                        ParticleData(
                            xPos = Random.nextFloat() * 400 - 20,
                            yPos = Random.nextFloat() * 800 - 20, 
                            particleSize = Random.nextFloat() * 5 + 3,
                            alphaValue = Random.nextFloat() * 0.15f + 0.05f,
                            xSpeed = Random.nextFloat() * 1f - 0.5f,
                            ySpeed = Random.nextFloat() * 0.4f - 0.2f
                        )
                    } else {
                        ParticleData(
                            xPos = newX,
                            yPos = newY,
                            particleSize = particle.particleSize,
                            alphaValue = particle.alphaValue,
                            xSpeed = particle.xSpeed,
                            ySpeed = particle.ySpeed
                        )
                    }
                }
                delay(50)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF19104B),
                        Color(0xFF25175B),
                        Color(0xFF2F1F65)
                    )
                )
            )
    ) {
        // Animated particles in background
        particles.forEach { state ->
            val particle = state.value
            Box(
                modifier = Modifier
                    .size(particle.particleSize.dp)
                    .alpha(particle.alphaValue)
                    .offset(x = particle.xPos.dp, y = particle.yPos.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD600).copy(alpha = 0.6f),
                                Color(0xFF4EE6FA).copy(alpha = 0.2f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // Circle decor top left
        Box(
            modifier = Modifier
                .size(200.dp)
                .alpha(0.12f)
                .offset(x = (-70).dp, y = (-70).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    ),
                    shape = CircleShape
                )
                .blur(20.dp)
                .align(Alignment.TopStart)
        )
        
        // Circle decor bottom right
        Box(
            modifier = Modifier
                .size(240.dp)
                .alpha(0.10f)
                .offset(x = 100.dp, y = 100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    ),
                    shape = CircleShape
                )
                .blur(20.dp)
                .align(Alignment.BottomEnd)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title with animation
            Box(
                modifier = Modifier
                    .offset(y = titleOffsetY.value.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD600),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .drawBehind {
                            drawTitleGlow(Color(0xFFFFD600).copy(alpha = 0.4f))
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Turn status
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(700)) + 
                        expandVertically(animationSpec = tween(700, easing = EaseOutBack)),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 8.dp,
                    backgroundColor = Color(0xFF3B256A)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 36.dp, vertical = 10.dp)
                    ) {
                        AnimatedContent(
                            targetState = when {
                                gameWinner.isEmpty() -> "Your Turn"
                                gameWinner == "Draw" -> "Game Draw"
                                else -> "Winner: $gameWinner"
                            },
                            transitionSpec = {
                                slideInVertically() + fadeIn() togetherWith slideOutVertically() + fadeOut()
                            }
                        ) { targetText ->
                            Text(
                                text = targetText,
                                fontSize = 18.sp,
                                color = Color(0xFF4EE6FA),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Players Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Player X
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlayerSymbolBox(
                        symbol = "X", 
                        isActive = currentTurn == "X" && gameWinner.isEmpty(),
                        symbolColor = Color(0xFFFFD600),
                        borderColor = Color(0xFFFFD600)
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "You",
                        color = Color(0xFFB0A9D1),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Player O
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlayerSymbolBox(
                        symbol = "O", 
                        isActive = currentTurn == "O" && gameWinner.isEmpty(),
                        symbolColor = Color(0xFF4EE6FA),
                        borderColor = Color(0xFF4EE6FA)
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Opponent",
                        color = Color(0xFFB0A9D1),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Game Board
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .weight(1f, fill = false)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color(0xFF6A4FC6)
                    )
                    .scale(boardScale.value),
                shape = RoundedCornerShape(32.dp),
                elevation = 0.dp,
                backgroundColor = Color(0xFF3F2C77)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4E3A8C),
                                    Color(0xFF3F2C77)
                                ),
                                center = Offset(0.5f, 0.5f),
                                radius = 1000f
                            )
                        )
                ) {
                    // Game grid
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (row in 0..2) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0..2) {
                                    GameCell(
                                        value = gameBoard[row][col],
                                        onClick = {
                                            if (gameBoard[row][col].isEmpty() && gameWinner.isEmpty()) {
                                                val newBoard = gameBoard.map { it.clone() }.toTypedArray()
                                                newBoard[row][col] = currentTurn
                                                gameBoard = newBoard
                                                
                                                // Check for winner
                                                val winner = checkWinner(newBoard)
                                                if (winner.isNotEmpty()) {
                                                    gameWinner = winner
                                                    // Update stats (in a real app, this would be persisted)
                                                    when (winner) {
                                                        "X" -> wins++
                                                        "O" -> losses++
                                                        "Draw" -> draws++
                                                    }
                                                    val totalGames = wins + losses + draws
                                                    if (totalGames > 0) {
                                                        winRate = (wins * 100) / totalGames
                                                    }
                                                } else {
                                                    // Switch turn
                                                    currentTurn = if (currentTurn == "X") "O" else "X"
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Back Button
                ModernButton(
                    text = "Back",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    backgroundColor = Color(0xFF4B357A),
                    contentColor = Color.White,
                    onClick = onBackClick
                )
                
                // Play Again Button
                ModernButton(
                    text = "Play Again",
                    icon = Icons.Filled.Refresh,
                    backgroundColor = Color(0xFFFFD600),
                    contentColor = Color(0xFF2D1863),
                    onClick = {
                        coroutineScope.launch {
                            // Add bounce animation on reset
                            boardScale.animateTo(
                                targetValue = 0.9f,
                                animationSpec = tween(300, easing = EaseInOutQuad)
                            )
                            boardScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                        
                        // Reset game state
                        gameBoard = Array(3) { Array(3) { "" } }
                        currentTurn = "X"
                        gameWinner = ""
                        onPlayAgain()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Game Stats Section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000)) + 
                        expandVertically(animationSpec = tween(800, easing = EaseOutBack))
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = 12.dp,
                    backgroundColor = Color(0xFF3B256A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BarChart, 
                                contentDescription = null,
                                tint = Color(0xFFB0A9D1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Game Stats",
                                color = Color(0xFFB0A9D1),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Wins
                            StatCard(
                                title = "Wins",
                                value = wins.toString(),
                                valueColor = Color(0xFFFFD600),
                                icon = Icons.Filled.EmojiEvents
                            )
                            
                            // Draws
                            StatCard(
                                title = "Draws",
                                value = draws.toString(),
                                valueColor = Color.White,
                                icon = Icons.Default.Balance
                            )
                            
                            // Losses
                            StatCard(
                                title = "Losses",
                                value = losses.toString(),
                                valueColor = Color(0xFF4EE6FA),
                                icon = Icons.Filled.Close
                            )
                            
                            // Win Rate
                            StatCard(
                                title = "Win Rate",
                                value = "$winRate%",
                                valueColor = Color(0xFFFFD600),
                                icon = Icons.AutoMirrored.Filled.ShowChart
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF4E3A8C))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = valueColor,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            color = valueColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = title,
            color = Color(0xFFB0A9D1),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ModernButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val buttonScale = remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = backgroundColor.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = contentColor.copy(alpha = 0.1f)),
                onClick = {
                    scope.launch {
                        buttonScale.floatValue = 0.95f
                        delay(100)
                        buttonScale.floatValue = 1f
                        onClick()
                    }
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .scale(buttonScale.floatValue),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlayerSymbolBox(
    symbol: String,
    isActive: Boolean,
    symbolColor: Color,
    borderColor: Color
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale = if (isActive) {
        pulseAnim.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        ).value
    } else 1f
    
    val shadowAlpha = if (isActive) {
        pulseAnim.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shadowAlpha"
        ).value
    } else 0.2f
    
    Box(
        modifier = Modifier
            .size(68.dp)
            .shadow(
                elevation = if (isActive) 12.dp else 4.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = symbolColor.copy(alpha = shadowAlpha)
            )
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4B357A),
                        Color(0xFF3F2C77)
                    )
                )
            )
            .border(
                width = if (isActive) 2.5.dp else 0.dp,
                color = if (isActive) borderColor else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = symbolColor
        )
    }
}

@Composable
fun GameCell(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Cell appears
    val animatedAlpha = remember { Animatable(0f) }
    val animatedScale = remember { Animatable(0.8f) }
    
    LaunchedEffect(key1 = value) {
        if (value.isNotEmpty()) {
            launch {
                animatedAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(300)
                )
            }
            launch {
                animatedScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        } else {
            animatedAlpha.snapTo(0f)
            animatedScale.snapTo(0.8f)
        }
    }
    
    Box(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0xFF6A4FC6).copy(alpha = 0.7f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF563FAF),
                        Color(0xFF4E3A8C)
                    )
                )
            )
            .border(
                width = 2.dp, 
                color = Color(0xFF6A4FC6),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                enabled = value.isEmpty(),
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.3f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .alpha(animatedAlpha.value)
                    .scale(animatedScale.value)
            ) {
                Text(
                    text = value,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (value == "X") Color(0xFFFFD600) else Color(0xFF4EE6FA)
                )
            }
        }
    }
}

fun DrawScope.drawTitleGlow(color: Color) {
    drawCircle(
        color = color,
        radius = 100f,
        center = Offset(size.width / 2, size.height / 2),
        style = Stroke(width = 50f)
    )
}

fun checkWinner(board: Array<Array<String>>): String {
    // Check rows
    for (i in 0..2) {
        if (board[i][0].isNotEmpty() && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
            return board[i][0]
        }
    }
    
    // Check columns
    for (i in 0..2) {
        if (board[0][i].isNotEmpty() && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
            return board[0][i]
        }
    }
    
    // Check diagonals
    if (board[0][0].isNotEmpty() && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
        return board[0][0]
    }
    
    if (board[0][2].isNotEmpty() && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
        return board[0][2]
    }
    
    // Check for draw
    for (i in 0..2) {
        for (j in 0..2) {
            if (board[i][j].isEmpty()) {
                return "" // Game still ongoing
            }
        }
    }
    
    return "Draw" // All cells filled and no winner
}

@Preview(showBackground = true)
@Composable
fun GameBoardScreenPreview() {
    GameBoardScreen()
} 