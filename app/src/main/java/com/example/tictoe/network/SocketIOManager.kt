package com.example.tictoe.network

import android.util.Log
import com.example.tictoe.model.WebSocketMessage
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Socket.IO manager for handling WebSocket connections
 */
class SocketIOManager(
    private val serverUrl: String,
    private val coroutineScope: CoroutineScope
) {
    private val tag = "SocketIOManager"
    private var socket: Socket? = null
    
    // Flow for received messages
    private val _messageFlow = MutableSharedFlow<WebSocketMessage>()
    val messageFlow: SharedFlow<WebSocketMessage> = _messageFlow
    
    // Flow for connection events
    private val _connectionFlow = MutableSharedFlow<ConnectionEvent>()
    val connectionFlow: SharedFlow<ConnectionEvent> = _connectionFlow
    
    // Event types
    private object Events {
        const val CONNECT = "connect"
        const val DISCONNECT = "disconnect"
        const val CONNECT_ERROR = "connect_error"
        const val GAME_MESSAGE = "game_message"
    }
    
    init {
        initializeSocket()
    }
    
    /**
     * Initialize Socket.IO connection
     */
    private fun initializeSocket() {
        try {
            val options = IO.Options()
            options.reconnection = true
            options.reconnectionAttempts = 5
            options.reconnectionDelay = 1000
            options.reconnectionDelayMax = 5000
            options.randomizationFactor = 0.5
            
            Log.d(tag, "Initializing Socket.IO with URL: $serverUrl")
            socket = IO.socket(serverUrl, options)
            
            // Set up event listeners
            socket?.on(Socket.EVENT_CONNECT, onConnect)
            socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socket?.on(Events.GAME_MESSAGE, onGameMessage)
            
            Log.d(tag, "Socket.IO client initialized")
        } catch (e: URISyntaxException) {
            Log.e(tag, "Error initializing Socket.IO client", e)
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Error("Invalid URI: ${e.message}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error initializing Socket.IO client", e)
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Error("Error: ${e.message}"))
            }
        }
    }
    
    /**
     * Connect to the server
     */
    fun connect() {
        Log.d(tag, "######## Attempting to connect to Socket.IO server at $serverUrl ########")
        try {
            // Thêm các listeners debug cho các sự kiện riêng lẻ thay vì onAny
            socket?.on("connect") { args ->
                Log.d(tag, "DEBUG: Socket.IO 'connect' event received")
            }
            socket?.on("disconnect") { args ->
                Log.d(tag, "DEBUG: Socket.IO 'disconnect' event with args: ${args?.joinToString()}")
            }
            socket?.on("connect_error") { args ->
                Log.d(tag, "DEBUG: Socket.IO 'connect_error' event with args: ${args?.joinToString()}")
            }
            socket?.on("game_message") { args ->
                Log.d(tag, "DEBUG: Socket.IO 'game_message' event received")
            }
            
            socket?.connect()
        } catch (e: Exception) {
            Log.e(tag, "Exception during connect: ${e.message}", e)
        }
    }
    
    /**
     * Disconnect from the server
     */
    fun disconnect() {
        socket?.disconnect()
    }
    
    /**
     * Send a message to the server
     */
    fun sendMessage(message: WebSocketMessage) {
        if (socket?.connected() != true) {
            Log.w(tag, "Attempted to send message while disconnected. Reconnecting...")
            socket?.connect()
            return
        }
        
        try {
            val json = WebSocketMessage.toJson(message)
            val jsonObject = JSONObject(json)
            Log.d(tag, "Sending message: $json")
            socket?.emit(Events.GAME_MESSAGE, jsonObject)
        } catch (e: Exception) {
            Log.e(tag, "Error sending message", e)
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Error("Failed to send message: ${e.message}"))
            }
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        socket?.off()
        socket?.disconnect()
        socket = null
    }
    
    /**
     * On connect listener
     */
    private val onConnect = Emitter.Listener {
        Log.d(tag, "######## Socket.IO CONNECTED SUCCESSFULLY to $serverUrl ########")
        coroutineScope.launch {
            _connectionFlow.emit(ConnectionEvent.Connected)
        }
    }
    
    /**
     * On disconnect listener
     */
    private val onDisconnect = Emitter.Listener { args ->
        val reason = if (args.isNotEmpty() && args[0] is String) args[0] as String else "Unknown reason"
        Log.d(tag, "######## Socket.IO DISCONNECTED from $serverUrl: $reason ########")
        coroutineScope.launch {
            _connectionFlow.emit(ConnectionEvent.Disconnected(0, reason))
        }
    }
    
    /**
     * On connect error listener
     */
    private val onConnectError = Emitter.Listener { args ->
        val error = if (args.isNotEmpty() && args[0] is Exception) {
            (args[0] as Exception).message ?: "Unknown error"
        } else if (args.isNotEmpty()) {
            args[0].toString()
        } else {
            "Unknown error"
        }
        Log.e(tag, "######## Socket.IO CONNECTION ERROR to $serverUrl: $error ########")
        coroutineScope.launch {
            _connectionFlow.emit(ConnectionEvent.Error(error))
        }
    }
    
    /**
     * On game message listener
     */
    private val onGameMessage = Emitter.Listener { args ->
        if (args.isEmpty() || args[0] !is JSONObject) {
            Log.e(tag, "Received invalid game message")
            return@Listener
        }
        
        try {
            val jsonObject = args[0] as JSONObject
            val json = jsonObject.toString()
            Log.d(tag, "Received message: $json")
            
            val message = WebSocketMessage.fromJson(json)
            if (message != null) {
                coroutineScope.launch {
                    _messageFlow.emit(message)
                }
            } else {
                Log.e(tag, "Failed to parse game message: $json")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error processing game message", e)
        }
    }
    
    companion object {
        /**
         * Create a new Socket.IO manager
         */
        fun create(host: String, port: Int, path: String = "", coroutineScope: CoroutineScope): SocketIOManager {
            val url = "http://$host:$port${if (path.isNotEmpty()) "/$path" else ""}"
            Log.d("SocketIOManager", "Creating new Socket.IO manager with URL: $url")
            return SocketIOManager(url, coroutineScope)
        }
    }
}

/**
 * WebSocket connection events
 */
sealed class ConnectionEvent {
    /**
     * Connected
     */
    object Connected : ConnectionEvent()
    
    /**
     * Disconnected
     */
    data class Disconnected(val code: Int, val reason: String) : ConnectionEvent()
    
    /**
     * Error
     */
    data class Error(val message: String) : ConnectionEvent()
    
    /**
     * Reconnecting
     */
    data class Reconnecting(val attempt: Int) : ConnectionEvent()
} 