package com.example.tictoe.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tictoe.LAN.*
import com.example.tictoe.model.SoundManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class LANViewModel : ViewModel() {
    // Room StateFlows
    private val _myRoom = MutableStateFlow<RoomInfo?>(null)
    val myRoom = _myRoom.asStateFlow()

    private val _availableRooms = MutableStateFlow<List<RoomInfo>>(emptyList())
    val availableRooms = _availableRooms.asStateFlow()

    // Before GameBoard StateFlows
    private val _isHosting = MutableStateFlow(false)
    val isHosting = _isHosting.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering = _isDiscovering.asStateFlow()

    private val _isJoining = MutableStateFlow(false)
    val isJoining = _isJoining.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _playerName = MutableStateFlow("")
    val playerName = _playerName.asStateFlow()

    private val _opponentName = MutableStateFlow<String?>(null)
    val opponentName = _opponentName.asStateFlow()

    private var nsdHelper: NsdHelper? = null
    private var gameServer: GameServer? = null
    private var gameClient: GameClient? = null

    private var soundManager: SoundManager? = null
    private var onMoveReceivedCallback: ((Int, Int) -> Unit)? = null

    var isPlayerTurn: Boolean = false
        private set

    var symbol: String = "X" // Default symbol for the player
        private set

    // Setters
    fun setSoundManager(manager: SoundManager) {
        soundManager = manager
    }

    fun setPlayerName(name: String) {
        _playerName.value = name
    }

    fun setOpponentName(name: String) {
        _opponentName.value = name
    }

    // Before GameBoard functions
    fun hostGame(context: Context, roomName: String) {
        val actualHost = "CaroRoom_${System.currentTimeMillis()}"
        val server = GameServer()
        gameServer = server
        try {
            server.startServer(
                    onServerStarted = { actualPort ->
                        if (actualPort != -1) {
                            val room =
                                    RoomInfo(
                                            roomName = roomName,
                                            hostName = _playerName.value,
                                            status = "available",
                                            host = actualHost,
                                            port = actualPort
                                    )
                            _myRoom.value = room
                            val nsd = NsdHelper(context = context, roomInfo = room)
                            nsd.registerService()
                            nsdHelper = nsd

                            _isHosting.value = true
                            isPlayerTurn = true // host plays first
                        } else {
                            Log.e("LANViewModel_Server", "Server started but port is invalid.")
                            stopHosting()
                        }
                    },
                    onMessage = { message ->
                        if (message is String) handleMessage(message, isHost = true)
                    },
                    onClientConnected = {
                        _myRoom.update { it?.copy(status = "playing") }
                        _isConnected.update { true }
                    }
            )
        } catch (e: Exception) {
            Log.e("LANViewModel_Server", "Error starting server: ${e.message}")
            stopHosting()
        }
    }

    fun stopHosting() {
        nsdHelper?.unregisterService()
        gameServer?.stop()
        nsdHelper = null
        gameServer = null
        _isHosting.value = false
    }

    fun startDiscovery(context: Context) {
        if (_isDiscovering.value) return
        _isDiscovering.value = true
        try {
            val nsd =
                    NsdHelper(
                            context = context,
                            onServiceFound = { room ->
                                _availableRooms.update { rooms ->
                                    if (rooms.none { it.host == room.host && it.port == room.port })
                                            rooms + room
                                    else rooms
                                }
                                Log.d(
                                        "LANViewModel_Client",
                                        "Service found: ${room.roomName} at ${room.host}:${room.port}"
                                )
                                _isDiscovering.value = false
                            }
                    )
            nsd.discoverServices()
            nsdHelper = nsd
        } catch (e: Exception) {
            Log.e("LANViewModel_Client", "Error starting discovery: ${e.message}")
            _isDiscovering.value = false
        }
    }

    fun stopDiscovery() {
        nsdHelper?.stopDiscovery()
        nsdHelper = null
        _availableRooms.value = emptyList()
        _isDiscovering.value = false
        Log.d("LANViewModel", "Discovery stopped")
    }

    fun joinRoom(room: RoomInfo) {
        _myRoom.value = room
        _isJoining.value = true
        val client = GameClient(host = room.host, port = room.port)
        gameClient = client
        try {
            client.connect(
                    playerName = _playerName.value,
                    onMessage = { message ->
                        if (message is String) handleMessage(message, isHost = false)
                    },
                    onConnected = {
                        // Handle successful connection (e.g., update status to "playing")
                        _isConnected.value = true
                        isPlayerTurn = false // guest plays second

                        // Optionally update the room status in your list
                        _availableRooms.update { rooms ->
                            rooms.map {
                                if (it.host == room.host && it.port == room.port)
                                        it.copy(status = "playing")
                                else it
                            }
                        }
                    }
            )
            gameClient = client
            _isConnected.value = true
            isPlayerTurn = false // guest plays second
        } catch (e: Exception) {
            Log.e("LANViewModel_Client", "Error connecting to room: ${e.message}")
            gameClient?.disconnect()
        }
    }

    fun disconnect() {
        gameClient?.disconnect()
        gameClient = null
        _isJoining.value = false
        _isConnected.value = false
        stopHosting()
    }

    // Handle received network message
    private fun handleMessage(message: String, isHost: Boolean) {
        when (val parsed = parseMessage(message)) {
            is Pair<*, *> -> {
                val row = parsed.first as? Int ?: return
                val col = parsed.second as? Int ?: return
                onMoveReceivedCallback?.invoke(row, col)
                isPlayerTurn = true
            }
            is String -> {
                // This is likely a player name
                _opponentName.value = parsed
                val nameMsg = "${MSG_PLAYER_NAME} ${_playerName.value}"
                if (isHost) {
                    gameServer?.send(nameMsg)
                } else {
                    gameClient?.send(nameMsg)
                }
                _isConnected.value = true
            }
            MSG_DISCONNECT -> {
                _isConnected.value = false
                disconnect()
            }
            MSG_REMATCH -> {
                // Handle other message types if needed
            }
            else -> {
                // Fallback for unrecognized messages
            }
        }
    }

    // In match functions
    // Provide a callback to call GameViewModel.makeMove() externally
    fun setOnMoveReceivedCallback(callback: (Int, Int) -> Unit) {
        onMoveReceivedCallback = callback
    }

    // Send a move to opponent
    fun sendMove(row: Int, col: Int) {
        val msg = "MOVE:$row,$col"
        if (_isHosting.value) {
            gameServer?.send(msg)
        } else {
            gameClient?.send(msg)
        }
        isPlayerTurn = false
    }

    // Called when player makes a local move
    fun onLocalMove(row: Int, col: Int) {
        if (isPlayerTurn) {
            sendMove(row, col)
            isPlayerTurn = false
        }
    }
}
