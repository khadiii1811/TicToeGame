package com.example.tictoe.AI

import kotlin.math.abs

object AIEngine {
    private const val EMPTY = 0
    private const val PLAYER = 1
    private const val AI = 2

    private const val MAX_TIME_MS = 1000L
    private const val WIN_SCORE = 1000000
    private var timeStart: Long = 0


    public fun findBestMove(board: Array<IntArray>, depth: Int): Pair<Int, Int>? {
        timeStart = System.currentTimeMillis()
        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

        val moves = getAvailableMoves(board).shuffled().sortedByDescending { (x, y) ->
            scoreMove(board, x, y, AI) // Heuristic for move ordering
        }

        for ((x, y) in moves) {
            if (System.currentTimeMillis() - timeStart >= MAX_TIME_MS) break

            board[x][y] = AI
            val score = minimax(board, depth - 1, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE, isMaximizing = false)
            board[x][y] = EMPTY

            if (score > bestScore) {
                bestScore = score
                bestMove = Pair(x, y)
            }
        }

        return bestMove
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
            System.currentTimeMillis() - timeStart >= MAX_TIME_MS) {
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