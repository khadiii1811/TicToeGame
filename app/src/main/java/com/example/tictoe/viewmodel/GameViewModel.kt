package com.example.tictoe.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictoe.model.AIEngine
import com.example.tictoe.model.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class GameViewModel : ViewModel() {
    // Game board state
    private val _gameBoard = MutableStateFlow(Array(3) { Array(3) { "" } })
    val gameBoard: StateFlow<Array<Array<String>>> = _gameBoard.asStateFlow()

    // Current turn - X or O
    private val _currentTurn = MutableStateFlow("X")
    val currentTurn: StateFlow<String> = _currentTurn.asStateFlow()

    // Winner - X, O, Draw, or empty string for game in progress
    private val _gameWinner = MutableStateFlow("")
    val gameWinner: StateFlow<String> = _gameWinner.asStateFlow()

    // Winning line for highlighting
    private val _winningLine = MutableStateFlow<WinningLine?>(null)
    val winningLine: StateFlow<WinningLine?> = _winningLine.asStateFlow()

    // Bot thinking state
    private val _isBotThinking = MutableStateFlow(false)
    val isBotThinking: StateFlow<Boolean> = _isBotThinking.asStateFlow()

    // Game stats
    private val _wins = MutableStateFlow(0)
    val wins: StateFlow<Int> = _wins.asStateFlow()

    private val _draws = MutableStateFlow(0)
    val draws: StateFlow<Int> = _draws.asStateFlow()

    private val _losses = MutableStateFlow(0)
    val losses: StateFlow<Int> = _losses.asStateFlow()

    private val _winRate = MutableStateFlow(0)
    val winRate: StateFlow<Int> = _winRate.asStateFlow()

    // Dialog state
    private val _showGameEndDialog = MutableStateFlow(false)
    val showGameEndDialog: StateFlow<Boolean> = _showGameEndDialog.asStateFlow()
    
    // SoundManager
    private var soundManager: SoundManager? = null
    
    // AI difficulty level
    private var aiDifficulty: Int = 2 // Default to Normal
    
    // Gắn SoundManager vào ViewModel
    fun setSoundManager(manager: SoundManager) {
        soundManager = manager
    }
    
    // Set AI difficulty level
    fun setAiDifficulty(difficulty: Int) {
        if (difficulty in 1..3) {
            aiDifficulty = difficulty
            Log.d("TicToe", "AI difficulty set to: $aiDifficulty")
        }
    }

    // Reset the game state
    fun resetGame() {
        _gameBoard.value = Array(3) { Array(3) { "" } }
        _currentTurn.value = "X"
        _gameWinner.value = ""
        _winningLine.value = null
        _showGameEndDialog.value = false
        _isBotThinking.value = false
    }

    // Make a move at the specified cell
    fun makeMove(row: Int, col: Int, isVsBot: Boolean) {
        // Only allow moves when cell is empty and game is not over
        if (_gameBoard.value[row][col].isEmpty() && _gameWinner.value.isEmpty() &&
            (_currentTurn.value == "X" || (_currentTurn.value == "O" && !isVsBot))) {
            
            // Create a new copy of the board to update
            val newBoard = _gameBoard.value.map { it.clone() }.toTypedArray()
            newBoard[row][col] = _currentTurn.value
            _gameBoard.value = newBoard
            
            // Phát âm thanh khi đặt quân
            soundManager?.playClickSound()
            
            // Check for winner
            checkForWinner(newBoard)
            
            // If no winner, switch turn
            if (_gameWinner.value.isEmpty()) {
                _currentTurn.value = if (_currentTurn.value == "X") "O" else "X"
                
                // If it's bot's turn, make the AI move
                if (isVsBot && _currentTurn.value == "O") {
                    makeBotMove()
                }
            }
        }
    }

    fun makeMoveLAN(row: Int, col: Int, symbol: String) {
        // Only allow moves when cell is empty and game is not over
        if (_gameBoard.value[row][col].isEmpty() && _gameWinner.value.isEmpty()) {
            val newBoard = _gameBoard.value.map { it.clone() }.toTypedArray()
            newBoard[row][col] = symbol
            _gameBoard.value = newBoard
            soundManager?.playClickSound()
            checkForWinner(newBoard)
            if (_gameWinner.value.isEmpty()) {
                _currentTurn.value = if (symbol == "X") "O" else "X"
            }
        }
    }

    // Make a move for the AI bot
    private fun makeBotMove() {
        viewModelScope.launch {
            _isBotThinking.value = true
            // Add a small delay to make it look like the bot is "thinking"
            delay(800)
            
            try {
                // Convert gameBoard from Array<Array<String>> to Array<IntArray> for AIEngine
                val aiBoard = Array(3) { row ->
                    IntArray(3) { col ->
                        when (_gameBoard.value[row][col]) {
                            "X" -> 1 // PLAYER
                            "O" -> 2 // AI
                            else -> 0 // EMPTY
                        }
                    }
                }
                
                // Log the board for debugging
                Log.d("TicToe", "Board converted for AI: ${aiBoard.contentDeepToString()}")
                
                // Get AI move with difficulty level
                val bestMove = AIEngine.findBestMove(aiBoard, aiDifficulty)
                Log.d("TicToe", "AI returned move with difficulty $aiDifficulty: $bestMove")
                
                // Use fallback if AI didn't return a valid move
                var moveRow = -1
                var moveCol = -1
                
                if (bestMove != null) {
                    moveRow = bestMove.first
                    moveCol = bestMove.second
                    Log.d("TicToe", "Using AI's suggested move: row=$moveRow, col=$moveCol")
                } else {
                    // Fallback: find any available cell
                    for (r in 0..2) {
                        for (c in 0..2) {
                            if (_gameBoard.value[r][c].isEmpty()) {
                                moveRow = r
                                moveCol = c
                                Log.d("TicToe", "Using fallback move: row=$moveRow, col=$moveCol")
                                break
                            }
                        }
                        if (moveRow >= 0) break
                    }
                }
                
                // Make sure the move is valid
                if (moveRow in 0..2 && moveCol in 0..2 && _gameBoard.value[moveRow][moveCol].isEmpty()) {
                    // Update the board with AI move
                    val newBoard = _gameBoard.value.map { it.clone() }.toTypedArray()
                    newBoard[moveRow][moveCol] = "O"
                    _gameBoard.value = newBoard
                    
                    // Phát âm thanh khi bot đánh
                    soundManager?.playClickSound()
                    
                    // Check for winner
                    checkForWinner(newBoard)
                    
                    // If no winner, switch turn
                    if (_gameWinner.value.isEmpty()) {
                        _currentTurn.value = "X"
                    }
                } else {
                    // Invalid move, just switch turn
                    _currentTurn.value = "X"
                    Log.e("TicToe", "Invalid move attempt by AI: row=$moveRow, col=$moveCol")
                }
            } catch (e: Exception) {
                Log.e("TicToe", "Error during AI move: ${e.message}", e)
                // Switch turn back to player if there's an error
                _currentTurn.value = "X"
            } finally {
                _isBotThinking.value = false
            }
        }
    }

    // Check if there's a winner on the board
    private fun checkForWinner(board: Array<Array<String>>) {
        val winner = getWinner(board)
        if (winner.isNotEmpty() && winner != "Draw") {
            _gameWinner.value = winner
            // Find winning line
            _winningLine.value = findWinningLine(board, winner)
            
            // Update stats
            when (winner) {
                "X" -> {
                    _wins.value = _wins.value + 1
                    updateWinRate()
                    // Phát âm thanh chiến thắng
                    soundManager?.playWinSound()
                }
                "O" -> {
                    _losses.value = _losses.value + 1
                    updateWinRate()
                    // Phát âm thanh thua
                    soundManager?.playLoseSound()
                }
            }
            
            // Show game end dialog after a delay
            viewModelScope.launch {
                delay(1200)
                _showGameEndDialog.value = true
            }
        } else if (winner == "Draw") {
            _gameWinner.value = "Draw"
            _draws.value = _draws.value + 1
            updateWinRate()
            
            // Phát âm thanh hòa
            soundManager?.playDrawSound()
            
            // Show game end dialog after a delay
            viewModelScope.launch {
                delay(1200)
                _showGameEndDialog.value = true
            }
        }
    }

    // Update win rate percentage
    private fun updateWinRate() {
        val totalGames = _wins.value + _losses.value + _draws.value
        if (totalGames > 0) {
            _winRate.value = (_wins.value * 100) / totalGames
        }
    }

    // Determine the winner of the board
    private fun getWinner(board: Array<Array<String>>): String {
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
    
    // Find the winning line for visual highlighting
    private fun findWinningLine(board: Array<Array<String>>, winner: String): WinningLine? {
        // Check rows
        for (i in 0..2) {
            if (board[i][0] == winner && board[i][1] == winner && board[i][2] == winner) {
                return WinningLine(i, 0, i, 2, LineType.ROW)
            }
        }
        
        // Check columns
        for (i in 0..2) {
            if (board[0][i] == winner && board[1][i] == winner && board[2][i] == winner) {
                return WinningLine(0, i, 2, i, LineType.COLUMN)
            }
        }
        
        // Check diagonal \
        if (board[0][0] == winner && board[1][1] == winner && board[2][2] == winner) {
            return WinningLine(0, 0, 2, 2, LineType.DIAGONAL_DOWN)
        }
        
        // Check diagonal /
        if (board[0][2] == winner && board[1][1] == winner && board[2][0] == winner) {
            return WinningLine(0, 2, 2, 0, LineType.DIAGONAL_UP)
        }
        
        return null
    }
    
    // Load saved stats if available
    fun loadStats(wins: Int, draws: Int, losses: Int) {
        _wins.value = wins
        _draws.value = draws
        _losses.value = losses
        updateWinRate()
    }
    
    // Dismiss the game end dialog
    fun dismissGameEndDialog() {
        _showGameEndDialog.value = false
    }

    // Additional state update methods for View
    fun updateGameBoard(newBoard: Array<Array<String>>?) {
        if (newBoard != null) {
            _gameBoard.value = newBoard
        }
    }
    
    fun updateCurrentTurn(turn: String) {
        _currentTurn.value = turn
    }
    
    fun updateGameWinner(winner: String) {
        _gameWinner.value = winner
    }
    
    fun updateWinningLine(line: WinningLine?) {
        _winningLine.value = line
    }
    
    fun updateWins() {
        _wins.value += 1
        updateWinRate()
    }
    
    fun updateDraws() {
        _draws.value += 1
        updateWinRate()
    }
    
    fun updateLosses() {
        _losses.value += 1
        updateWinRate()
    }
    
    fun updateWinRate(newRate: Int) {
        _winRate.value = newRate
    }
}

// Winning line data
data class WinningLine(
    val startRow: Int,
    val startCol: Int,
    val endRow: Int,
    val endCol: Int,
    val type: LineType
) {
    fun containsCell(row: Int, col: Int): Boolean {
        return when (type) {
            LineType.ROW -> row == startRow && col in startCol..endCol
            LineType.COLUMN -> col == startCol && row in startRow..endRow
            LineType.DIAGONAL_DOWN -> {
                val offset = row - startRow
                row in startRow..endRow && col == startCol + offset
            }
            LineType.DIAGONAL_UP -> {
                val offset = row - startRow
                row in startRow..endRow && col == startCol - offset
            }
        }
    }
}

enum class LineType {
    ROW, COLUMN, DIAGONAL_DOWN, DIAGONAL_UP
} 