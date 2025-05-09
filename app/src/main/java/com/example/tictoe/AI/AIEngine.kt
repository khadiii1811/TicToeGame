package com.example.tictoe.AI

object AIEngine {
    private const val EMPTY = 0
    private const val PLAYER = 1
    private const val AI = 2
    private const val MAX_TIME_MS = 300L

    fun findBestMove(board: Array<IntArray>, depth: Int): Pair<Int, Int>? {
        val deadline = System.currentTimeMillis() + MAX_TIME_MS

        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

        for ((x, y) in getAvailableMoves(board)) {
            board[x][y] = AI
            val score = minimax(board, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, false, deadline)
            board[x][y] = EMPTY

            if (score > bestScore) {
                bestScore = score
                bestMove = Pair(x, y)
            }

            if (System.currentTimeMillis() >= deadline) break
        }

        return bestMove
    }

    private fun minimax(
        board: Array<IntArray>,
        depth: Int,
        alphaInit: Int,
        betaInit: Int,
        isMaximizing: Boolean,
        deadline: Long
    ): Int {
        if (System.currentTimeMillis() >= deadline) return evaluateBoard(board)

        var alpha = alphaInit
        var beta = betaInit

        if (depth == 0) return evaluateBoard(board)

        val mark = if (isMaximizing) AI else PLAYER
        val moves = getAvailableMoves(board)
            .map { move -> Pair(move, scoreMove(board, move.first, move.second, mark)) }
            .sortedByDescending { it.second }

        if (moves.isEmpty()) return 0

        return if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for ((move, _) in moves) {
                val (x, y) = move
                board[x][y] = AI
                val eval = minimax(board, depth - 1, alpha, beta, false, deadline)
                board[x][y] = EMPTY
                maxEval = maxOf(maxEval, eval)
                alpha = maxOf(alpha, eval)
                if (beta <= alpha) break
            }
            maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for ((move, _) in moves) {
                val (x, y) = move
                board[x][y] = PLAYER
                val eval = minimax(board, depth - 1, alpha, beta, true, deadline)
                board[x][y] = EMPTY
                minEval = minOf(minEval, eval)
                beta = minOf(beta, eval)
                if (beta <= alpha) break
            }
            minEval
        }
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
                val mark = board[x][y]
                if (mark != EMPTY) {
                    for ((dx, dy) in directions) {
                        if (isStartOfLine(board, x, y, dx, dy, mark)) {
                            val pattern = countPattern(board, x, y, dx, dy, mark)
                            score += patternScoreWithOpenEnds(pattern.first, pattern.second, mark)
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
    fun countPattern(board: Array<IntArray>, x: Int, y: Int, dx: Int, dy: Int, mark: Int): Pair<Int, Int> {
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

    // Pattern scoring with open ends
    fun patternScoreWithOpenEnds(count: Int, openEnds: Int, mark: Int): Int {
        val score = when {
            count >= 5 -> 100000
            count == 4 && openEnds == 2 -> 10000     // open four
            count == 4 && openEnds == 1 -> 5000      // closed four
            count == 3 && openEnds == 2 -> 1000      // open three
            count == 3 && openEnds == 1 -> 300       // closed three
            count == 2 && openEnds == 2 -> 100       // open two
            count == 2 && openEnds == 1 -> 10        // closed two
            else -> 0
        }
        return if (mark == AI) score else -score
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