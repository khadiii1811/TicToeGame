package com.example.tictoe.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Base sealed class for WebSocket messages with type field in the constructor
 */
sealed class WebSocketMessage(
    @SerializedName("type")
    val type: String
) {

    companion object {
        private val gson = Gson()

        /**
         * Convert JSON string to message object
         */
        fun fromJson(json: String): WebSocketMessage? {
            if (json.isNullOrEmpty()) {
                return null
            }
            
            return try {
                // Read message type
                val mapType = gson.fromJson(json, Map::class.java) ?: return null
                val type = mapType["type"]?.toString() ?: return null

                // Parse message based on type
                when (type) {
                    "CONNECT" -> gson.fromJson(json, ConnectMessage::class.java)
                    "GAME_STATE" -> gson.fromJson(json, GameStateMessage::class.java)
                    "MOVE" -> gson.fromJson(json, MoveMessage::class.java)
                    "CHAT" -> gson.fromJson(json, ChatMessage::class.java)
                    "DISCONNECT" -> gson.fromJson(json, DisconnectMessage::class.java)
                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Convert message object to JSON string
         */
        fun toJson(message: WebSocketMessage): String {
            return gson.toJson(message)
        }
    }
}

/**
 * Connection message
 */
data class ConnectMessage(
    @SerializedName("playerName")
    val playerName: String,
    @SerializedName("requestGameId")
    val requestGameId: String? = null
) : WebSocketMessage("CONNECT")

/**
 * Game state message
 */
data class GameStateMessage(
    @SerializedName("gameId")
    val gameId: String,
    @SerializedName("board")
    val board: List<List<String>>,
    @SerializedName("currentPlayer")
    val currentPlayer: String,
    @SerializedName("player1")
    val player1: String,
    @SerializedName("player2")
    val player2: String,
    @SerializedName("winner")
    val winner: String? = null,
    @SerializedName("isDraw")
    val isDraw: Boolean = false,
    @SerializedName("gameStatus")
    val gameStatus: String
) : WebSocketMessage("GAME_STATE")

/**
 * Move message
 */
data class MoveMessage(
    @SerializedName("gameId")
    val gameId: String,
    @SerializedName("playerName")
    val playerName: String,
    @SerializedName("row")
    val row: Int,
    @SerializedName("col")
    val col: Int
) : WebSocketMessage("MOVE")

/**
 * Chat message
 */
data class ChatMessage(
    @SerializedName("gameId")
    val gameId: String,
    @SerializedName("playerName")
    val playerName: String,
    @SerializedName("message")
    val message: String
) : WebSocketMessage("CHAT")

/**
 * Disconnect message
 */
data class DisconnectMessage(
    @SerializedName("gameId")
    val gameId: String,
    @SerializedName("playerName")
    val playerName: String,
    @SerializedName("reason")
    val reason: String? = null
) : WebSocketMessage("DISCONNECT")