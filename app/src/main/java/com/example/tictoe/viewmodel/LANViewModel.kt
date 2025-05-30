package com.example.tictoe.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictoe.LAN.*
import com.example.tictoe.model.SoundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _isPlayerTurn = MutableStateFlow(false)
    val isPlayerTurn = _isPlayerTurn.asStateFlow()

    private var nsdHelper: NsdHelper? = null
    private var gameServer: GameServer? = null
    private var gameClient: GameClient? = null

    private var soundManager: SoundManager? = null
    private var onMoveReceivedCallback: ((Int, Int) -> Unit)? = null

    var symbol: String = "X" // Default symbol for the player
        private set

    private var onMoveReceived: ((row: Int, col: Int) -> Unit)? = null
    private var onTurnChanged: (() -> Unit)? = null

    private var gameViewModel: GameViewModel? = null

    fun setGameViewModel(vm: GameViewModel) {
        gameViewModel = vm
    }

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

    fun setIsPlayerTurn(isTurn: Boolean) {
        _isPlayerTurn.value = isTurn
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

                            symbol = "X"
                            _isHosting.value = true
                            _isPlayerTurn.value = true // host plays first
                        } else {
                            Log.e("LANViewModel_Server", "Server started but port is invalid.")
                            stopHosting()
                        }
                    },
                    onMessage = { message -> handleMessage(message, isHost = true) },
                    onClientConnected = {
                        viewModelScope.launch(Dispatchers.Main) {
                            _myRoom.update { it?.copy(status = "playing") }
                            _isConnected.value = true
                            // Gửi tên của mình cho client
                            gameServer?.send("${MSG_PLAYER_NAME} ${_playerName.value}")
                        }
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
                    onMessage = { message -> handleMessage(message, isHost = false) },
                    onConnected = {
                        viewModelScope.launch(Dispatchers.Main) {
                            symbol = "O"
                            _isConnected.value = true
                            _isPlayerTurn.value = false // guest plays second

                            // Gửi tên ngay khi kết nối thành công
                            val nameMsg = "${MSG_PLAYER_NAME} ${_playerName.value}"
                            Log.d("LANViewModel", "Client sending player name on connect: $nameMsg")
                            gameClient?.send(nameMsg)
                        }
                    }
            )
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
    private fun handleMessage(message: Any, isHost: Boolean) {
        Log.d("LANViewModel", "Handling message: $message, isHost=$isHost")
        viewModelScope.launch(Dispatchers.Main) {
            when (message) {
                is Pair<*, *> -> {
                    when (message.first) {
                        is Int -> {
                            // Đây là nước đi
                            val row = message.first as Int
                            val col = message.second as Int
                            Log.d("LANViewModel", "Received move: row=$row, col=$col")

                            // Xác định symbol của đối thủ
                            val opponentSymbol = if (symbol == "X") "O" else "X"
                            Log.d(
                                    "LANViewModel",
                                    "Making move with opponent symbol: $opponentSymbol"
                            )

                            // Cập nhật game state và UI
                            withContext(Dispatchers.Main) {
                                gameViewModel?.makeMoveLAN(row, col, opponentSymbol)
                                _isPlayerTurn.value = true
                                soundManager?.playClickSound()
                                Log.d(
                                        "LANViewModel",
                                        "Updated game state after receiving move. isPlayerTurn set to true"
                                )
                            }
                        }
                        MSG_PLAYER_NAME -> {
                            val playerName = message.second as String
                            val wasNull = _opponentName.value == null
                            Log.d("LANViewModel", "Setting opponent name to: $playerName")
                            _opponentName.value = playerName

                            // Only send back your name if you haven't received
                            // opponent's name before
                            if (wasNull) {
                                val nameMsg = "${MSG_PLAYER_NAME} ${_playerName.value}"
                                Log.d("LANViewModel", "Replying with my player name: $nameMsg")
                                if (isHost) {
                                    gameServer?.send(nameMsg)
                                } else {
                                    gameClient?.send(nameMsg)
                                }
                            }
                        }
                    }
                    _isConnected.value = true
                }
                MSG_DISCONNECT -> {
                    Log.d("LANViewModel", "Received disconnect message")
                    _isConnected.value = false
                    disconnect()
                }
                MSG_REMATCH -> {
                    Log.d("LANViewModel", "Received rematch message")
                    // Handle rematch message if needed
                }
                else -> {
                    Log.d("LANViewModel", "Received unknown message type: $message")
                }
            }
        }
    }

    // In match functions
    // Provide a callback to call GameViewModel.makeMove() externally
    fun setOnMoveReceivedCallback(
            onMove: (row: Int, col: Int) -> Unit,
            onTurnChange: () -> Unit // Add this parameter
    ) {
        onMoveReceived = onMove
        onTurnChanged = onTurnChange // Store the new callback
    }

    // When a move is actually received from the network
    private fun handleMoveReceived(row: Int, col: Int) {
        onMoveReceived?.invoke(row, col)
        onTurnChanged?.invoke() // Call the turn change callback
    }

    // Send a move to opponent
    fun sendMove(row: Int, col: Int) {
        viewModelScope.launch {
            val moveMsg = moveToMessage(Pair(row, col))
            Log.d("LANViewModel", "Sending move: $moveMsg, isHost=${_isHosting.value}")
            withContext(Dispatchers.IO) {
                if (_isHosting.value) {
                    gameServer?.send(moveMsg)
                } else {
                    gameClient?.send(moveMsg)
                }
            }
            _isPlayerTurn.value = false
            soundManager?.playClickSound()
            Log.d("LANViewModel", "Updated local state after sending move")
        }
    }

    // Called when player makes a local move
    fun onLocalMove(row: Int, col: Int) {
        Log.d(
                "LANViewModel",
                "onLocalMove: row=$row, col=$col, isPlayerTurn=${_isPlayerTurn.value}, symbol=$symbol"
        )
        if (_isPlayerTurn.value) {
            viewModelScope.launch(Dispatchers.Main) {
                // Update local board
                gameViewModel?.makeMoveLAN(row, col, symbol)
                Log.d("LANViewModel", "Local move made with symbol: $symbol")
                // Send move to opponent
                sendMove(row, col)
            }
        }
    }
}
