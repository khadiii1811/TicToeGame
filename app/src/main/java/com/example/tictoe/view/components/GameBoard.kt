package com.example.tictoe.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tictoe.viewmodel.WinningLine

@Composable
fun GameBoard(
    gameBoard: Array<Array<String>>,
    onCellClick: (row: Int, col: Int) -> Unit,
    winningLine: WinningLine?,
    boardScale: Float = 1f,
    gameWinner: String = ""
) {
    // Game Board
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            // Không sử dụng weight ở đây vì nó chỉ hợp lệ trong Row, Column hoặc Box với BoxScope
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0xFF6A4FC6)
            )
            .scale(boardScale),
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
                                    // Only allow moves when cell is empty and game is not over
                                    if (gameBoard[row][col].isEmpty() && gameWinner.isEmpty()) {
                                        onCellClick(row, col)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                isHighlighted = winningLine?.containsCell(row, col) == true
                            )
                        }
                    }
                }
            }
            
            // Draw winning line
            winningLine?.let { line ->
                WinningLineOverlay(line = line)
            }
        }
    }
} 