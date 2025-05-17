package com.example.tictoe.network

import android.util.Log
import com.example.tictoe.model.RoomInfo
import com.google.gson.Gson
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONObject
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket server for Tic-tac-toe game
 */
class TicTacToeServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {
    
    private val tag = "TicTacToeServer"
    private val gson = Gson()
    
    // Store games and players
    private val games = ConcurrentHashMap<String, Game>()
    private val players = ConcurrentHashMap<WebSocket, Player>()
    private val waitingPlayers = mutableListOf<Player>()
    
    // Store rooms 
    private val rooms = ConcurrentHashMap<String, Room>()
    
    override fun onStart() {
        Log.d(tag, "WebSocket server started on port ${address.port}")
    }
    
    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Log.d(tag, "New client connected: ${conn.remoteSocketAddress}")
    }
    
    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        Log.d(tag, "Client disconnected: ${conn.remoteSocketAddress}, code: $code, reason: $reason, remote: $remote")
        val player = players[conn]
        if (player != null) {
            handlePlayerDisconnect(player)
            players.remove(conn)
        }
    }
    
    override fun onMessage(conn: WebSocket, message: String) {
        Log.d(tag, "Received message: $message")
        try {
            val jsonObject = JSONObject(message)
            val type = jsonObject.optString("type")
            
            when (type) {
                "CONNECT" -> handleConnectMessage(conn, jsonObject)
                "MOVE" -> handleMoveMessage(conn, jsonObject)
                "CHAT" -> handleChatMessage(conn, jsonObject)
                "DISCONNECT" -> handleDisconnectMessage(conn, jsonObject)
                "LIST_ROOMS" -> handleListRoomsMessage(conn, jsonObject)
                "CREATE_ROOM" -> handleCreateRoomMessage(conn, jsonObject)
                "JOIN_ROOM" -> handleJoinRoomMessage(conn, jsonObject)
                else -> Log.e(tag, "Unknown message type: $type")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error processing message", e)
        }
    }
    
    override fun onError(conn: WebSocket, ex: Exception) {
        Log.e(tag, "Error occurred on connection ${conn?.remoteSocketAddress}: ${ex.message}")
        ex.printStackTrace()
    }
    
    /**
     * Handle connect message
     */
    private fun handleConnectMessage(conn: WebSocket, message: JSONObject) {
        val playerName = message.optString("playerName")
        val requestGameId = message.optString("requestGameId", null)
        
        Log.d(tag, "Connect from $playerName, requestGameId: $requestGameId")
        
        // Create player
        val player = Player(conn, playerName)
        players[conn] = player
        
        if (!requestGameId.isNullOrEmpty() && games.containsKey(requestGameId)) {
            // Join existing game
            val game = games[requestGameId]
            if (game != null && game.player2 == null) {
                game.player2 = player
                game.status = "PLAYING"
                
                // Send game state to both players
                sendGameState(game)
                
                Log.d(tag, "Player $playerName joined game ${game.gameId}")
            } else {
                // Game already full or doesn't exist, create new game
                createNewGame(player)
            }
        } else {
            // Check for waiting players
            if (waitingPlayers.isNotEmpty()) {
                val opponent = waitingPlayers.removeAt(0)
                
                // Create game with both players
                val gameId = UUID.randomUUID().toString()
                val game = Game(
                    gameId = gameId,
                    player1 = opponent,
                    player2 = player,
                    board = Array(3) { Array(3) { "" } },
                    currentPlayer = opponent.name,
                    winner = null,
                    isDraw = false,
                    status = "PLAYING"
                )
                
                games[gameId] = game
                
                // Send game state to both players
                sendGameState(game)
                
                Log.d(tag, "Created game $gameId with players ${opponent.name} and ${player.name}")
            } else {
                // No waiting player, add to waiting list and create new game
                waitingPlayers.add(player)
                createNewGame(player)
            }
        }
    }
    
    /**
     * Create new game for player
     */
    private fun createNewGame(player: Player) {
        val gameId = UUID.randomUUID().toString()
        val game = Game(
            gameId = gameId,
            player1 = player,
            player2 = null,
            board = Array(3) { Array(3) { "" } },
            currentPlayer = player.name,
            winner = null,
            isDraw = false,
            status = "WAITING_OPPONENT"
        )
        
        games[gameId] = game
        
        // Send game state to player
        sendGameState(game)
        
        Log.d(tag, "Created new game $gameId with player ${player.name}")
    }
    
    /**
     * Handle move message
     */
    private fun handleMoveMessage(conn: WebSocket, message: JSONObject) {
        val gameId = message.optString("gameId")
        val playerName = message.optString("playerName")
        val row = message.optInt("row")
        val col = message.optInt("col")
        
        Log.d(tag, "Move from $playerName in game $gameId: $row, $col")
        
        val game = games[gameId] ?: return
        
        // Check if it's the player's turn
        if (game.currentPlayer != playerName) {
            Log.d(tag, "Not $playerName's turn")
            return
        }
        
        // Check valid move
        if (row < 0 || row >= 3 || col < 0 || col >= 3 || game.board[row][col].isNotEmpty()) {
            Log.d(tag, "Invalid move: $row, $col")
            return
        }
        
        // Make the move
        game.board[row][col] = game.currentPlayer
        
        // Check for win or draw
        checkGameResult(game)
        
        // Switch player
        if (game.status == "PLAYING") {
            game.currentPlayer = if (game.currentPlayer == game.player1.name) game.player2?.name ?: game.player1.name else game.player1.name
        }
        
        // Send updated game state
        sendGameState(game)
    }
    
    /**
     * Check game result (win or draw)
     */
    private fun checkGameResult(game: Game) {
        // Check rows
        for (i in 0..2) {
            if (game.board[i][0].isNotEmpty() && 
                game.board[i][0] == game.board[i][1] && 
                game.board[i][1] == game.board[i][2]) {
                game.winner = game.currentPlayer
                game.status = "GAME_OVER"
                return
            }
        }
        
        // Check columns
        for (i in 0..2) {
            if (game.board[0][i].isNotEmpty() && 
                game.board[0][i] == game.board[1][i] && 
                game.board[1][i] == game.board[2][i]) {
                game.winner = game.currentPlayer
                game.status = "GAME_OVER"
                return
            }
        }
        
        // Check diagonals
        if (game.board[0][0].isNotEmpty() && 
            game.board[0][0] == game.board[1][1] && 
            game.board[1][1] == game.board[2][2]) {
            game.winner = game.currentPlayer
            game.status = "GAME_OVER"
            return
        }
        
        if (game.board[0][2].isNotEmpty() && 
            game.board[0][2] == game.board[1][1] && 
            game.board[1][1] == game.board[2][0]) {
            game.winner = game.currentPlayer
            game.status = "GAME_OVER"
            return
        }
        
        // Check for draw
        var isDraw = true
        for (i in 0..2) {
            for (j in 0..2) {
                if (game.board[i][j].isEmpty()) {
                    isDraw = false
                    break
                }
            }
            if (!isDraw) break
        }
        
        if (isDraw) {
            game.isDraw = true
            game.status = "GAME_OVER"
        }
    }
    
    /**
     * Handle chat message
     */
    private fun handleChatMessage(conn: WebSocket, message: JSONObject) {
        val gameId = message.optString("gameId")
        val playerName = message.optString("playerName")
        val chatMessage = message.optString("message")
        
        Log.d(tag, "Chat from $playerName in game $gameId: $chatMessage")
        
        val game = games[gameId] ?: return
        
        // Send chat to both players
        val chatData = JSONObject().apply {
            put("type", "CHAT")
            put("gameId", gameId)
            put("playerName", playerName)
            put("message", chatMessage)
        }
        
        val chatJson = chatData.toString()
        game.player1.socket.send(chatJson)
        game.player2?.socket?.send(chatJson)
    }
    
    /**
     * Handle disconnect message
     */
    private fun handleDisconnectMessage(conn: WebSocket, message: JSONObject) {
        val gameId = message.optString("gameId")
        val playerName = message.optString("playerName")
        val reason = message.optString("reason", "Player disconnected")
        
        Log.d(tag, "Disconnect from $playerName in game $gameId: $reason")
        
        val player = players[conn] ?: return
        handlePlayerDisconnect(player)
    }
    
    /**
     * Handle list rooms message
     */
    private fun handleListRoomsMessage(conn: WebSocket, message: JSONObject) {
        val lastUpdate = message.optLong("lastUpdate", 0)
        
        Log.d(tag, "List rooms request from ${conn.remoteSocketAddress}, lastUpdate: $lastUpdate")
        
        // Convert rooms to RoomInfo list
        val roomInfoList = rooms.values.map { room -> 
            RoomInfo(
                id = room.id,
                name = room.name,
                hostName = room.host.name,
                playerCount = room.players.size,
                maxPlayers = room.maxPlayers,
                status = room.status,
                createdAt = room.createdAt
            )
        }
        
        // Send room list response
        val roomListData = JSONObject().apply {
            put("type", "ROOM_LIST")
            put("rooms", gson.toJson(roomInfoList))
            put("timestamp", System.currentTimeMillis())
        }
        
        conn.send(roomListData.toString())
        Log.d(tag, "Sent room list with ${roomInfoList.size} rooms")
    }
    
    /**
     * Handle create room message
     */
    private fun handleCreateRoomMessage(conn: WebSocket, message: JSONObject) {
        val roomName = message.optString("roomName")
        val hostName = message.optString("hostName")
        val maxPlayers = message.optInt("maxPlayers", 2)
        
        Log.d(tag, "Create room request: $roomName by $hostName, maxPlayers: $maxPlayers")
        
        // Get or create player
        val player = players[conn] ?: Player(conn, hostName).also { players[conn] = it }
        
        // Create room
        val roomId = UUID.randomUUID().toString()
        val room = Room(
            id = roomId,
            name = roomName,
            host = player,
            maxPlayers = maxPlayers,
            status = "WAITING",
            createdAt = System.currentTimeMillis()
        )
        room.players.add(player)
        
        rooms[roomId] = room
        
        // Send room info to creator
        val roomInfo = RoomInfo(
            id = room.id,
            name = room.name,
            hostName = room.host.name,
            playerCount = room.players.size,
            maxPlayers = room.maxPlayers,
            status = room.status,
            createdAt = room.createdAt
        )
        
        val roomUpdateData = JSONObject().apply {
            put("type", "ROOM_UPDATE")
            put("room", gson.toJson(roomInfo))
            put("event", "CREATED")
        }
        
        conn.send(roomUpdateData.toString())
        
        // Broadcast room creation to all connected clients
        broadcastRoomUpdate(room, "CREATED")
        
        Log.d(tag, "Created room $roomId: $roomName by $hostName")
    }
    
    /**
     * Handle join room message
     */
    private fun handleJoinRoomMessage(conn: WebSocket, message: JSONObject) {
        val roomId = message.optString("roomId")
        val playerName = message.optString("playerName")
        
        Log.d(tag, "Join room request: $roomId by $playerName")
        
        val room = rooms[roomId]
        if (room == null) {
            Log.d(tag, "Room not found: $roomId")
            val errorData = JSONObject().apply {
                put("type", "ERROR")
                put("message", "Room not found")
            }
            conn.send(errorData.toString())
            return
        }
        
        // Check if room is full
        if (room.players.size >= room.maxPlayers) {
            Log.d(tag, "Room is full: $roomId")
            val errorData = JSONObject().apply {
                put("type", "ERROR")
                put("message", "Room is full")
            }
            conn.send(errorData.toString())
            return
        }
        
        // Get or create player
        val player = players[conn] ?: Player(conn, playerName).also { players[conn] = it }
        
        // Add player to room
        room.players.add(player)
        
        // Create game if room is now full
        if (room.players.size == room.maxPlayers) {
            room.status = "PLAYING"
            
            // Create new game for room players
            val gameId = UUID.randomUUID().toString()
            val game = Game(
                gameId = gameId,
                player1 = room.host,
                player2 = room.players.find { it != room.host },
                board = Array(3) { Array(3) { "" } },
                currentPlayer = room.host.name,
                winner = null,
                isDraw = false,
                status = "PLAYING"
            )
            
            games[gameId] = game
            room.gameId = gameId
            
            // Send game state to players
            sendGameState(game)
            
            Log.d(tag, "Created game $gameId for room $roomId")
        }
        
        // Send room update to all players in room
        val roomInfo = RoomInfo(
            id = room.id,
            name = room.name,
            hostName = room.host.name,
            playerCount = room.players.size,
            maxPlayers = room.maxPlayers,
            status = room.status,
            createdAt = room.createdAt
        )
        
        val roomUpdateData = JSONObject().apply {
            put("type", "ROOM_UPDATE")
            put("room", gson.toJson(roomInfo))
            put("event", "PLAYER_JOINED")
            put("playerName", playerName)
        }
        
        for (p in room.players) {
            p.socket.send(roomUpdateData.toString())
        }
        
        // Broadcast room update to all connected clients
        broadcastRoomUpdate(room, "UPDATED")
        
        Log.d(tag, "Player $playerName joined room $roomId")
    }
    
    /**
     * Broadcast room update to all connected clients
     */
    private fun broadcastRoomUpdate(room: Room, event: String) {
        val roomInfo = RoomInfo(
            id = room.id,
            name = room.name,
            hostName = room.host.name,
            playerCount = room.players.size,
            maxPlayers = room.maxPlayers,
            status = room.status,
            createdAt = room.createdAt
        )
        
        val roomUpdateData = JSONObject().apply {
            put("type", "ROOM_UPDATE")
            put("room", gson.toJson(roomInfo))
            put("event", event)
        }
        
        val updateJson = roomUpdateData.toString()
        broadcast(updateJson)
    }
    
    /**
     * Handle player disconnect
     */
    private fun handlePlayerDisconnect(player: Player) {
        // Handle room membership
        for ((roomId, room) in rooms) {
            if (room.players.contains(player)) {
                // Remove player from room
                room.players.remove(player)
                
                // If player was host, remove the room or transfer host
                if (room.host == player) {
                    if (room.players.isEmpty()) {
                        // Remove empty room
                        rooms.remove(roomId)
                        Log.d(tag, "Removed empty room $roomId after host disconnect")
                    } else {
                        // Transfer host to next player
                        room.host = room.players.first()
                        
                        // Notify remaining players
                        val roomInfo = RoomInfo(
                            id = room.id,
                            name = room.name,
                            hostName = room.host.name,
                            playerCount = room.players.size,
                            maxPlayers = room.maxPlayers,
                            status = room.status,
                            createdAt = room.createdAt
                        )
                        
                        val hostChangeData = JSONObject().apply {
                            put("type", "ROOM_UPDATE")
                            put("room", gson.toJson(roomInfo))
                            put("event", "HOST_CHANGED")
                        }
                        
                        for (p in room.players) {
                            p.socket.send(hostChangeData.toString())
                        }
                        
                        Log.d(tag, "Transferred host in room $roomId to ${room.host.name}")
                    }
                } else {
                    // Notify remaining players
                    val roomInfo = RoomInfo(
                        id = room.id,
                        name = room.name,
                        hostName = room.host.name,
                        playerCount = room.players.size,
                        maxPlayers = room.maxPlayers,
                        status = room.status,
                        createdAt = room.createdAt
                    )
                    
                    val playerLeftData = JSONObject().apply {
                        put("type", "ROOM_UPDATE")
                        put("room", gson.toJson(roomInfo))
                        put("event", "PLAYER_LEFT")
                        put("playerName", player.name)
                    }
                    
                    for (p in room.players) {
                        p.socket.send(playerLeftData.toString())
                    }
                }
                
                // Broadcast room update
                if (rooms.containsKey(roomId)) {
                    broadcastRoomUpdate(room, "UPDATED")
                }
                
                break
            }
        }
        
        // Find game with player
        for ((gameId, game) in games) {
            if (game.player1.socket.remoteSocketAddress == player.socket.remoteSocketAddress || 
                game.player2?.socket?.remoteSocketAddress == player.socket.remoteSocketAddress) {
                
                // Notify remaining player
                val disconnectedPlayer = if (game.player1.socket.remoteSocketAddress == player.socket.remoteSocketAddress) {
                    game.player1
                } else {
                    game.player2
                }
                
                val remainingPlayer = if (game.player1.socket.remoteSocketAddress == player.socket.remoteSocketAddress) {
                    game.player2
                } else {
                    game.player1
                }
                
                if (remainingPlayer != null) {
                    val disconnectData = JSONObject().apply {
                        put("type", "DISCONNECT")
                        put("gameId", gameId)
                        put("playerName", disconnectedPlayer?.name)
                        put("reason", "Player disconnected")
                    }
                    
                    remainingPlayer.socket.send(disconnectData.toString())
                }
                
                // Remove game
                games.remove(gameId)
                break
            }
        }
        
        // Remove from waiting list
        waitingPlayers.removeIf { it.socket.remoteSocketAddress == player.socket.remoteSocketAddress }
    }
    
    /**
     * Send game state to players
     */
    private fun sendGameState(game: Game) {
        val boardList = game.board.map { row -> row.toList() }
        
        val gameStateData = JSONObject().apply {
            put("type", "GAME_STATE")
            put("gameId", game.gameId)
            put("board", boardList)
            put("currentPlayer", game.currentPlayer)
            put("player1", game.player1.name)
            put("player2", game.player2?.name ?: "")
            put("winner", game.winner ?: JSONObject.NULL)
            put("isDraw", game.isDraw)
            put("gameStatus", game.status)
        }
        
        val gameStateJson = gameStateData.toString()
        game.player1.socket.send(gameStateJson)
        game.player2?.socket?.send(gameStateJson)
    }
    
    /**
     * Player data class
     */
    data class Player(val socket: WebSocket, val name: String)
    
    /**
     * Game data class
     */
    data class Game(
        val gameId: String,
        val player1: Player,
        var player2: Player?,
        val board: Array<Array<String>>,
        var currentPlayer: String,
        var winner: String?,
        var isDraw: Boolean,
        var status: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as Game
            
            if (gameId != other.gameId) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            return gameId.hashCode()
        }
    }
    
    /**
     * Room data class
     */
    data class Room(
        val id: String,
        val name: String,
        var host: Player,
        val maxPlayers: Int,
        var status: String,
        val createdAt: Long,
        var gameId: String? = null
    ) {
        val players = mutableListOf<Player>()
        
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as Room
            
            if (id != other.id) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            return id.hashCode()
        }
    }
} 