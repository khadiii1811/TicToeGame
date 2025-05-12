package com.example.tictoe.model

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.tictoe.network.ConnectionEvent
import com.example.tictoe.network.TicToeWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.net.InetAddress
import java.net.ServerSocket
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Repository để quản lý game online
 */
class OnlineGameRepository(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "OnlineGameRepository"
        private const val SERVER_PORT = 8887
        private const val HOST_SERVER_RANGE = 10 // Tìm kiếm host trong phạm vi 10 địa chỉ IP
    }
    
    // Web socket client
    private var webSocketClient: TicToeWebSocketClient? = null
    
    // Game ID
    private var currentGameId: String? = null
    
    // Player name
    private var playerName: String = "Player"
    
    // State flow to track connection and game state
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()
    
    // Lưu trữ các địa chỉ IP được tìm thấy
    private val _discoveredHosts = MutableStateFlow<List<String>>(emptyList())
    val discoveredHosts: StateFlow<List<String>> = _discoveredHosts.asStateFlow()
    
    // Kiểm tra nếu đang tìm kiếm host
    private val isScanning = AtomicBoolean(false)
    
    /**
     * Khởi tạo repository
     */
    init {
        Log.d(TAG, "Initializing OnlineGameRepository")
    }
    
    /**
     * Tạo game mới làm server
     */
    fun hostGame(playerName: String) {
        this.playerName = playerName
        currentGameId = UUID.randomUUID().toString()
        
        val ip = getLocalIpAddress()
        if (ip == null) {
            _connectionState.value = ConnectionState.Error("Could not get local IP address")
            return
        }
        
        Log.d(TAG, "Hosting game on IP: $ip")
        _connectionState.value = ConnectionState.Hosting
        
        // Create and connect to the WebSocket server
        connectToServer(ip, SERVER_PORT)
    }
    
    /**
     * Tham gia một game với địa chỉ IP
     */
    fun joinGame(playerName: String, serverIp: String) {
        this.playerName = playerName
        currentGameId = null
        
        Log.d(TAG, "Joining game on server: $serverIp")
        _connectionState.value = ConnectionState.Connecting
        
        // Connect to the WebSocket server
        connectToServer(serverIp, SERVER_PORT)
    }
    
    /**
     * Tìm kiếm các máy chủ trong mạng LAN
     */
    fun discoverHosts() {
        if (isScanning.getAndSet(true)) {
            Log.d(TAG, "Already scanning for hosts")
            return
        }
        
        _discoveredHosts.value = emptyList()
        
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val baseIp = getLocalIpPrefix()
                if (baseIp == null) {
                    _connectionState.value = ConnectionState.Error("Could not get local IP address")
                    isScanning.set(false)
                    return@launch
                }
                
                Log.d(TAG, "Starting LAN scan with base IP: $baseIp")
                
                val foundHosts = java.util.Collections.synchronizedList(mutableListOf<String>())
                val hostRange = 1..254
                val batchSize = 25 // Process hosts in batches
                val batches = hostRange.chunked(batchSize)
                
                // Create jobs for each batch of IPs
                val jobs = batches.map { batch ->
                    coroutineScope.launch(Dispatchers.IO) {
                        for (i in batch) {
                            val hostToCheck = "$baseIp$i"
                            
                            // Skip own IP
                            if (hostToCheck == getLocalIpAddress()) continue
                            
                            try {
                                if (isWebSocketServerRunning(hostToCheck, SERVER_PORT)) {
                                    Log.d(TAG, "Found WebSocket server at: $hostToCheck")
                                    synchronized(foundHosts) {
                                        foundHosts.add(hostToCheck)
                                        _discoveredHosts.value = foundHosts.toList()
                                    }
                                }
                            } catch (e: Exception) {
                                // Ignore errors for individual hosts
                            }
                        }
                    }
                }
                
                // Wait for all scans to complete
                jobs.forEach { it.join() }
                
                if (foundHosts.isEmpty()) {
                    Log.d(TAG, "No WebSocket servers found on LAN")
                } else {
                    Log.d(TAG, "Finished LAN scan, found ${foundHosts.size} servers")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scanning for hosts", e)
                _connectionState.value = ConnectionState.Error("Error scanning for hosts: ${e.message}")
            } finally {
                isScanning.set(false)
            }
        }
    }
    
    /**
     * Kiểm tra xem có WebSocket server đang chạy tại địa chỉ và cổng chỉ định
     */
    private fun isWebSocketServerRunning(host: String, port: Int): Boolean {
        return try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(host, port), 300) // 300ms timeout
            socket.close()
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * Lấy địa chỉ IP của thiết bị
     */
    private fun getLocalIpAddress(): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            
            // Convert little-endian to big-endian if needed
            val ipByteArray = ByteArray(4)
            ipByteArray[0] = (ipAddress and 0xff).toByte()
            ipByteArray[1] = (ipAddress shr 8 and 0xff).toByte()
            ipByteArray[2] = (ipAddress shr 16 and 0xff).toByte()
            ipByteArray[3] = (ipAddress shr 24 and 0xff).toByte()
            
            val ipAddr = InetAddress.getByAddress(ipByteArray)
            return ipAddr.hostAddress
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address", e)
            return null
        }
    }
    
    /**
     * Lấy prefix của địa chỉ IP mạng LAN (ví dụ: "192.168.1.")
     */
    private fun getLocalIpPrefix(): String? {
        val ip = getLocalIpAddress() ?: return null
        val lastDotIndex = ip.lastIndexOf('.')
        if (lastDotIndex <= 0) return null
        return ip.substring(0, lastDotIndex + 1)
    }
    
    /**
     * Kết nối đến WebSocket server
     */
    private fun connectToServer(serverIp: String, port: Int) {
        try {
            Log.d(TAG, "Attempting to connect to WebSocket server at $serverIp:$port")
            webSocketClient = TicToeWebSocketClient.createClient(serverIp, port, "", coroutineScope)
            
            // Collect connection events
            coroutineScope.launch {
                try {
                    webSocketClient?.connectionFlow?.collectLatest { event ->
                        handleConnectionEvent(event)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error collecting connection events: ${e.message}", e)
                    _connectionState.value = ConnectionState.Error("Connection error: ${e.message}")
                }
            }
            
            // Collect message events
            coroutineScope.launch {
                try {
                    webSocketClient?.messageFlow?.collectLatest { message ->
                        handleWebSocketMessage(message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error collecting message events: ${e.message}", e)
                }
            }
            
            // Add connection timeout
            coroutineScope.launch {
                try {
                    webSocketClient?.connectBlocking(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                    if (webSocketClient?.isOpen != true) {
                        Log.e(TAG, "Failed to connect to WebSocket server within timeout")
                        _connectionState.value = ConnectionState.Error("Connection timeout")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error connecting to WebSocket server: ${e.message}", e)
                    _connectionState.value = ConnectionState.Error("Connection error: ${e.message}")
                    webSocketClient?.close()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating WebSocket client: ${e.message}", e)
            _connectionState.value = ConnectionState.Error("Failed to connect: ${e.message}")
        }
    }
    
    /**
     * Xử lý các sự kiện kết nối
     */
    private fun handleConnectionEvent(event: ConnectionEvent) {
        when (event) {
            is ConnectionEvent.Connected -> {
                Log.d(TAG, "Connected to WebSocket server")
                
                // Send connect message with player name
                val connectMessage = ConnectMessage(
                    playerName = playerName,
                    requestGameId = currentGameId
                )
                webSocketClient?.sendMessage(connectMessage)
                
                _connectionState.value = ConnectionState.Connected
            }
            is ConnectionEvent.Disconnected -> {
                Log.d(TAG, "Disconnected from WebSocket server: ${event.code}, ${event.reason}")
                _connectionState.value = ConnectionState.Disconnected
                _gameState.value = null
            }
            is ConnectionEvent.Error -> {
                Log.e(TAG, "WebSocket error: ${event.message}")
                _connectionState.value = ConnectionState.Error(event.message)
            }
            is ConnectionEvent.Reconnecting -> {
                Log.d(TAG, "Reconnecting to WebSocket server (attempt ${event.attempt})")
                _connectionState.value = ConnectionState.Reconnecting(event.attempt)
            }
        }
    }
    
    /**
     * Xử lý các tin nhắn WebSocket
     */
    private fun handleWebSocketMessage(message: WebSocketMessage) {
        when (message) {
            is GameStateMessage -> {
                Log.d(TAG, "Received game state: ${message.gameStatus}")
                currentGameId = message.gameId
                
                // Update game state
                _gameState.value = GameState(
                    gameId = message.gameId,
                    board = message.board.map { row -> row.toTypedArray() }.toTypedArray(),
                    currentPlayer = message.currentPlayer,
                    player1 = message.player1, 
                    player2 = message.player2,
                    winner = message.winner,
                    isDraw = message.isDraw,
                    gameStatus = GameStatus.valueOf(message.gameStatus)
                )
            }
            is ConnectMessage -> {
                Log.d(TAG, "Player connected: ${message.playerName}")
            }
            is MoveMessage -> {
                Log.d(TAG, "Received move from ${message.playerName}: row=${message.row}, col=${message.col}")
            }
            is ChatMessage -> {
                Log.d(TAG, "Received chat from ${message.playerName}: ${message.message}")
            }
            is DisconnectMessage -> {
                Log.d(TAG, "Player disconnected: ${message.playerName}, reason: ${message.reason}")
            }
        }
    }
    
    /**
     * Gửi nước đi đến server
     */
    fun makeMove(row: Int, col: Int) {
        val gameId = currentGameId ?: return
        
        val moveMessage = MoveMessage(
            gameId = gameId,
            playerName = playerName,
            row = row,
            col = col
        )
        webSocketClient?.sendMessage(moveMessage)
    }
    
    /**
     * Gửi tin nhắn chat
     */
    fun sendChat(message: String) {
        val gameId = currentGameId ?: return
        
        val chatMessage = ChatMessage(
            gameId = gameId,
            playerName = playerName,
            message = message
        )
        webSocketClient?.sendMessage(chatMessage)
    }
    
    /**
     * Ngắt kết nối từ server
     */
    fun disconnect() {
        val gameId = currentGameId
        if (gameId != null) {
            val disconnectMessage = DisconnectMessage(
                gameId = gameId,
                playerName = playerName
            )
            webSocketClient?.sendMessage(disconnectMessage)
        }
        
        webSocketClient?.close()
        webSocketClient = null
        currentGameId = null
        _connectionState.value = ConnectionState.Disconnected
        _gameState.value = null
    }
    
    // Add a parameter to set the player name
    fun setPlayerName(name: String) {
        if (name.isNotEmpty()) {
            playerName = name
            Log.d(TAG, "Player name set to: $playerName")
        }
    }
}

/**
 * Trạng thái kết nối
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState() 
    object Hosting : ConnectionState()
    data class Error(val message: String) : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
}

/**
 * Trạng thái của game
 */
data class GameState(
    val gameId: String,
    val board: Array<Array<String>>,
    val currentPlayer: String,
    val player1: String,
    val player2: String,
    val winner: String? = null,
    val isDraw: Boolean = false,
    val gameStatus: GameStatus
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as GameState
        
        if (gameId != other.gameId) return false
        if (!board.contentDeepEquals(other.board)) return false
        if (currentPlayer != other.currentPlayer) return false
        if (player1 != other.player1) return false
        if (player2 != other.player2) return false
        if (winner != other.winner) return false
        if (isDraw != other.isDraw) return false
        if (gameStatus != other.gameStatus) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = gameId.hashCode()
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + player1.hashCode()
        result = 31 * result + player2.hashCode()
        result = 31 * result + (winner?.hashCode() ?: 0)
        result = 31 * result + isDraw.hashCode()
        result = 31 * result + gameStatus.hashCode()
        return result
    }
}

/**
 * Trạng thái của game
 */
enum class GameStatus {
    WAITING_OPPONENT,  // Đang chờ người chơi thứ hai
    PLAYING,           // Đang chơi
    GAME_OVER          // Game kết thúc
} 