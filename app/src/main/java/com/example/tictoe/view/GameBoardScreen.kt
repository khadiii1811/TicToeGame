package com.example.tictoe.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictoe.ui.components.*
import com.example.tictoe.ui.theme.AppColors
import com.example.tictoe.ui.theme.GlowingBackgroundEffect
import com.example.tictoe.ui.theme.PulseEffect
import com.example.tictoe.view.components.*
import com.example.tictoe.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameBoardScreen(
        onBackClick: () -> Unit = {},
        onPlayAgain: () -> Unit = {},
        isVsBot: Boolean = false,
        gameViewModel: GameViewModel,
        lanViewModel: LANViewModel
) {
        // Collect state from GameViewModel with safe default values
        val currentTurn by gameViewModel.currentTurn.collectAsState()
        val gameWinner by gameViewModel.gameWinner.collectAsState()
        val gameBoard by gameViewModel.gameBoard.collectAsState()
        val winningLine by gameViewModel.winningLine.collectAsState()
        val showGameEndDialog by gameViewModel.showGameEndDialog.collectAsState()
        val isBotThinking by gameViewModel.isBotThinking.collectAsState()
        val wins by gameViewModel.wins.collectAsState()
        val draws by gameViewModel.draws.collectAsState()
        val losses by gameViewModel.losses.collectAsState()
        val winRate by gameViewModel.winRate.collectAsState()

        // Convert winningLine to List<Pair<Int, Int>> if it's not already
        val winningCells =
                remember(winningLine) {
                        val line = winningLine
                        if (line != null) {
                                val cells = mutableListOf<Pair<Int, Int>>()
                                for (i in 0..2) {
                                        for (j in 0..2) {
                                                if (line.containsCell(i, j)) {
                                                        cells.add(Pair(i, j))
                                                }
                                        }
                                }
                                cells
                        } else {
                                emptyList()
                        }
                }

        // Animation states
        val coroutineScope = rememberCoroutineScope()
        val boardScale = remember { Animatable(0.8f) }
        val titleOffsetY = remember { Animatable(-100f) }
        val dialogScale = remember { Animatable(0.8f) }

        // Floating particles animation
        val particles = rememberParticles(16)

        // Check for game end - show dialog after a short delay
        LaunchedEffect(gameWinner) {
                if (gameWinner.isEmpty()) return@LaunchedEffect

                delay(1200) // Delay showing the dialog to let player see the winning line

                // Trigger dialog scale animation
                dialogScale.snapTo(0.8f)
                dialogScale.animateTo(
                        targetValue = 1f,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                )
                )
        }

        // Wire up LAN ViewModel if provided
        LaunchedEffect(Unit) {
                lanViewModel.setOnMoveReceivedCallback { row, col ->
                        gameViewModel.makeMove(row, col, isVsBot = false)
                }
        }

        // Initial animations for board and title
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
                        while (true) {
                                particles.forEach { state ->
                                        state.value = updateParticle(state.value)
                                }
                                delay(50)
                        }
                }
        }

        Box(modifier = Modifier.fillMaxSize().background(brush = AppColors.MainGradient)) {
                // Glowing background effect
                GlowingBackgroundEffect(modifier = Modifier.fillMaxSize())

                // Animated particles in background
                particles.forEach { state -> ParticleEffect(particle = state.value) }

                Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Header with Back button
                        AppHeader(
                                title =
                                        if (lanViewModel.isConnected.value) "LAN Match"
                                        else (if (isVsBot) "Play vs Bot" else "Play vs Player"),
                                onBackClick = onBackClick
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Players' stats row
                        if (lanViewModel.isConnected.value == true) {
                                GlassCard(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                                // Current player
                                                Column {
                                                        Text(
                                                                text =
                                                                        "Player: ${lanViewModel.playerName}",
                                                                color =
                                                                        AppColors.OnSurface.copy(
                                                                                alpha = 0.7f
                                                                        ),
                                                                fontSize = 14.sp
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                                text =
                                                                        "Symbol: ${lanViewModel.symbol}",
                                                                color = AppColors.OnSurface,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }

                                                // COpponent player
                                                Column {
                                                        Text(
                                                                text =
                                                                        "Opponent: ${lanViewModel.opponentName}",
                                                                color =
                                                                        AppColors.OnSurface.copy(
                                                                                alpha = 0.7f
                                                                        ),
                                                                fontSize = 14.sp
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        val opponentSymbol =
                                                                if (lanViewModel.symbol == "X") "O"
                                                                else "X"
                                                        Text(
                                                                text = "Symbol: ${opponentSymbol}",
                                                                color = AppColors.OnSurface,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }
                                }
                        }

                        // Current player indicator
                        GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                        // Current player
                                        Column {
                                                Text(
                                                        text = "Current Turn",
                                                        color =
                                                                AppColors.OnSurface.copy(
                                                                        alpha = 0.7f
                                                                ),
                                                        fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                        text = currentTurn,
                                                        color = AppColors.OnSurface,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        }

                                        // Bot thinking indicator
                                        if (isVsBot && isBotThinking) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        PulseEffect(
                                                                pulseFraction = 1.2f,
                                                                durationMillis = 800
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default
                                                                                        .SmartToy,
                                                                        contentDescription =
                                                                                "Bot Thinking",
                                                                        tint =
                                                                                AppColors
                                                                                        .AccentYellow,
                                                                        modifier =
                                                                                Modifier.size(24.dp)
                                                                )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text = "Thinking...",
                                                                color = AppColors.AccentYellow,
                                                                fontSize = 16.sp
                                                        )
                                                }
                                        }

                                        if (lanViewModel.isConnected.value &&
                                                        !lanViewModel.isPlayerTurn
                                        ) {
                                                Text(
                                                        text = "Opponent Thinking...",
                                                        color = AppColors.AccentYellow,
                                                        fontSize = 16.sp
                                                )
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Game board
                        Box(modifier = Modifier.aspectRatio(1f).scale(boardScale.value)) {
                                GameBoard(
                                        board = gameBoard,
                                        winningLine = winningCells,
                                        onCellClick = { row, col ->
                                                if (gameWinner.isEmpty() && !isBotThinking) {
                                                        if (lanViewModel.isConnected.value) {
                                                                gameViewModel.makeMove(
                                                                        row,
                                                                        col,
                                                                        isVsBot
                                                                )
                                                        } else {
                                                                lanViewModel.onLocalMove(row, col)
                                                        }
                                                }
                                        }
                                )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Stats row
                        GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                        StatItem(
                                                value = wins.toString(),
                                                label = "Wins",
                                                icon = Icons.Default.EmojiEvents,
                                                color = AppColors.Success
                                        )

                                        StatItem(
                                                value = draws.toString(),
                                                label = "Draws",
                                                icon = Icons.Default.Balance,
                                                color = AppColors.Warning
                                        )

                                        StatItem(
                                                value = losses.toString(),
                                                label = "Losses",
                                                icon = Icons.Default.Close,
                                                color = AppColors.Error
                                        )

                                        StatItem(
                                                value = "${(winRate * 100).toInt()}%",
                                                label = "Win Rate",
                                                icon = Icons.AutoMirrored.Filled.ShowChart,
                                                color = AppColors.Info
                                        )
                                }
                        }
                }

                // Game end dialog
                EndGameDialog(
                        showGameEndDialog,
                        gameWinner,
                        resetGame = {
                                gameViewModel.resetGame()
                                onPlayAgain()
                        },
                        dialogScale
                )
        }
}

@Composable
fun EndGameDialog(
        showGameEndDialog: Boolean,
        gameWinner: String,
        resetGame: () -> Unit,
        dialogScale: Animatable<Float, AnimationVector1D>
) {
        AnimatedVisibility(
                visible = showGameEndDialog,
                enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .clickable(enabled = false, onClick = {}),
                        contentAlignment = Alignment.Center
                ) {
                        GlassCard(modifier = Modifier.padding(32.dp).scale(dialogScale.value)) {
                                Column(
                                        modifier = Modifier.padding(24.dp).width(IntrinsicSize.Min),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                text =
                                                        when {
                                                                gameWinner == "X" ||
                                                                        gameWinner == "O" ->
                                                                        "Winner!"
                                                                gameWinner == "Draw" ->
                                                                        "It\'s a Draw!"
                                                                else -> ""
                                                        },
                                                color =
                                                        when {
                                                                gameWinner == "X" ||
                                                                        gameWinner == "O" ->
                                                                        AppColors.AccentYellow
                                                                else -> AppColors.OnSurface
                                                        },
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (gameWinner == "X" || gameWinner == "O") {
                                                Text(
                                                        text = gameWinner,
                                                        color =
                                                                if (gameWinner == "X")
                                                                        AppColors.AccentPink
                                                                else AppColors.AccentPurple,
                                                        fontSize = 64.sp,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        } else {
                                                Icon(
                                                        imageVector = Icons.Default.Balance,
                                                        contentDescription = null,
                                                        tint = AppColors.Warning,
                                                        modifier = Modifier.size(64.dp)
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        GradientButton(
                                                text = "Play Again",
                                                onClick = { resetGame() },
                                                modifier = Modifier.width(200.dp)
                                        )
                                }
                        }
                }
        }
}

// Helper function to create and remember particle states
@Composable
private fun rememberParticles(count: Int): List<MutableState<ParticleData>> {
        return remember {
                List(count) {
                        mutableStateOf(
                                ParticleData(
                                        x = (Math.random() * 1000).toFloat(),
                                        y = (Math.random() * 2000).toFloat(),
                                        radius = (5 + Math.random() * 15).toFloat(),
                                        alpha = (0.1f + Math.random() * 0.2f).toFloat(),
                                        speed = (0.5 + Math.random() * 2).toFloat()
                                )
                        )
                }
        }
}

// Data class for particle animation
private data class ParticleData(
        val x: Float,
        val y: Float,
        val radius: Float,
        val alpha: Float,
        val speed: Float
)

// Update particle position
private fun updateParticle(particle: ParticleData): ParticleData {
        val newY =
                if (particle.y > 0) {
                        particle.y - particle.speed
                } else {
                        2000f
                }

        return particle.copy(y = newY)
}

// Particle effect composable
@Composable
private fun ParticleEffect(particle: ParticleData) {
        Box(
                modifier =
                        Modifier.offset(x = particle.x.dp, y = particle.y.dp)
                                .size(particle.radius.dp)
                                .alpha(particle.alpha)
                                .background(
                                        color = AppColors.AccentYellow.copy(alpha = 0.6f),
                                        shape = CircleShape
                                )
        )
}

// Game board composable
@Composable
private fun GameBoard(
        board: Array<Array<String>>,
        winningLine: List<Pair<Int, Int>>,
        onCellClick: (Int, Int) -> Unit
) {
        GlassCard(modifier = Modifier.aspectRatio(1f).padding(8.dp)) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        for (i in 0..2) {
                                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        for (j in 0..2) {
                                                val isWinningCell = winningLine.contains(Pair(i, j))
                                                GameCell(
                                                        value = board[i][j],
                                                        isWinningCell = isWinningCell,
                                                        onClick = { onCellClick(i, j) },
                                                        modifier =
                                                                Modifier.weight(1f)
                                                                        .fillMaxHeight()
                                                                        .padding(6.dp)
                                                )
                                        }
                                }
                        }
                }
        }
}

// Game cell composable
@Composable
private fun GameCell(
        value: String,
        isWinningCell: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
        val cellBackground =
                if (isWinningCell) {
                        Brush.radialGradient(
                                colors =
                                        listOf(
                                                AppColors.AccentYellow.copy(alpha = 0.3f),
                                                AppColors.AccentYellow.copy(alpha = 0.1f)
                                        )
                        )
                } else {
                        Brush.radialGradient(
                                colors =
                                        listOf(
                                                AppColors.SurfaceLight.copy(alpha = 0.7f),
                                                AppColors.Surface.copy(alpha = 0.4f)
                                        )
                        )
                }

        val cellBorder =
                if (isWinningCell) {
                        BorderStroke(
                                width = 2.dp,
                                brush =
                                        Brush.linearGradient(
                                                colors =
                                                        listOf(
                                                                AppColors.AccentYellow,
                                                                AppColors.AccentPink
                                                        )
                                        )
                        )
                } else {
                        BorderStroke(width = 1.dp, color = AppColors.CardBorder)
                }

        Card(
                modifier =
                        modifier.shadow(
                                        elevation = if (isWinningCell) 8.dp else 4.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        spotColor =
                                                if (isWinningCell)
                                                        AppColors.AccentYellow.copy(alpha = 0.5f)
                                                else AppColors.AccentPurple.copy(alpha = 0.3f)
                                )
                                .clickable(enabled = value.isEmpty()) { onClick() },
                shape = RoundedCornerShape(16.dp),
                border = cellBorder,
                backgroundColor = Color.Transparent,
                elevation = 0.dp
        ) {
                Box(
                        modifier = Modifier.fillMaxSize().background(cellBackground),
                        contentAlignment = Alignment.Center
                ) {
                        when (value) {
                                "X" -> {
                                        Text(
                                                text = "X",
                                                color =
                                                        if (isWinningCell) AppColors.AccentYellow
                                                        else AppColors.AccentPink,
                                                fontSize = 40.sp,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                                "O" -> {
                                        Text(
                                                text = "O",
                                                color =
                                                        if (isWinningCell) AppColors.AccentYellow
                                                        else AppColors.AccentPurple,
                                                fontSize = 40.sp,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        }
                }
        }
}

// Stat item composable
@Composable
private fun StatItem(value: String, label: String, icon: ImageVector, color: Color) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                        modifier =
                                Modifier.size(40.dp)
                                        .shadow(
                                                elevation = 4.dp,
                                                shape = CircleShape,
                                                spotColor = color.copy(alpha = 0.3f)
                                        )
                                        .background(
                                                color = AppColors.SurfaceLight,
                                                shape = CircleShape
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                        )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                        text = value,
                        color = AppColors.OnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                )

                Text(text = label, color = AppColors.OnSurface.copy(alpha = 0.7f), fontSize = 12.sp)
        }
}

@Preview(showBackground = true)
@Composable
fun GameBoardScreenPreview() {
        // Create a preview version of the ViewModel
        val previewViewModel = remember { GameViewModel() }
        val lanViewModel = remember { LANViewModel() }

        GameBoardScreen(
                gameViewModel = previewViewModel,
                isVsBot = true,
                lanViewModel = lanViewModel
        )
}
