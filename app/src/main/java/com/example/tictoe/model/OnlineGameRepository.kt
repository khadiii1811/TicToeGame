package com.example.tictoe.model

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.tictoe.network.ConnectionEvent
import com.example.tictoe.network.SocketIOManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.net.InetAddress
import java.net.Inet4Address
import java.net.NetworkInterface
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
        
        // Địa chỉ IP của máy chủ Socket.IO
        // 10.0.2.2 là địa chỉ đặc biệt trong máy ảo Android để truy cập đến máy host (localhost của máy tính)
        // Nếu bạn chạy trên thiết bị thật, hãy đổi thành IP thực của máy tính (ví dụ: 192.168.1.2)
        private const val SERVER_IP = "10.0.2.2" // Đặc biệt cho máy ảo Android
        
        // Lưu lại IP của thiết bị hiện tại
        var currentDeviceIp: String = "unknown"
            private set
    }
    
    // Socket.IO manager
    private var socketManager: SocketIOManager? = null
    

    
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
    
    // Timeout for matchmaking (in milliseconds)
    private val matchmakingTimeout = 60000L // Tăng lên 60 giây
    private var matchmakingTimeoutJob: kotlinx.coroutines.Job? = null
    
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
        
        // Sử dụng SERVER_IP cố định thay vì tìm IP
        val serverIp = SERVER_IP
        Log.d(TAG, "Using configured server IP: $serverIp")
        
        _connectionState.value = ConnectionState.Hosting
        
        try {
            // Kết nối đến Node.js server
            Log.d(TAG, "Connecting to Node.js server at $serverIp:$SERVER_PORT")
            connectToServer(serverIp, SERVER_PORT)
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to server: ${e.message}", e)
            _connectionState.value = ConnectionState.Error("Failed to connect to game server: ${e.message}")
        }
    }
    
    /**
     * Tham gia một game với địa chỉ IP
     */
    fun joinGame(playerName: String, serverIp: String = SERVER_IP) {
        this.playerName = playerName
        currentGameId = null
        
        Log.d(TAG, "Joining game on server: $serverIp")
        _connectionState.value = ConnectionState.Connecting
        
        // Kết nối đến server
        connectToServer(serverIp, SERVER_PORT)
    }
    
    /**
     * Tìm kiếm các máy chủ trong mạng LAN
     */
    fun discoverHosts() {
        // Thay vì quét mạng, kết nối trực tiếp đến server
        Log.d(TAG, "Skipping host scanning, connecting directly to fixed server: $SERVER_IP")
        
        // Thêm server cố định vào danh sách
        val hosts = listOf(SERVER_IP)
        _discoveredHosts.value = hosts
        
        // Kết nối trực tiếp đến server cố định
        try {
            Log.d(TAG, "Connecting directly to server at $SERVER_IP:$SERVER_PORT")
            connectToServer(SERVER_IP, SERVER_PORT)
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to server: ${e.message}", e)
            _connectionState.value = ConnectionState.Error("Failed to connect to game server: ${e.message}")
        }
    }
    
    /**
     * Kiểm tra xem có Socket.IO server đang chạy tại địa chỉ và cổng chỉ định
     */
    private fun isWebSocketServerRunning(host: String, port: Int): Boolean {
        return try {
            Log.d(TAG, "Checking Socket.IO server at: $host:$port")
            val socket = java.net.Socket()
            // Giảm timeout xuống để phản hồi nhanh hơn
            socket.connect(java.net.InetSocketAddress(host, port), 1000)
            
            // Kiểm tra xem kết nối có thực sự thành công
            val connected = socket.isConnected
            Log.d(TAG, "Socket connected: $connected")
            
            // Đóng socket đúng cách
            socket.close()
            
            if (connected) {
                Log.d(TAG, "Socket.IO server is running at: $host:$port")
                true
            } else {
                Log.d(TAG, "Unable to connect to Socket.IO server at: $host:$port")
                false
            }
        } catch (e: IOException) {
            Log.d(TAG, "No Socket.IO server at: $host:$port - ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Socket.IO server at: $host:$port", e)
            false
        }
    }
    
    /**
     * Lấy địa chỉ IP của thiết bị
     */
    private fun getLocalIpAddress(): String? {
        try {
            // Try WiFi first
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            
            if (ipAddress != 0) {
                // Convert little-endian to big-endian if needed
                val ipByteArray = ByteArray(4)
                ipByteArray[0] = (ipAddress and 0xff).toByte()
                ipByteArray[1] = (ipAddress shr 8 and 0xff).toByte()
                ipByteArray[2] = (ipAddress shr 16 and 0xff).toByte()
                ipByteArray[3] = (ipAddress shr 24 and 0xff).toByte()
                
                val ipAddr = InetAddress.getByAddress(ipByteArray)
                val hostAddress = ipAddr.hostAddress
                Log.d(TAG, "Found WiFi IP address: $hostAddress")
                
                // Cập nhật IP của thiết bị hiện tại
                currentDeviceIp = hostAddress
                
                return hostAddress
            }
            
            // If WiFi IP is not available, try all network interfaces
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        val hostAddress = inetAddress.hostAddress
                        Log.d(TAG, "Found IP address on interface ${networkInterface.name}: $hostAddress")
                        
                        // Cập nhật IP của thiết bị hiện tại
                        currentDeviceIp = hostAddress
                        
                        return hostAddress
                    }
                }
            }
            
            Log.e(TAG, "Could not find any suitable IP address")
            return null
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
     * Tạo kết nối đến máy chủ
     */
    private fun connectToServer(serverIp: String, port: Int) {
        Log.d(TAG, "########## Connecting to server: $serverIp:$port ##########")
        
        // Đóng kết nối hiện tại nếu có
        try {
            if (socketManager != null) {
                Log.d(TAG, "Closing existing connection")
                socketManager?.release()
                socketManager = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error closing existing connection: ${e.message}", e)
        }
        
        // Tạo kết nối mới
        try {
            Log.d(TAG, "########## Creating SocketIOManager for $serverIp:$port ##########")
            socketManager = SocketIOManager.create(serverIp, port, "", coroutineScope)
            
            // Đăng ký lắng nghe sự kiện kết nối
            coroutineScope.launch {
                socketManager?.connectionFlow?.collectLatest { event ->
                    Log.d(TAG, "########## Received connection event: $event ##########")
                    when (event) {
                        is ConnectionEvent.Connected -> {
                            Log.d(TAG, "########## CONNECTED to server $serverIp:$port ##########")
                            _connectionState.value = ConnectionState.Connected
                            
                            // Gửi tin nhắn kết nối với tên người chơi
                            sendConnectMessage()
                        }
                        
                        is ConnectionEvent.Disconnected -> {
                            Log.d(TAG, "########## DISCONNECTED from server: ${event.reason} ##########")
                            _connectionState.value = ConnectionState.Disconnected
                            _gameState.value = null
                        }
                        
                        is ConnectionEvent.Error -> {
                            Log.e(TAG, "########## CONNECTION ERROR: ${event.message} ##########")
                            _connectionState.value = ConnectionState.Error(event.message)
                        }
                        
                        is ConnectionEvent.Reconnecting -> {
                            Log.d(TAG, "########## RECONNECTING to server, attempt: ${event.attempt} ##########")
                            _connectionState.value = ConnectionState.Connecting
                        }
                    }
                }
            }
            
            // Đăng ký lắng nghe tin nhắn từ máy chủ
            coroutineScope.launch {
                socketManager?.messageFlow?.collectLatest { message ->
                    Log.d(TAG, "########## Received WebSocket message: ${message::class.java.simpleName} ##########")
                    handleWebSocketMessage(message)
                }
            }
            
            // Khởi tạo kết nối
            Log.d(TAG, "########## STARTING connection to $serverIp:$port ##########")
            socketManager?.connect()
            
            // Thiết lập timeout cho matchmaking
            startMatchmakingTimeoutJob()
        } catch (e: Exception) {
            Log.e(TAG, "########## ERROR connecting to server $serverIp:$port: ${e.message} ##########", e)
            _connectionState.value = ConnectionState.Error("Failed to connect: ${e.message}")
        }
    }
    
    /**
     * Đóng kết nối
     */
    fun disconnect() {
        matchmakingTimeoutJob?.cancel()
        socketManager?.disconnect()
        socketManager = null

        _connectionState.value = ConnectionState.Disconnected
        _gameState.value = null
    }
    
    /**
     * Gửi tin nhắn kết nối
     */
    private fun sendConnectMessage() {
        val connectMessage = ConnectMessage(
            playerName = playerName,
            requestGameId = currentGameId
        )
        
        socketManager?.sendMessage(connectMessage)
    }
    
    /**
     * Gửi nước đi
     */
    fun sendMove(row: Int, col: Int) {
        val gameId = currentGameId ?: return
        
        val moveMessage = MoveMessage(
            gameId = gameId,
            playerName = playerName,
            row = row,
            col = col
        )
        
        socketManager?.sendMessage(moveMessage)
    }
    
    /**
     * Gửi tin nhắn chat
     */
    fun sendChatMessage(message: String) {
        val gameId = currentGameId ?: return
        
        val chatMessage = ChatMessage(
            gameId = gameId,
            playerName = playerName,
            message = message
        )
        
        socketManager?.sendMessage(chatMessage)
    }
    
    /**
     * Start the matchmaking timeout, which will trigger an error if no match is found
     */
    private fun startMatchmakingTimeoutJob() {
        // Cancel any existing timeout job
        matchmakingTimeoutJob?.cancel()
        
        // Start a new timeout job
        matchmakingTimeoutJob = coroutineScope.launch {
            delay(matchmakingTimeout)
            
            // Check if we're still in WAITING_OPPONENT state
            val currentGameState = _gameState.value
            if (currentGameState == null || currentGameState.gameStatus == GameStatus.WAITING_OPPONENT) {
                Log.d(TAG, "Matchmaking timeout reached")
                _connectionState.value = ConnectionState.Error("No opponent found after waiting")
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
                val newGameState = GameState(
                    gameId = message.gameId,
                    board = message.board.map { row -> row.toTypedArray() }.toTypedArray(),
                    currentPlayer = message.currentPlayer,
                    player1 = message.player1, 
                    player2 = message.player2,
                    winner = message.winner,
                    isDraw = message.isDraw,
                    gameStatus = GameStatus.valueOf(message.gameStatus)
                )
                _gameState.value = newGameState
                
                // If the gameStatus is now PLAYING, cancel the matchmaking timeout
                if (newGameState.gameStatus == GameStatus.PLAYING) {
                    matchmakingTimeoutJob?.cancel()
                }
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
    
    // Add a parameter to set the player name
    fun setPlayerName(name: String) {
        if (name.isNotEmpty()) {
            playerName = name
            Log.d(TAG, "Player name set to: $playerName")
        }
    }
    
    /**
     * Lấy địa chỉ IP hiện tại để hiển thị
     */
    fun getCurrentIpAddress(): String {
        return getLocalIpAddress() ?: "IP not available"
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