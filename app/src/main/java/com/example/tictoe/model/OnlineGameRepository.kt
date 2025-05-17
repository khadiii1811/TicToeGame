package com.example.tictoe.model

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.tictoe.network.ConnectionEvent
import com.example.tictoe.network.TicTacToeClient
import com.example.tictoe.network.TicTacToeServer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Date
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
        private const val DEFAULT_SERVER_PORT = 8887
        private var SERVER_PORT = DEFAULT_SERVER_PORT
        private const val HOST_SERVER_RANGE = 10 // Tìm kiếm host trong phạm vi 10 địa chỉ IP
        
        // Địa chỉ IP máy chủ mặc định
        // 10.0.2.2 là địa chỉ đặc biệt trong máy ảo Android để truy cập đến máy host
        // Không cần sử dụng giá trị này nữa vì chúng ta dùng WebSocket server trực tiếp trên thiết bị
        
        // Lưu lại IP của thiết bị hiện tại
        var currentDeviceIp: String = "unknown"
            private set
        
        // Manual IP address for hosting - can be set by user
        var manualHostIp: String? = null
    }
    
    // WebSocket server (khi làm host)
    private var wsServer: TicTacToeServer? = null
    
    // WebSocket client
    private var wsClient: TicTacToeClient? = null
    
    // Game ID
    private var currentGameId: String? = null
    
    // Room ID
    private var currentRoomId: String? = null
    
    // Player name
    private var playerName: String = "Player"
    
    // Json parser
    private val gson = Gson()
    
    // State flow to track connection and game state
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()
    
    // Room states
    private val _availableRooms = MutableStateFlow<List<RoomInfo>>(emptyList())
    val availableRooms: StateFlow<List<RoomInfo>> = _availableRooms.asStateFlow()
    
    private val _currentRoom = MutableStateFlow<RoomInfo?>(null)
    val currentRoom: StateFlow<RoomInfo?> = _currentRoom.asStateFlow()
    
    // Lưu trữ các địa chỉ IP được tìm thấy
    private val _discoveredHosts = MutableStateFlow<List<String>>(emptyList())
    val discoveredHosts: StateFlow<List<String>> = _discoveredHosts.asStateFlow()
    
    // Kiểm tra nếu đang tìm kiếm host
    private val isScanning = AtomicBoolean(false)
    
    // Timeout for matchmaking (in milliseconds)
    private val matchmakingTimeout = 60000L // Tăng lên 60 giây
    private var matchmakingTimeoutJob: kotlinx.coroutines.Job? = null
    
    // Last room list update timestamp
    private var lastRoomsUpdate = 0L
    
    /**
     * Khởi tạo repository
     */
    init {
        Log.d(TAG, "Initializing OnlineGameRepository")
    }
    
    /**
     * Kiểm tra xem server đã được khởi động thành công trên cổng chỉ định chưa
     */
    private suspend fun checkServerRunning(port: Int): Boolean {
        // Đợi một khoảng thời gian ngắn để server có thể khởi động
        delay(500)
        
        return try {
            Log.d(TAG, "Kiểm tra server đã hoạt động ở cổng $port chưa")
            
            val socket = java.net.Socket()
            val socketAddress = InetSocketAddress("127.0.0.1", port)
            val timeout = 1000
            
            socket.connect(socketAddress, timeout)
            socket.close()
            
            Log.d(TAG, "Server đang chạy ở cổng $port")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Server không chạy ở cổng $port: ${e.message}")
            false
        }
    }
    
    /**
     * Tạo game mới làm server
     */
    fun hostGame(playerName: String) {
        this.playerName = playerName
        currentGameId = UUID.randomUUID().toString()
        
        // Đóng kết nối hiện tại nếu có
        cleanup()
        
        _connectionState.value = ConnectionState.Hosting
        
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Check if user has specified a manual IP address first
                val localIp = if (!manualHostIp.isNullOrBlank()) {
                    Log.d(TAG, "Using manually specified host IP: $manualHostIp")
                    manualHostIp!!
                } else {
                    // Otherwise get local IP address
                    val detectedIp = getLocalIpAddress() ?: "127.0.0.1"
                    Log.d(TAG, "Using auto-detected IP: $detectedIp")
                    detectedIp
                }
                
                currentDeviceIp = localIp
                
                // Tìm cổng trống đơn giản
                try {
                    val testSocket = ServerSocket(DEFAULT_SERVER_PORT)
                    testSocket.close()
                    SERVER_PORT = DEFAULT_SERVER_PORT
                    Log.d(TAG, "Sử dụng cổng mặc định: $SERVER_PORT")
                } catch (e: Exception) {
                    // Tìm cổng khác
                    for (port in 8888..8900) {
                        try {
                            val testSocket = ServerSocket(port)
                            testSocket.close()
                            SERVER_PORT = port
                            Log.d(TAG, "Sử dụng cổng thay thế: $SERVER_PORT")
                            break
                        } catch (e: Exception) {
                            // Tiếp tục thử cổng khác
                        }
                    }
                }
                
                // Khởi động WebSocket server
                Log.d(TAG, "Starting WebSocket server on port $SERVER_PORT")
                wsServer = TicTacToeServer(SERVER_PORT)
                wsServer?.start()
                
                // Đợi server khởi động (quan trọng!)
                delay(1000) // Giảm từ 2000ms xuống 1000ms
                
                // Kiểm tra xem server đã khởi động thành công chưa
                var serverStarted = checkServerRunning(SERVER_PORT)
                
                // Nếu server không khởi động được, thử lại một lần nữa
                if (!serverStarted) {
                    Log.w(TAG, "Server không khởi động được, thử lại...")
                    wsServer?.stop()
                    delay(1000)
                    
                    wsServer = TicTacToeServer(SERVER_PORT)
                    wsServer?.start()
                    delay(1000)
                    
                    serverStarted = checkServerRunning(SERVER_PORT)
                    if (!serverStarted) {
                        Log.e(TAG, "Không thể khởi động server sau nhiều lần thử")
                        _connectionState.value = ConnectionState.Error("Không thể khởi động server. Hãy kiểm tra mạng hoặc sử dụng thiết bị thật.")
                        return@launch
                    }
                }
                
                Log.d(TAG, "WebSocket server đã khởi động, kết nối với tư cách client")
                
                // Kết nối đến server - kết nối qua IP thủ công hoặc IP được phát hiện, KHÔNG dùng localhost
                connectToServerSimple(localIp, SERVER_PORT)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error hosting game: ${e.message}", e)
                _connectionState.value = ConnectionState.Error("Failed to host game: ${e.message}")
            }
        }
    }
    
    /**
     * Tham gia một game với địa chỉ IP
     */
    fun joinGame(playerName: String, serverIp: String) {
        this.playerName = playerName
        currentGameId = null
        
        // Đóng kết nối hiện tại nếu có
        cleanup()
        
        // Ưu tiên IP thủ công nếu có
        val targetIp = if (!manualHostIp.isNullOrBlank() && serverIp == currentDeviceIp) {
            Log.d(TAG, "Overriding auto-detected IP with manual IP: $manualHostIp")
            manualHostIp!!
        } else {
            serverIp
        }
        
        Log.d(TAG, "Joining game on server: $targetIp:$SERVER_PORT")
        _connectionState.value = ConnectionState.Connecting
        
        // Kết nối đến server
        connectToServerSimple(targetIp, SERVER_PORT)
    }
    
    /**
     * Create a new room
     */
    fun createRoom(roomName: String, maxPlayers: Int = 2) {
        Log.d(TAG, "Creating room with name: $roomName, maxPlayers: $maxPlayers...")
        
        coroutineScope.launch(Dispatchers.IO) {
            // Nếu chưa kết nối, thử host một game mới
            if (wsClient == null || _connectionState.value !is ConnectionState.Connected) {
                Log.d(TAG, "Chưa kết nối tới server. Thử tạo server mới...")
                
                // Nếu đã có IP thủ công, log để debug
                if (!manualHostIp.isNullOrBlank()) {
                    Log.d(TAG, "Using manual host IP for creating room: $manualHostIp")
                }
                
                hostGame(playerName)
                
                // Đợi kết nối thành công (tối đa 5 giây)
                var timeWaited = 0
                val checkInterval = 250 // Giảm từ 500ms xuống 250ms
                val maxWaitTime = 5000
                
                while (timeWaited < maxWaitTime) {
                    if (_connectionState.value is ConnectionState.Connected) {
                        Log.d(TAG, "Kết nối thành công sau $timeWaited ms")
                        break
                    }
                    delay(checkInterval.toLong())
                    timeWaited += checkInterval
                    Log.d(TAG, "Đang đợi kết nối, đã đợi $timeWaited ms")
                }
            }
            
            // Kiểm tra lại kết nối
            if (wsClient == null || _connectionState.value !is ConnectionState.Connected) {
                Log.e(TAG, "Không thể tạo phòng: không kết nối được đến server")
                _connectionState.value = ConnectionState.Error("Không thể tạo phòng: không kết nối được đến server") 
                return@launch
            }
            
            // Bây giờ đã kết nối, tạo phòng
            Log.d(TAG, "Đã kết nối, gửi yêu cầu tạo phòng")
            val createRoomMessage = CreateRoomMessage(
                roomName = roomName,
                hostName = playerName,
                maxPlayers = maxPlayers
            )
            
            Log.d(TAG, "Sending create room message: roomName=$roomName, hostName=$playerName, maxPlayers=$maxPlayers")
            wsClient?.sendMessage(createRoomMessage)
            Log.d(TAG, "Sent create room request: $roomName")
            
            // Lên lịch refresh phòng sau khoảng thời gian ngắn
            delay(1000) // Đợi 1 giây để server có thời gian xử lý yêu cầu tạo phòng
            Log.d(TAG, "Auto-refreshing rooms after create room request")
            refreshRooms()
        }
    }
    
    /**
     * Request room list update
     */
    fun refreshRooms() {
        Log.d(TAG, "Refreshing room list...")
        
        coroutineScope.launch(Dispatchers.IO) {
            // Nếu chưa kết nối, thử kết nối với server mới
            if (wsClient == null || _connectionState.value !is ConnectionState.Connected) {
                Log.d(TAG, "Chưa kết nối tới server. Cố gắng kết nối...")
                
                // Trong trường hợp đã kết nối ít nhất một lần, sử dụng địa chỉ IP đã được lưu
                if (currentDeviceIp != "unknown") {
                    connectToServerSimple(currentDeviceIp, SERVER_PORT)
                } else {
                    val localIp = getLocalIpAddress() ?: "127.0.0.1"
                    connectToServerSimple(localIp, SERVER_PORT)
                }
                
                // Đợi kết nối thành công (tối đa 3 giây)
                var timeWaited = 0
                val checkInterval = 250 // Giảm từ 500ms xuống 250ms
                val maxWaitTime = 3000
                
                while (timeWaited < maxWaitTime) {
                    if (_connectionState.value is ConnectionState.Connected) {
                        break
                    }
                    delay(checkInterval.toLong())
                    timeWaited += checkInterval
                    Log.d(TAG, "Đang đợi kết nối, đã đợi $timeWaited ms")
                }
            }
            
            // Kiểm tra lại kết nối
            if (wsClient == null || _connectionState.value !is ConnectionState.Connected) {
                Log.e(TAG, "Không thể refresh room list: không kết nối được đến server")
                
                // Thử kết nối với localhost
                if (currentDeviceIp != "127.0.0.1" && currentDeviceIp != "localhost") {
                    Log.d(TAG, "Thử kết nối với localhost")
                    connectToServerSimple("127.0.0.1", SERVER_PORT)
                    
                    // Đợi kết nối thành công (tối đa 1 giây)
                    var timeWaited = 0
                    val checkInterval = 200
                    val maxWaitTime = 1000
                    
                    while (timeWaited < maxWaitTime) {
                        if (_connectionState.value is ConnectionState.Connected) {
                            break
                        }
                        delay(checkInterval.toLong())
                        timeWaited += checkInterval
                    }
                }
                
                // Nếu vẫn không kết nối được, trả về lỗi
                if (_connectionState.value !is ConnectionState.Connected) {
                    Log.e(TAG, "Vẫn không thể kết nối với server sau khi thử nhiều lần")
                    return@launch
                }
            }
            
            // Gửi yêu cầu danh sách phòng
            val connectionState = _connectionState.value
            Log.d(TAG, "Current connection state: $connectionState")
            
            val listRoomsMessage = ListRoomsMessage(lastUpdate = lastRoomsUpdate)
            Log.d(TAG, "Sending ListRoomsMessage with lastUpdate=$lastRoomsUpdate")
            wsClient?.sendMessage(listRoomsMessage)
            Log.d(TAG, "Requested room list update")
        }
    }
    
    /**
     * Join an existing room
     */
    fun joinRoom(roomId: String) {
        if (wsClient == null) {
            Log.e(TAG, "Cannot join room: not connected to server")
            return
        }
        
        val joinRoomMessage = JoinRoomMessage(
            roomId = roomId,
            playerName = playerName
        )
        
        wsClient?.sendMessage(joinRoomMessage)
        Log.d(TAG, "Sent join room request: $roomId")
    }
    
    /**
     * Tìm kiếm các máy chủ trong mạng LAN
     */
    fun discoverHosts() {
        if (isScanning.getAndSet(true)) {
            Log.d(TAG, "Host discovery already in progress")
            return
        }
        
        // Clear discovered hosts
        _discoveredHosts.value = emptyList()
        
        // Run in background
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val currentIp = getLocalIpAddress()
                if (currentIp == null) {
                    Log.e(TAG, "Could not determine local IP address")
                    isScanning.set(false)
                    return@launch
                }
                
                val foundHosts = mutableListOf<String>()
                
                // Kiểm tra xem đang chạy trên máy ảo hay thiết bị thật
                val isEmulator = currentIp.startsWith("10.0.2.")
                Log.d(TAG, "Detected environment: ${if (isEmulator) "Emulator" else "Real device"} with IP $currentIp")
                
                // Add our own IP as a possible server (self-hosting)
                foundHosts.add(currentIp)
                _discoveredHosts.value = foundHosts.toList()
                
                // Xử lý đặc biệt cho môi trường máy ảo
                if (isEmulator) {
                    // Thêm các địa chỉ đặc biệt cho máy ảo
                    foundHosts.add("127.0.0.1") // localhost
                    foundHosts.add("10.0.2.2")  // host machine từ emulator
                    _discoveredHosts.value = foundHosts.toList()
                    
                    Log.d(TAG, "Running on emulator, skipping regular network scan")
                    isScanning.set(false)
                    return@launch
                }
                
                // Trên thiết bị thật, quét mạng bình thường
                // Extract network prefix (e.g., "192.168.1.")
                val ipPrefix = getLocalIpPrefix()
                if (ipPrefix == null) {
                    Log.e(TAG, "Could not determine IP prefix")
                    isScanning.set(false)
                    return@launch
                }
                
                Log.d(TAG, "Scanning for hosts on network: $ipPrefix")
                
                // Scan a range of IP addresses to find other servers
                // Lấy phạm vi quét từ IP prefix (192.168.x.y -> quét từ .1 đến .254)
                // Tránh scan quá nhiều địa chỉ không cần thiết
                for (i in 1..254) {
                    // Chỉ scan quét 20 địa chỉ IP quanh IP hiện tại để tiết kiệm thời gian
                    val lastOctet = getLastOctet(currentIp)
                    if (lastOctet != -1) {
                        val startRange = Math.max(1, lastOctet - 10)
                        val endRange = Math.min(254, lastOctet + 10)
                        if (i < startRange || i > endRange) {
                            continue  // Bỏ qua các địa chỉ nằm ngoài range
                        }
                    }
                    
                    val targetIp = "$ipPrefix$i"
                    
                    // Skip own IP
                    if (targetIp == currentIp) continue
                    
                    Log.d(TAG, "Checking $targetIp:$SERVER_PORT")
                    
                    if (isWebSocketServerRunning(targetIp, SERVER_PORT)) {
                        Log.d(TAG, "Found WebSocket server at: $targetIp:$SERVER_PORT")
                        foundHosts.add(targetIp)
                        _discoveredHosts.value = foundHosts.toList()
                    }
                    
                    // Check if scanning was cancelled
                    if (!isScanning.get()) break
                }
                
                Log.d(TAG, "Host discovery completed, found ${foundHosts.size} servers")
            } catch (e: Exception) {
                Log.e(TAG, "Error during host discovery", e)
            } finally {
                isScanning.set(false)
            }
        }
    }
    
    /**
     * Lấy octet cuối cùng của địa chỉ IP
     */
    private fun getLastOctet(ipAddress: String): Int {
        return try {
            val parts = ipAddress.split(".")
            if (parts.size == 4) {
                parts[3].toInt()
            } else {
                -1
            }
        } catch (e: Exception) {
            -1
        }
    }
    
    /**
     * Kiểm tra xem có WebSocket server đang chạy tại địa chỉ và cổng chỉ định
     */
    private fun isWebSocketServerRunning(host: String, port: Int): Boolean {
        return try {
            // Đối với các địa chỉ 10.0.2.x (trừ 10.0.2.2) trong môi trường máy ảo, 
            // bỏ qua kiểm tra để tránh timeout không cần thiết
            if (host.startsWith("10.0.2.") && host != "10.0.2.2" && host != currentDeviceIp) {
                Log.d(TAG, "Skipping check for emulator address: $host")
                return false
            }
            
            val socket = ServerSocket(0)
            val testPort = socket.localPort
            socket.close()
            
            val testClient = TicTacToeClient.create(host, port, coroutineScope)
            var isConnected = false
            
            // Try to connect with shorter timeout for faster discovery
            val connectJob = coroutineScope.launch {
                testClient.connectionFlow.collectLatest { event ->
                    when (event) {
                        is ConnectionEvent.Connected -> isConnected = true
                        is ConnectionEvent.Error -> {
                            isConnected = false
                            Log.d(TAG, "Test connection failed: ${event.message}")
                        }
                        is ConnectionEvent.Disconnected -> {
                            isConnected = false
                            Log.d(TAG, "Test connection disconnected: ${event.reason}")
                        }
                        is ConnectionEvent.Reconnecting -> {
                            Log.d(TAG, "Test connection reconnecting (attempt: ${event.attempt})")
                        }
                    }
                }
            }
            
            testClient.connect()
            
            // Wait for a short time (reduced from 1000ms to 500ms)
            Thread.sleep(500)
            
            // Cleanup
            connectJob.cancel()
            testClient.disconnect()
            
            isConnected
        } catch (e: Exception) {
            Log.d(TAG, "Error testing connection to $host:$port: ${e.message}")
            false
        }
    }
    
    /**
     * Ngắt kết nối
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting from game")
        cleanup()
        _connectionState.value = ConnectionState.Disconnected
    }
    
    /**
     * Lấy địa chỉ IP của thiết bị
     */
    private fun getLocalIpAddress(): String? {
        try {
            // Ưu tiên tìm địa chỉ WiFi bằng cách duyệt qua tất cả network interfaces
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            
            // Danh sách các interfaces cần ưu tiên 
            val wifiInterfaces = listOf("wlan0", "eth0", "en0", "Wi-Fi") // Android, iOS và Windows
            
            // Danh sách các interfaces cần bỏ qua (thường là máy ảo hoặc adapter ảo)
            val skipInterfaces = listOf("vboxnet", "vmnet", "virtualbox", "VirtualBox", "docker", "Ethernet 2")

            // Lưu địa chỉ wifi tìm được
            var wifiAddress: Pair<String, InetAddress>? = null
            
            // Lưu các địa chỉ khác làm backup
            val otherAddresses = mutableListOf<Pair<String, InetAddress>>()
            
            // Lưu tất cả địa chỉ 192.168.1.x (mạng LAN phổ biến) làm ưu tiên cao
            val lanAddresses = mutableListOf<Pair<String, InetAddress>>()

            Log.d(TAG, "Scanning network interfaces for IP address...")
            
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val interfaceName = networkInterface.name
                val addresses = networkInterface.inetAddresses
                
                // Skip các interface không hoạt động hoặc loopback
                if (!networkInterface.isUp || networkInterface.isLoopback) {
                    Log.d(TAG, "Skipping $interfaceName (down or loopback)")
                    continue
                }
                
                // Skip các virtual interface
                if (skipInterfaces.any { interfaceName.contains(it) }) {
                    Log.d(TAG, "Skipping $interfaceName (virtual interface)")
                    continue
                }
                
                Log.d(TAG, "Checking interface: $interfaceName")
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    // Bỏ qua địa chỉ loopback và non-ipv4
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        val addrStr = address.hostAddress
                        Log.d(TAG, "Found IP: $addrStr on interface $interfaceName")
                        
                        // Lưu địa chỉ 192.168.1.x làm ưu tiên cao
                        if (addrStr.startsWith("192.168.1.")) {
                            Log.d(TAG, "Found LAN IP: $addrStr on interface $interfaceName (high priority)")
                            lanAddresses.add(Pair(interfaceName, address))
                        }
                        
                        // Lưu lại cả địa chỉ 10.0.2.x (máy ảo Android) để xác định khi nào đang chạy trên máy ảo
                        if (wifiInterfaces.contains(interfaceName)) {
                            Log.d(TAG, "Found WiFi IP: $addrStr on interface $interfaceName")
                            wifiAddress = Pair(interfaceName, address)
                            // Tìm thấy địa chỉ WiFi, ưu tiên cao nhất
                            break
                        } else {
                            // Lưu lại các địa chỉ khác để dùng nếu không tìm thấy WiFi
                            otherAddresses.add(Pair(interfaceName, address))
                        }
                    }
                }
                
                // Nếu đã tìm thấy địa chỉ WiFi, không cần kiểm tra các interface khác
                if (wifiAddress != null) {
                    break
                }
            }

            // Ưu tiên theo thứ tự: WiFi > LAN > Other
            if (wifiAddress != null) {
                val addr = wifiAddress.second.hostAddress
                Log.d(TAG, "Using WiFi address: $addr on interface ${wifiAddress.first}")
                return addr
            }
            
            if (lanAddresses.isNotEmpty()) {
                val addr = lanAddresses.first().second.hostAddress
                Log.d(TAG, "Using LAN address: $addr on interface ${lanAddresses.first().first}")
                return addr
            }

            if (otherAddresses.isNotEmpty()) {
                val addr = otherAddresses.first().second.hostAddress
                Log.d(TAG, "Using other address: $addr on interface ${otherAddresses.first().first}")
                return addr
            }

            // Nếu không tìm được cách nào khác, sử dụng WifiManager (Android only)
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            
            if (ipAddress != 0) {
                val formattedIp = String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
                Log.d(TAG, "Using IP from WifiManager: $formattedIp")
                return formattedIp
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP address", e)
        }
        
        // Nếu tất cả thất bại, dùng localhost
        Log.w(TAG, "Cannot get IP address, using localhost")
        return "127.0.0.1"
    }
    
    /**
     * Lấy prefix của địa chỉ IP hiện tại (3 octet đầu tiên)
     */
    private fun getLocalIpPrefix(): String? {
        val ip = getLocalIpAddress() ?: return null
        val lastDotIndex = ip.lastIndexOf('.')
        if (lastDotIndex == -1) return null
        
        return ip.substring(0, lastDotIndex + 1)
    }
    
    /**
     * Kết nối đơn giản đến server không kiểm tra trước
     */
    private fun connectToServerSimple(host: String, port: Int) {
        try {
            Log.d(TAG, "Connecting directly to server at $host:$port")
            
            // Đóng client cũ nếu có
            wsClient?.disconnect()
            
            // Tạo client mới 
            wsClient = TicTacToeClient.create(host, port, coroutineScope)
            
            // Handle connection events
            coroutineScope.launch {
                wsClient?.connectionFlow?.collectLatest { event ->
                    when (event) {
                        is ConnectionEvent.Connected -> {
                            Log.d(TAG, "Connected to server successfully")
                            _connectionState.value = ConnectionState.Connected
                            
                            // Send connect message
                            sendConnectMessage()
                            
                            // Start matchmaking timeout
                            startMatchmakingTimeoutJob()
                        }
                        is ConnectionEvent.Disconnected -> {
                            Log.d(TAG, "Disconnected from server: ${event.reason}")
                            _connectionState.value = ConnectionState.Disconnected
                            currentGameId = null
                            _gameState.value = null
                            _currentRoom.value = null
                        }
                        is ConnectionEvent.Error -> {
                            Log.e(TAG, "Connection error: ${event.message}")
                            _connectionState.value = ConnectionState.Error(event.message)
                            
                            // Chỉ thử kết nối dự phòng nếu không dùng IP thủ công
                            if (manualHostIp.isNullOrBlank()) {
                                val isEmulator = currentDeviceIp.startsWith("10.0.2.")
                                
                                if (host != "127.0.0.1" && host != "localhost") {
                                    Log.d(TAG, "Trying fallback to localhost")
                                    coroutineScope.launch(Dispatchers.IO) {
                                        delay(500) // Đợi chút để tránh nhiều kết nối cùng lúc
                                        connectToServerSimple("127.0.0.1", port)
                                    }
                                } else if (isEmulator && host != "10.0.2.2" && host != "10.0.2.15") {
                                    // Trong môi trường máy ảo, thử kết nối với địa chỉ đặc biệt của máy chủ
                                    Log.d(TAG, "Trying fallback to emulator host (10.0.2.2)")
                                    coroutineScope.launch(Dispatchers.IO) {
                                        delay(500)
                                        connectToServerSimple("10.0.2.2", port)
                                    }
                                } else if (!isEmulator && currentDeviceIp.startsWith("192.168.") && host != currentDeviceIp) {
                                    // Trong môi trường thiết bị thật, thử kết nối với IP của thiết bị hiện tại
                                    Log.d(TAG, "Trying fallback to current device IP: $currentDeviceIp")
                                    coroutineScope.launch(Dispatchers.IO) {
                                        delay(500)
                                        connectToServerSimple(currentDeviceIp, port)
                                    }
                                }
                            } else {
                                Log.d(TAG, "Not trying fallback connections because manual IP is set: $manualHostIp")
                            }
                        }
                        is ConnectionEvent.Reconnecting -> {
                            Log.d(TAG, "Reconnecting to server (attempt: ${event.attempt})")
                            _connectionState.value = ConnectionState.Reconnecting(event.attempt)
                        }
                    }
                }
            }
            
            // Handle WebSocket messages
            coroutineScope.launch {
                wsClient?.messageFlow?.collectLatest { message ->
                    handleWebSocketMessage(message)
                }
            }
            
            // Connect
            wsClient?.connect()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to server: ${e.message}", e)
            _connectionState.value = ConnectionState.Error("Failed to connect: ${e.message}")
        }
    }
    
    /**
     * Cleanup resources
     */
    private fun cleanup() {
        matchmakingTimeoutJob?.cancel()
        matchmakingTimeoutJob = null
        
        wsClient?.disconnect()
        wsClient = null
        
        wsServer?.stop()
        wsServer = null
    }
    
    /**
     * Gửi tin nhắn kết nối
     */
    private fun sendConnectMessage() {
        val connectMessage = ConnectMessage(
            playerName = playerName,
            requestGameId = currentGameId
        )
        
        wsClient?.sendMessage(connectMessage)
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
        
        wsClient?.sendMessage(moveMessage)
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
        
        wsClient?.sendMessage(chatMessage)
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
        Log.d(TAG, "Received WebSocket message: ${message.type}")
        
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
            is RoomListMessage -> {
                Log.d(TAG, "Received room list with ${message.rooms.size} rooms")
                // Cập nhật danh sách phòng
                _availableRooms.value = message.rooms
                lastRoomsUpdate = message.timestamp
                
                // Ghi log chi tiết từng phòng để dễ debug
                message.rooms.forEachIndexed { index, room ->
                    Log.d(TAG, "Room $index: ID=${room.id}, Name=${room.name}, Host=${room.hostName}, Players=${room.playerCount}/${room.maxPlayers}")
                }
            }
            is CreateRoomMessage -> {
                Log.d(TAG, "Received create room message for room: ${message.roomName}")
                // Client normally doesn't receive this message type, server handles it
                // Thử refresh danh sách phòng sau khi tạo phòng
                refreshRooms()
            }
            is JoinRoomMessage -> {
                Log.d(TAG, "Received join room message for room: ${message.roomId}")
                // Client normally doesn't receive this message type, server handles it
            }
            is ListRoomsMessage -> {
                Log.d(TAG, "Received list rooms request with lastUpdate: ${message.lastUpdate}")
                // Client normally doesn't receive this message type, server handles it
            }
            is ServerStatusMessage -> {
                Log.d(TAG, "Received server status: active connections: ${message.activeConnections}, active games: ${message.activeGames}")
                // Handle server status update if needed
            }
            is RoomUpdateMessage -> {
                Log.d(TAG, "Received room update: ${message.event} for room ${message.room.name} (ID: ${message.room.id})")
                
                // Lưu ID phòng hiện tại nếu phòng này là phòng mình tạo
                if (message.event == "CREATED" && message.room.hostName == playerName) {
                    currentRoomId = message.room.id
                    _currentRoom.value = message.room
                    Log.d(TAG, "Set current room ID to ${message.room.id}")
                }
                
                // Handle room update
                when (message.event) {
                    "CREATED", "UPDATED", "PLAYER_JOINED", "PLAYER_LEFT" -> {
                        // Update available rooms list
                        val currentRooms = _availableRooms.value.toMutableList()
                        val roomIndex = currentRooms.indexOfFirst { it.id == message.room.id }
                        
                        if (roomIndex >= 0) {
                            // Update existing room
                            Log.d(TAG, "Updating existing room at index $roomIndex")
                            currentRooms[roomIndex] = message.room
                        } else {
                            // Add new room
                            Log.d(TAG, "Adding new room to list")
                            currentRooms.add(message.room)
                        }
                        
                        _availableRooms.value = currentRooms
                        
                        // Update current room if this is our room
                        if (currentRoomId == message.room.id) {
                            _currentRoom.value = message.room
                        }
                        
                        // Auto-request room list update to keep list fresh
                        refreshRooms()
                    }
                    "HOST_CHANGED" -> {
                        // Update available rooms list
                        val currentRooms = _availableRooms.value.toMutableList()
                        val roomIndex = currentRooms.indexOfFirst { it.id == message.room.id }
                        
                        if (roomIndex >= 0) {
                            // Update existing room
                            currentRooms[roomIndex] = message.room
                        }
                        
                        _availableRooms.value = currentRooms
                        
                        // Update current room if this is our room
                        if (currentRoomId == message.room.id) {
                            _currentRoom.value = message.room
                        }
                    }
                    "GAME_STARTED" -> {
                        // Update room status
                        val currentRooms = _availableRooms.value.toMutableList()
                        val roomIndex = currentRooms.indexOfFirst { it.id == message.room.id }
                        
                        if (roomIndex >= 0) {
                            // Update existing room
                            currentRooms[roomIndex] = message.room
                        }
                        
                        _availableRooms.value = currentRooms
                        
                        // Update current room if this is our room
                        if (currentRoomId == message.room.id) {
                            _currentRoom.value = message.room
                        }
                    }
                }
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
        // If manual IP is set, return it
        if (!manualHostIp.isNullOrBlank()) {
            return "$manualHostIp (Manual)"
        }
        
        // Otherwise return detected IP
        return getLocalIpAddress() ?: "IP not available"
    }
    
    /**
     * Tìm một cổng trống để sử dụng
     * @return Cổng trống hoặc -1 nếu không tìm thấy
     */
    private fun findAvailablePort(startPort: Int = DEFAULT_SERVER_PORT, range: Int = 10): Int {
        for (port in startPort until startPort + range) {
            try {
                val serverSocket = ServerSocket(port)
                serverSocket.close()
                Log.d(TAG, "Tìm thấy cổng trống: $port")
                return port
            } catch (e: Exception) {
                Log.d(TAG, "Cổng $port đã bị sử dụng, thử cổng khác")
            }
        }
        Log.e(TAG, "Không tìm thấy cổng trống nào trong khoảng từ $startPort đến ${startPort + range}")
        return -1
    }
    
    /**
     * Set a manual host IP address to use instead of auto-detection
     */
    fun setManualHostIp(ipAddress: String?) {
        // Validate IP format if provided
        if (!ipAddress.isNullOrBlank()) {
            // Simple validation to check if it's a valid IPv4 format
            val ipPattern = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
            if (!ipPattern.matches(ipAddress)) {
                Log.e(TAG, "Invalid IP address format: $ipAddress")
                return
            }
        }
        
        manualHostIp = ipAddress
        Log.d(TAG, "Manual host IP " + (if (ipAddress.isNullOrBlank()) "cleared" else "set to: $ipAddress"))
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