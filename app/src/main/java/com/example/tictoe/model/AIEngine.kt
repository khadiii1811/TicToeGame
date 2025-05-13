package com.example.tictoe.model

import android.util.Log
import kotlin.math.abs

object AIEngine {
    private const val EMPTY = 0
    private const val PLAYER = 1
    private const val AI = 2

    private const val MAX_TIME_MS = 1000L
    private const val WIN_SCORE = 1000000
    private var timeStart: Long = 0

    // Difficulty levels
    const val DIFFICULTY_EASY = 1   // Depth 1
    const val DIFFICULTY_NORMAL = 2  // Depth 3
    const val DIFFICULTY_HARD = 3    // Depth 5

    public fun findBestMove(board: Array<IntArray>, difficulty: Int): Pair<Int, Int>? {
        try {
            // Safety check
            if (board.isEmpty() || board[0].isEmpty()) {
                Log.e("TicToe", "Invalid board in findBestMove: empty board")
                return null
            }
            
            Log.d("TicToe", "Finding best move for board with difficulty: $difficulty")
            
            // Convert difficulty to actual search depth
            val searchDepth = when (difficulty) {
                DIFFICULTY_EASY -> 1
                DIFFICULTY_NORMAL -> 3
                DIFFICULTY_HARD -> 5
                else -> 3 // Default to normal
            }
            
            // For easy difficulty, sometimes make random moves
            if (difficulty == DIFFICULTY_EASY && Math.random() < 0.4) {
                val availableMoves = getAvailableMoves(board)
                if (availableMoves.isNotEmpty()) {
                    val randomMove = availableMoves.random()
                    Log.d("TicToe", "Easy difficulty: making random move $randomMove")
                    return randomMove
                }
            }
            
            // Special optimization for 3x3 tic-tac-toe
            if (board.size == 3 && board[0].size == 3 && difficulty >= DIFFICULTY_NORMAL) {
                val move = getOptimalMove(board)
                Log.d("TicToe", "Optimal move found: $move")
                return move
            }
            
        timeStart = System.currentTimeMillis()
        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

            val moves = getAvailableMoves(board)
            if (moves.isEmpty()) {
                Log.e("TicToe", "No available moves found")
                return findAnyEmptyCell(board)
            }
            
            val sortedMoves = moves.shuffled().sortedByDescending { (x, y) ->
            scoreMove(board, x, y, AI) // Heuristic for move ordering
        }

            Log.d("TicToe", "Available moves: $sortedMoves, searchDepth=$searchDepth")

            for ((x, y) in sortedMoves) {
            if (System.currentTimeMillis() - timeStart >= MAX_TIME_MS) break

            board[x][y] = AI
                val score = minimax(board, searchDepth - 1, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE, isMaximizing = false)
            board[x][y] = EMPTY

            if (score > bestScore) {
                bestScore = score
                bestMove = Pair(x, y)
            }
        }

            return bestMove ?: findAnyEmptyCell(board)
        } catch (e: Exception) {
            Log.e("TicToe", "Exception in findBestMove: ${e.message}", e)
            // Fallback: find any empty cell
            return findAnyEmptyCell(board)
        }
    }
    
    private fun findAnyEmptyCell(board: Array<IntArray>): Pair<Int, Int>? {
        for (i in board.indices) {
            for (j in board[0].indices) {
                if (board[i][j] == EMPTY) {
                    Log.d("TicToe", "Fallback: found empty cell at ($i, $j)")
                    return Pair(i, j)
                }
            }
        }
        Log.e("TicToe", "No empty cells found, board is full")
        return null
    }
    
    // Optimized function for 3x3 tic-tac-toe - made safer
    private fun getOptimalMove(board: Array<IntArray>): Pair<Int, Int> {
        try {
            // First check if board is valid
            if (board.size != 3 || board.any { it.size != 3 }) {
                Log.e("TicToe", "Invalid board size in getOptimalMove")
                throw IllegalArgumentException("Invalid board size")
            }
            
            // Debug output of board
            val boardStr = StringBuilder("Current board:\n")
            for (i in 0..2) {
                for (j in 0..2) {
                    boardStr.append(when(board[i][j]) {
                        EMPTY -> "[ ]"
                        PLAYER -> "[X]"
                        AI -> "[O]"
                        else -> "[?]"
                    })
                }
                boardStr.append("\n")
            }
            Log.d("TicToe", boardStr.toString())
            
            // Check if there are any empty cells
            var hasEmptyCell = false
            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col] == EMPTY) {
                        hasEmptyCell = true
                        break
                    }
                }
                if (hasEmptyCell) break
            }
            
            if (!hasEmptyCell) {
                Log.e("TicToe", "Board is full in getOptimalMove")
                throw IllegalStateException("Board is full")
            }
            
            // Check if center is available (best first move)
            if (board[1][1] == EMPTY) {
                Log.d("TicToe", "AI choosing center position")
                return Pair(1, 1)
            }
            
            // Check if AI can win
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = AI
                        val score = evaluate3x3Board(board)
                        board[i][j] = EMPTY
                        if (score >= WIN_SCORE) {
                            Log.d("TicToe", "AI found winning move at ($i, $j)")
                            return Pair(i, j)
                        }
                    }
                }
            }
            
            // Check if opponent can win and block
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = PLAYER
                        val score = evaluate3x3Board(board)
                        board[i][j] = EMPTY
                        if (score <= -WIN_SCORE) {
                            Log.d("TicToe", "AI blocking opponent's winning move at ($i, $j)")
                            return Pair(i, j)
                        }
                    }
                }
            }
            
            // Try to take corners
            val corners = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
            for ((i, j) in corners.shuffled()) {
                if (board[i][j] == EMPTY) {
                    Log.d("TicToe", "AI taking corner position at ($i, $j)")
                    return Pair(i, j)
                }
            }
            
            // Take any available edge
            val edges = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 2), Pair(2, 1))
            for ((i, j) in edges.shuffled()) {
                if (board[i][j] == EMPTY) {
                    Log.d("TicToe", "AI taking edge position at ($i, $j)")
                    return Pair(i, j)
                }
            }
            
            // Fallback to first available cell
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == EMPTY) {
                        Log.d("TicToe", "AI taking last available position at ($i, $j)")
                        return Pair(i, j)
                    }
                }
            }
            
            // Should never reach here but just in case, return null
            Log.e("TicToe", "No available move found")
            throw IllegalStateException("No available move found")
        } catch (e: Exception) {
            Log.e("TicToe", "Exception in getOptimalMove: ${e.message}", e)
            // Last resort fallback
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == EMPTY) {
                        Log.d("TicToe", "AI emergency fallback move at ($i, $j)")
                        return Pair(i, j)
                    }
                }
            }
            Log.e("TicToe", "No empty cell found, returning (0,0) as last resort")
            return Pair(0, 0)
        }
    }
    
    // Evaluate 3x3 board specifically for optimal tic-tac-toe
    private fun evaluate3x3Board(board: Array<IntArray>): Int {
        // Check rows
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                if (board[i][0] == AI) return WIN_SCORE
                if (board[i][0] == PLAYER) return -WIN_SCORE
            }
        }
        
        // Check columns
        for (i in 0..2) {
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                if (board[0][i] == AI) return WIN_SCORE
                if (board[0][i] == PLAYER) return -WIN_SCORE
            }
        }
        
        // Check diagonals
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == AI) return WIN_SCORE
            if (board[0][0] == PLAYER) return -WIN_SCORE
        }
        
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == AI) return WIN_SCORE
            if (board[0][2] == PLAYER) return -WIN_SCORE
        }
        
        return 0
    }

    private fun minimax(
        board: Array<IntArray>,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean
    ): Int {
        var alphaVar = alpha
        var betaVar = beta

        val score = evaluateBoard(board)
        if (depth == 0 || isGameOver(board) ||
            System.currentTimeMillis() - timeStart >= MAX_TIME_MS
        ) {
            return score
        }

        val moves = getAvailableMoves(board).shuffled().sortedByDescending { (x, y) ->
            scoreMove(board, x, y, if (isMaximizing) AI else PLAYER)
        }

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for ((x, y) in moves) {
                board[x][y] = AI
                best = maxOf(best, minimax(board, depth - 1, alphaVar, betaVar, false))
                board[x][y] = EMPTY

                alphaVar = maxOf(alphaVar, best)
                if (betaVar <= alphaVar) break // Beta cut-off
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for ((x, y) in moves) {
                board[x][y] = PLAYER
                best = minOf(best, minimax(board, depth - 1, alphaVar, betaVar, true))
                board[x][y] = EMPTY

                betaVar = minOf(betaVar, best)
                if (betaVar <= alphaVar) break // Alpha cut-off
            }
            best
        }
    }

    private fun isGameOver(board: Array<IntArray>): Boolean {
        val score = evaluateBoard(board)
        return abs(score) >= WIN_SCORE
    }

    private fun evaluateBoard(board: Array<IntArray>): Int {
        val directions = listOf(
            Pair(0, 1),   // horizontal
            Pair(1, 0),   // vertical
            Pair(1, 1),   // diagonal \
            Pair(1, -1)   // diagonal /
        )

        var score = 0
        val rows = board.size
        val cols = board[0].size

        for (x in 0 until rows) {
            for (y in 0 until cols) {
                for (mark in listOf(AI, PLAYER)) {
                    if (board[x][y] == mark) {
                        for ((dx, dy) in directions) {
                            if (isStartOfLine(board, x, y, dx, dy, mark)) {
                                val (count, openEnds) = countPattern(board, x, y, dx, dy, mark)
                                val patternScore = patternScoreWithOpenEnds(count, openEnds)
                                score += if (mark == AI) patternScore else -patternScore * 2 // Strong penalty
                            }
                        }
                    }
                }
            }
        }

        return score
    }

    private fun isStartOfLine(board: Array<IntArray>, x: Int, y: Int, dx: Int, dy: Int, mark: Int): Boolean {
        val prevX = x - dx
        val prevY = y - dy
        return !(prevX in board.indices && prevY in board[0].indices && board[prevX][prevY] == mark)
    }

    // Returns (count, openEnds)
    private fun countPattern(board: Array<IntArray>, x: Int, y: Int, dx: Int, dy: Int, mark: Int): Pair<Int, Int> {
        var count = 1
        var openEnds = 0

        // Forward
        var i = x + dx
        var j = y + dy
        while (i in board.indices && j in board[0].indices && board[i][j] == mark) {
            count++
            i += dx
            j += dy
        }
        if (i in board.indices && j in board[0].indices && board[i][j] == EMPTY) {
            openEnds++
        }

        // Backward
        i = x - dx
        j = y - dy
        while (i in board.indices && j in board[0].indices && board[i][j] == mark) {
            count++
            i -= dx
            j -= dy
        }
        if (i in board.indices && j in board[0].indices && board[i][j] == EMPTY) {
            openEnds++
        }

        return Pair(count, openEnds)
    }

    private fun patternScoreWithOpenEnds(count: Int, openEnds: Int): Int {
        return when {
            count >= 5 -> 1000000
            count == 4 && openEnds == 2 -> 100000
            count == 4 && openEnds == 1 -> 10000
            count == 3 && openEnds == 2 -> 1000
            count == 3 && openEnds == 1 -> 100
            count == 2 && openEnds == 2 -> 50
            count == 2 && openEnds == 1 -> 10
            else -> 0
        }
    }

    private fun scoreMove(board: Array<IntArray>, x: Int, y: Int, mark: Int): Int {
        board[x][y] = mark
        val score = evaluateBoard(board)
        board[x][y] = EMPTY
        return score
    }

    private fun getAvailableMoves(board: Array<IntArray>): List<Pair<Int, Int>> {
        val rows = board.size
        val cols = board[0].size
        val moves = mutableSetOf<Pair<Int, Int>>()

        for (x in 0 until rows) {
            for (y in 0 until cols) {
                if (board[x][y] != EMPTY) {
                    for (dx in -2..2) {
                        for (dy in -2..2) {
                            val nx = x + dx
                            val ny = y + dy
                            if (nx in 0 until rows && ny in 0 until cols && board[nx][ny] == EMPTY) {
                                moves.add(Pair(nx, ny))
                            }
                        }
                    }
                }
            }
        }

        return moves.toList()
    }
}