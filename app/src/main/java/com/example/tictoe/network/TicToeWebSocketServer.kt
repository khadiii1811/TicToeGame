package com.example.tictoe.network

import android.util.Log
import com.example.tictoe.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket server for Tic Tac Toe game
 */
class TicToeWebSocketServer(
    host: String,
    port: Int,
    private val coroutineScope: CoroutineScope
) : WebSocketServer(InetSocketAddress(host, port)) {
    private val tag = "TicToeWebSocketServer"
    
    // Map of WebSocket connections to player names
    private val players = ConcurrentHashMap<WebSocket, String>()
    
    // Map of games by gameId
    private val games = ConcurrentHashMap<String, GameRoom>()
    
    // Flow for server events
    private val _serverEvents = MutableSharedFlow<ServerEvent>()
    val serverEvents: SharedFlow<ServerEvent> = _serverEvents
    
    init {
        connectionLostTimeout = 60 // 60 seconds
    }
    
    /**
     * When a new connection is established
     */
    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        val address = conn.remoteSocketAddress.address.hostAddress
        Log.d(tag, "New connection from: $address")
        
        coroutineScope.launch {
            _serverEvents.emit(ServerEvent.ClientConnected(address))
        }
    }
    
    /**
     * When a connection is closed
     */
    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        val address = conn.remoteSocketAddress.address.hostAddress
        val playerName = players[conn]
        
        Log.d(tag, "Connection closed from: $address, player: $playerName")
        
        if (playerName == null) {
            Log.w(tag, "Unknown player disconnected from $address")
            return
        }
        
        // Find the game the player is participating in
        val gameEntry = games.entries.find { (_, game) -> 
            game.player1.name == playerName || game.player2?.name == playerName 
        }
        
        if (gameEntry != null) {
            val (gameId, game) = gameEntry
            
            // Notify the remaining player about the opponent leaving
            if (playerName == game.player1.name && game.player2 != null) {
                // Player 1 disconnected, notify player 2
                val disconnectMessage = DisconnectMessage(
                    gameId = gameId,
                    playerName = playerName,
                    reason = "Player disconnected"
                )
                sendMessage(game.player2!!.connection, disconnectMessage)
            } else if (game.player2 != null && playerName == game.player2!!.name) {
                // Player 2 disconnected, notify player 1
                val disconnectMessage = DisconnectMessage(
                    gameId = gameId,
                    playerName = playerName,
                    reason = "Player disconnected"
                )
                sendMessage(game.player1.connection, disconnectMessage)
            }
            
            // Remove the game
            games.remove(gameId)
            
            Log.d(tag, "Game $gameId removed due to player $playerName disconnection")
            
            coroutineScope.launch {
                _serverEvents.emit(ServerEvent.PlayerDisconnected(gameId, playerName))
            }
        }
        
        // Remove the player
        players.remove(conn)
        
        coroutineScope.launch {
            _serverEvents.emit(ServerEvent.ClientDisconnected(address, playerName))
        }
    }
    
    /**
     * When a message is received from a client
     */
    override fun onMessage(conn: WebSocket, message: String) {
        val address = conn.remoteSocketAddress.address.hostAddress
        Log.d(tag, "Received message from $address: $message")
        
        val webSocketMessage = WebSocketMessage.fromJson(message)
        if (webSocketMessage == null) {
            Log.e(tag, "Invalid message format: $message")
            return
        }
        
        handleClientMessage(conn, webSocketMessage)
    }
    
    /**
     * Handle message from client
     */
    private fun handleClientMessage(conn: WebSocket, message: WebSocketMessage) {
        when (message) {
            is ConnectMessage -> handleConnectMessage(conn, message)
            is MoveMessage -> handleMoveMessage(conn, message)
            is ChatMessage -> handleChatMessage(conn, message)
            is DisconnectMessage -> handleDisconnectMessage(conn, message)
            is GameStateMessage -> {
                // Client shouldn't send GameState messages
                Log.w(tag, "Received unexpected GameState message from client")
            }
        }
    }
    
    /**
     * Handle connect message
     */
    private fun handleConnectMessage(conn: WebSocket, message: ConnectMessage) {
        val playerName = message.playerName
        
        // Save playerName
        players[conn] = playerName
        
        // Check if the request specifies a gameId
        val requestedGameId = message.requestGameId
        
        if (requestedGameId != null) {
            // Player wants to create a new game
            createNewGame(conn, requestedGameId, playerName)
        } else {
            // Player wants to join an existing game
            joinExistingGame(conn, playerName)
        }
    }
    
    /**
     * Create a new game
     */
    private fun createNewGame(conn: WebSocket, gameId: String, playerName: String) {
        // Create a new game room
        val gameRoom = GameRoom(
            player1 = Player(name = playerName, connection = conn),
            board = Array(3) { Array(3) { "" } }
        )
        
        // Save the game room
        games[gameId] = gameRoom
        
        // Send game state to the player
        val gameStateMessage = GameStateMessage(
            gameId = gameId,
            board = gameRoom.board.map { it.toList() },
            currentPlayer = playerName, // Player 1 goes first
            player1 = playerName,
            player2 = "", // No second player yet
            gameStatus = GameStatus.WAITING_OPPONENT.name
        )
        
        sendMessage(conn, gameStateMessage)
        
        Log.d(tag, "Created new game: $gameId with player: $playerName")
        coroutineScope.launch {
            _serverEvents.emit(ServerEvent.GameCreated(gameId, playerName))
        }
    }
    
    /**
     * Join an existing game
     */
    private fun joinExistingGame(conn: WebSocket, playerName: String) {
        // Find a game room waiting for a second player
        val gameEntry = games.entries.find { (_, game) -> game.player2 == null }
        
        if (gameEntry != null) {
            val (gameId, gameRoom) = gameEntry
            
            // Add the second player
            gameRoom.player2 = Player(name = playerName, connection = conn)
            
            // Update game status
            gameRoom.status = GameStatus.PLAYING
            
            // Send game state to both players
            val gameStateMessage = GameStateMessage(
                gameId = gameId,
                board = gameRoom.board.map { it.toList() },
                currentPlayer = gameRoom.player1.name, // Player 1 goes first
                player1 = gameRoom.player1.name,
                player2 = playerName,
                gameStatus = GameStatus.PLAYING.name
            )
            
            sendMessage(gameRoom.player1.connection, gameStateMessage)
            sendMessage(conn, gameStateMessage)
            
            Log.d(tag, "Player $playerName joined game: $gameId")
            coroutineScope.launch {
                _serverEvents.emit(ServerEvent.PlayerJoined(gameId, playerName))
            }
        } else {
            // No game waiting, create a new game
            val gameId = UUID.randomUUID().toString()
            createNewGame(conn, gameId, playerName)
        }
    }
    
    /**
     * Handle move message
     */
    private fun handleMoveMessage(conn: WebSocket, message: MoveMessage) {
        val gameId = message.gameId
        val playerName = message.playerName
        val row = message.row
        val col = message.col
        
        // Check if the game exists
        val gameRoom = games[gameId]
        if (gameRoom == null) {
            Log.e(tag, "Game not found: $gameId")
            return
        }
        
        // Check if it's this player's turn
        if (gameRoom.currentPlayer != playerName) {
            Log.e(tag, "Not player's turn: $playerName")
            return
        }
        
        // Check if the cell is already occupied
        if (gameRoom.board[row][col].isNotEmpty()) {
            Log.e(tag, "Cell already occupied: $row, $col")
            return
        }
        
        // Mark the cell
        val symbol = if (playerName == gameRoom.player1.name) "X" else "O"
        gameRoom.board[row][col] = symbol
        
        // Check game state after move
        checkGameState(gameId, gameRoom)
    }
    
    /**
     * Check game state after each move
     */
    private fun checkGameState(gameId: String, gameRoom: GameRoom) {
        val board = gameRoom.board
        
        // Check horizontal rows
        for (i in 0..2) {
            if (board[i][0].isNotEmpty() && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                endGame(gameId, gameRoom, board[i][0])
                return
            }
        }
        
        // Check vertical columns
        for (i in 0..2) {
            if (board[0][i].isNotEmpty() && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                endGame(gameId, gameRoom, board[0][i])
                return
            }
        }
        
        // Check diagonals
        if (board[0][0].isNotEmpty() && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            endGame(gameId, gameRoom, board[0][0])
            return
        }
        
        if (board[0][2].isNotEmpty() && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            endGame(gameId, gameRoom, board[0][2])
            return
        }
        
        // Check for draw
        var isDraw = true
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) {
                    isDraw = false
                    break
                }
            }
            if (!isDraw) break
        }
        
        if (isDraw) {
            endGame(gameId, gameRoom, null)
            return
        }
        
        // Switch turns
        gameRoom.currentPlayer = if (gameRoom.currentPlayer == gameRoom.player1.name) {
            gameRoom.player2?.name ?: gameRoom.player1.name
        } else {
            gameRoom.player1.name
        }
        
        // Send new game state
        val gameStateMessage = GameStateMessage(
            gameId = gameId,
            board = gameRoom.board.map { it.toList() },
            currentPlayer = gameRoom.currentPlayer,
            player1 = gameRoom.player1.name,
            player2 = gameRoom.player2?.name ?: "",
            gameStatus = gameRoom.status.name
        )
        
        sendMessage(gameRoom.player1.connection, gameStateMessage)
        gameRoom.player2?.let { player2 ->
            sendMessage(player2.connection, gameStateMessage)
        }
    }
    
    /**
     * End the game
     */
    private fun endGame(gameId: String, gameRoom: GameRoom, winnerSymbol: String?) {
        // Update game status
        gameRoom.status = GameStatus.GAME_OVER
        
        // Determine the winner
        val winner: String?
        val isDraw: Boolean
        
        if (winnerSymbol == null) {
            // Draw
            winner = null
            isDraw = true
        } else {
            // Someone won
            winner = if (winnerSymbol == "X") gameRoom.player1.name else gameRoom.player2?.name
            isDraw = false
        }
        
        // Send new game state
        val gameStateMessage = GameStateMessage(
            gameId = gameId,
            board = gameRoom.board.map { it.toList() },
            currentPlayer = gameRoom.currentPlayer,
            player1 = gameRoom.player1.name,
            player2 = gameRoom.player2?.name ?: "",
            winner = winner,
            isDraw = isDraw,
            gameStatus = GameStatus.GAME_OVER.name
        )
        
        sendMessage(gameRoom.player1.connection, gameStateMessage)
        gameRoom.player2?.let { player2 ->
            sendMessage(player2.connection, gameStateMessage)
        }
        
        // Log game result
        val resultMessage = if (isDraw) "Game ended in a draw" else "Game won by $winner"
        Log.d(tag, "$resultMessage, gameId: $gameId")
        
        coroutineScope.launch {
            _serverEvents.emit(
                if (isDraw) ServerEvent.GameDraw(gameId)
                else ServerEvent.GameWon(gameId, winner ?: "Unknown")
            )
        }
    }
    
    /**
     * Handle chat message
     */
    private fun handleChatMessage(conn: WebSocket, message: ChatMessage) {
        val gameId = message.gameId
        val playerName = message.playerName
        
        // Check if the game exists
        val gameRoom = games[gameId]
        if (gameRoom == null) {
            Log.e(tag, "Game not found: $gameId")
            return
        }
        
        // Forward message to other player
        if (playerName == gameRoom.player1.name) {
            // Message from player 1, send to player 2
            gameRoom.player2?.let { player2 ->
                sendMessage(player2.connection, message)
            }
        } else if (playerName == gameRoom.player2?.name) {
            // Message from player 2, send to player 1
            sendMessage(gameRoom.player1.connection, message)
        } else {
            Log.e(tag, "Unknown player sent chat message: $playerName")
        }
    }
    
    /**
     * Handle disconnect message
     */
    private fun handleDisconnectMessage(conn: WebSocket, message: DisconnectMessage) {
        val gameId = message.gameId
        val playerName = message.playerName
        
        // Check if the game exists
        val gameRoom = games[gameId]
        if (gameRoom == null) {
            Log.e(tag, "Game not found: $gameId")
            return
        }
        
        // Notify the other player
        if (playerName == gameRoom.player1.name) {
            // Disconnect from player 1, notify player 2
            gameRoom.player2?.let { player2 ->
                sendMessage(player2.connection, message)
            }
        } else if (playerName == gameRoom.player2?.name) {
            // Disconnect from player 2, notify player 1
            sendMessage(gameRoom.player1.connection, message)
        } else {
            Log.e(tag, "Unknown player disconnected: $playerName")
        }
        
        // Remove the game
        games.remove(gameId)
        
        Log.d(tag, "Player $playerName disconnected from game: $gameId")
        coroutineScope.launch {
            _serverEvents.emit(ServerEvent.PlayerDisconnected(gameId, playerName))
        }
    }
    
    /**
     * Send message to a connection
     */
    private fun sendMessage(conn: WebSocket, message: WebSocketMessage) {
        if (conn.isOpen) {
            val json = WebSocketMessage.toJson(message)
            conn.send(json)
        }
    }
    
    /**
     * When an error occurs
     */
    override fun onError(conn: WebSocket?, ex: Exception) {
        val address = conn?.remoteSocketAddress?.address?.hostAddress ?: "Unknown"
        Log.e(tag, "Error for connection from $address", ex)
        
        coroutineScope.launch {
            _serverEvents.emit(ServerEvent.ServerError(ex.message ?: "Unknown error"))
        }
    }
    
    /**
     * When the server starts
     */
    override fun onStart() {
        Log.d(tag, "WebSocket server started on port ${address.port}")
        
        coroutineScope.launch {
            _serverEvents.emit(ServerEvent.ServerStarted(address.port))
        }
    }
}

/**
 * Class representing a game room
 */
data class GameRoom(
    val player1: Player,
    var player2: Player? = null,
    val board: Array<Array<String>>,
    var currentPlayer: String = player1.name,
    var status: GameStatus = GameStatus.WAITING_OPPONENT
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as GameRoom
        
        if (player1 != other.player1) return false
        if (player2 != other.player2) return false
        if (!board.contentDeepEquals(other.board)) return false
        if (currentPlayer != other.currentPlayer) return false
        if (status != other.status) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = player1.hashCode()
        result = 31 * result + (player2?.hashCode() ?: 0)
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }
}

/**
 * Class representing a player
 */
data class Player(
    val name: String,
    val connection: WebSocket
)

/**
 * Server events
 */
sealed class ServerEvent {
    data class ClientConnected(val address: String) : ServerEvent()
    data class ClientDisconnected(val address: String, val playerName: String?) : ServerEvent()
    data class GameCreated(val gameId: String, val hostName: String) : ServerEvent()
    data class PlayerJoined(val gameId: String, val playerName: String) : ServerEvent()
    data class PlayerDisconnected(val gameId: String, val playerName: String) : ServerEvent()
    data class GameWon(val gameId: String, val winner: String) : ServerEvent()
    data class GameDraw(val gameId: String) : ServerEvent()
    data class ServerError(val message: String) : ServerEvent()
    data class ServerStarted(val port: Int) : ServerEvent()
} 