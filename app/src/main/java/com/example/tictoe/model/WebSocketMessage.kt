package com.example.tictoe.model

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/**
 * Base sealed class for WebSocket messages with type field in the constructor
 */
sealed class WebSocketMessage(
    @SerializedName("type")
    val type: String
) {

    companion object {
        // Custom GSON instance that can handle both string and array board formats
        private val gson = GsonBuilder()
            .registerTypeAdapter(GameStateMessage::class.java, GameStateMessageDeserializer())
            .create()

        /**
         * Convert JSON string to message object
         */
        fun fromJson(json: String): WebSocketMessage? {
            if (json.isBlank()) {
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
                    "LIST_ROOMS" -> gson.fromJson(json, ListRoomsMessage::class.java)
                    "ROOM_LIST" -> gson.fromJson(json, RoomListMessage::class.java)
                    "CREATE_ROOM" -> gson.fromJson(json, CreateRoomMessage::class.java)
                    "JOIN_ROOM" -> gson.fromJson(json, JoinRoomMessage::class.java)
                    "ROOM_UPDATE" -> gson.fromJson(json, RoomUpdateMessage::class.java)
                    "SERVER_STATUS" -> gson.fromJson(json, ServerStatusMessage::class.java)
                    else -> {
                        Log.e("WebSocketMessage", "Unknown message type: $type")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocketMessage", "Error parsing message: ${e.message}\nJSON: $json", e)
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
 * Custom deserializer for GameStateMessage to handle both string and array board formats
 */
class GameStateMessageDeserializer : JsonDeserializer<GameStateMessage> {
    override fun deserialize(
        json: JsonElement, 
        typeOfT: Type, 
        context: JsonDeserializationContext
    ): GameStateMessage {
        val jsonObject = json.asJsonObject
        
        // Extract all fields directly
        val gameId = jsonObject.get("gameId").asString
        val currentPlayer = jsonObject.get("currentPlayer").asString
        val player1 = jsonObject.get("player1").asString
        val player2 = jsonObject.get("player2").asString
        
        // Handle both string and array board formats
        val boardElement = jsonObject.get("board")
        val board: List<List<String>> = if (boardElement.isJsonArray) {
            // If board is already an array, parse it normally
            val boardArray = mutableListOf<List<String>>()
            val boardJsonArray = boardElement.asJsonArray
            
            for (row in boardJsonArray) {
                val rowList = mutableListOf<String>()
                val rowArray = row.asJsonArray
                
                for (cell in rowArray) {
                    rowList.add(if (cell.isJsonNull) "" else cell.asString)
                }
                
                boardArray.add(rowList)
            }
            boardArray
        } else {
            // If board is a string, parse it manually
            try {
                val boardString = boardElement.asString
                // Convert "[[, , ], [, , ], [, , ]]" to proper list
                val rows = boardString.trim('[', ']').split("],[")
                
                rows.map { row ->
                    row.trim('[', ']').split(",").map { cell ->
                        cell.trim()
                    }
                }
            } catch (e: Exception) {
                Log.e("GameStateDeserializer", "Error parsing board string: ${e.message}", e)
                // Return empty 3x3 board as fallback
                listOf(
                    listOf("", "", ""),
                    listOf("", "", ""),
                    listOf("", "", "")
                )
            }
        }
        
        // Handle optional fields with null checks
        val winner = if (jsonObject.has("winner") && !jsonObject.get("winner").isJsonNull) {
            jsonObject.get("winner").asString
        } else {
            null
        }
        
        val isDraw = if (jsonObject.has("isDraw")) {
            jsonObject.get("isDraw").asBoolean
        } else {
            false
        }
        
        val gameStatus = jsonObject.get("gameStatus").asString
        
        return GameStateMessage(
            gameId = gameId,
            board = board,
            currentPlayer = currentPlayer,
            player1 = player1,
            player2 = player2,
            winner = winner,
            isDraw = isDraw,
            gameStatus = gameStatus
        )
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

/**
 * Request to list rooms
 */
data class ListRoomsMessage(
    @SerializedName("lastUpdate")
    val lastUpdate: Long = 0
) : WebSocketMessage("LIST_ROOMS")

/**
 * Response with list of rooms
 */
data class RoomListMessage(
    @SerializedName("rooms")
    val rooms: List<RoomInfo>,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
) : WebSocketMessage("ROOM_LIST")

/**
 * Create a new room
 */
data class CreateRoomMessage(
    @SerializedName("roomName")
    val roomName: String,
    @SerializedName("hostName")
    val hostName: String,
    @SerializedName("maxPlayers")
    val maxPlayers: Int = 2
) : WebSocketMessage("CREATE_ROOM")

/**
 * Join an existing room
 */
data class JoinRoomMessage(
    @SerializedName("roomId")
    val roomId: String,
    @SerializedName("playerName") 
    val playerName: String
) : WebSocketMessage("JOIN_ROOM")

/**
 * Room update notification
 */
data class RoomUpdateMessage(
    @SerializedName("room")
    val room: RoomInfo,
    @SerializedName("event")
    val event: String // CREATED, UPDATED, PLAYER_JOINED, PLAYER_LEFT, GAME_STARTED, GAME_ENDED
) : WebSocketMessage("ROOM_UPDATE")

/**
 * Server status information
 */
data class ServerStatusMessage(
    @SerializedName("activeConnections")
    val activeConnections: Int,
    @SerializedName("activeGames") 
    val activeGames: Int,
    @SerializedName("cpuUsage")
    val cpuUsage: Float,
    @SerializedName("memoryUsage")
    val memoryUsage: Float,
    @SerializedName("uptime")
    val uptime: Long
) : WebSocketMessage("SERVER_STATUS")

/**
 * Room information
 */
data class RoomInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("name") 
    val name: String,
    @SerializedName("hostName")
    val hostName: String,
    @SerializedName("playerCount")
    val playerCount: Int,
    @SerializedName("maxPlayers")
    val maxPlayers: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("createdAt")
    val createdAt: Long
)