package com.example.tictoe.LAN

import android.util.Log

fun moveToMessage(move: Pair<Int, Int>): String = "$MSG_MOVE ${move.first},${move.second}"

fun parseMessage(message: String): Any? {
    Log.d("MessageParser", "Parsing message: $message")
    return when {
        message.startsWith(MSG_MOVE) -> {
            try {
                val coords = message.removePrefix("$MSG_MOVE ").split(",")
                val x = coords.getOrNull(0)?.trim()?.toIntOrNull()
                val y = coords.getOrNull(1)?.trim()?.toIntOrNull()
                if (x != null && y != null) {
                    Log.d("MessageParser", "Parsed move: ($x, $y)")
                    Pair(x, y)
                } else {
                    Log.e("MessageParser", "Failed to parse coordinates from: $coords")
                    null
                }
            } catch (e: Exception) {
                Log.e("MessageParser", "Error parsing move message: ${e.message}")
                null
            }
        }
        message.startsWith(MSG_PLAYER_NAME) -> {
            val name = message.removePrefix("$MSG_PLAYER_NAME ").trim()
            Log.d("MessageParser", "Parsed player name: $name")
            name
        }
        message == MSG_REMATCH -> {
            Log.d("MessageParser", "Parsed rematch message")
            MSG_REMATCH
        }
        message == MSG_DISCONNECT -> {
            Log.d("MessageParser", "Parsed disconnect message")
            MSG_DISCONNECT
        }
        else -> {
            Log.w("MessageParser", "Unknown message format: $message")
            null
        }
    }
}

fun messageToData(message: Any): String {
    return when (message) {
        is Pair<*, *> -> {
            val (first, second) = message
            if (first is Int && second is Int) {
                moveToMessage(Pair(first, second))
            } else {
                Log.e("MessageParser", "Invalid move data: $message")
                ""
            }
        }
        is String -> message
        else -> {
            Log.e("MessageParser", "Unsupported message type: ${message.javaClass}")
            ""
        }
    }
}
