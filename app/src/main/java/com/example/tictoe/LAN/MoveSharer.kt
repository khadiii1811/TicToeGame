package com.example.tictoe.LAN

interface MoveSharer {
    fun shareMoveWithOpponent(row: Int, col: Int)
}