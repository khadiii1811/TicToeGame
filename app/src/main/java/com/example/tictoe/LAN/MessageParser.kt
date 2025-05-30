package com.example.tictoe.LAN

fun moveToMessage(move: Pair<Int, Int>): String = "$MSG_MOVE ${move.first},${move.second}"

fun parseMessage(message: String): Any? {
    return when {
        message.startsWith(MSG_MOVE) -> {
            val coords = message.removePrefix("$MSG_MOVE ").split(",")
            val x = coords.getOrNull(0)?.toIntOrNull()
            val y = coords.getOrNull(1)?.toIntOrNull()
            if (x != null && y != null) Pair(x, y) else null
        }
        message.startsWith(MSG_PLAYER_NAME) -> {
            message.removePrefix("$MSG_PLAYER_NAME ")
        }
        message == MSG_REMATCH -> MSG_REMATCH
        message == MSG_DISCONNECT -> MSG_DISCONNECT
        else -> null
    }
}

fun messageToData(message: Any): Any {
    return when (message) {
        is Pair<*, *> -> {
            val (first, second) = message
            if (first is Int && second is Int) {
                moveToMessage(Pair(first, second))
            } else {
                return Unit
            }
        }
        is String -> message
        else -> return Unit
    }
}
