package com.example.tictoe.network

import android.util.Log
import com.example.tictoe.model.WebSocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * WebSocket client for Tic-tac-toe game
 */
class TicTacToeClient(
    private val serverUrl: String,
    private val coroutineScope: CoroutineScope
) {
    private val tag = "TicTacToeClient"
    
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)  // Reduced from 30
        .readTimeout(20, TimeUnit.SECONDS)   // Reduced from 30
        .writeTimeout(20, TimeUnit.SECONDS)  // Reduced from 30
        .connectTimeout(5, TimeUnit.SECONDS) // Reduced from 10 to fail faster
        .build()
    
    // Flow for received messages
    private val _messageFlow = MutableSharedFlow<WebSocketMessage>()
    val messageFlow: SharedFlow<WebSocketMessage> = _messageFlow
    
    // Flow for connection events
    private val _connectionFlow = MutableSharedFlow<ConnectionEvent>()
    val connectionFlow: SharedFlow<ConnectionEvent> = _connectionFlow
    
    // Connection state tracking
    private val isConnecting = AtomicBoolean(false)
    private var reconnectJob: kotlinx.coroutines.Job? = null
    private var connectionAttempts = 0
    private val maxReconnectAttempts = 3
    
    init {
        Log.d(tag, "Initializing WebSocket client for $serverUrl")
    }
    
    /**
     * Connect to server
     */
    fun connect() {
        // If already connecting, don't start another connection attempt
        if (isConnecting.getAndSet(true)) {
            Log.d(tag, "Connection attempt already in progress")
            return
        }
        
        try {
            Log.d(tag, "Connecting to WebSocket server at $serverUrl")
            
            val request = Request.Builder()
                .url(serverUrl)
                .build()
            
            // Cancel any previous connection
            webSocket?.cancel()
            webSocket = null
            
            // Start new connection
            webSocket = client.newWebSocket(request, webSocketListener)
            
            // Set timeout for connection attempt
            startConnectionTimeout()
        } catch (e: Exception) {
            Log.e(tag, "Error connecting to WebSocket server", e)
            isConnecting.set(false)
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Error("Error connecting: ${e.message}"))
            }
        }
    }
    
    /**
     * Set a timeout for the connection attempt
     */
    private fun startConnectionTimeout() {
        coroutineScope.launch {
            delay(6000) // 6 seconds timeout
            
            // If still connecting after timeout, consider it failed
            if (isConnecting.get()) {
                Log.d(tag, "Connection attempt timed out")
                webSocket?.cancel()
                webSocket = null
                isConnecting.set(false)
                
                _connectionFlow.emit(ConnectionEvent.Error("Connection timeout"))
                
                // Try to reconnect if we haven't exceeded max attempts
                if (connectionAttempts < maxReconnectAttempts) {
                    val backoffDelay = calculateBackoffDelay(connectionAttempts)
                    connectionAttempts++
                    Log.d(tag, "Will retry connection (attempt $connectionAttempts) after $backoffDelay ms")
                    
                    delay(backoffDelay)
                    connect()
                }
            }
        }
    }
    
    /**
     * Calculate exponential backoff delay
     */
    private fun calculateBackoffDelay(attempt: Int): Long {
        return Math.min(1000 * Math.pow(2.0, attempt.toDouble()).toLong(), 10000)
    }
    
    /**
     * Disconnect from server
     */
    fun disconnect() {
        Log.d(tag, "Disconnecting from WebSocket server")
        reconnectJob?.cancel()
        reconnectJob = null
        connectionAttempts = 0
        isConnecting.set(false)
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
    
    /**
     * Send message to server
     */
    fun sendMessage(message: WebSocketMessage) {
        if (webSocket == null) {
            Log.w(tag, "Attempted to send message while disconnected. Reconnecting...")
            connect()
            return
        }
        
        try {
            val json = WebSocketMessage.toJson(message)
            Log.d(tag, "Sending message: $json")
            webSocket?.send(json)
        } catch (e: Exception) {
            Log.e(tag, "Error sending message", e)
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Error("Failed to send message: ${e.message}"))
            }
        }
    }
    
    /**
     * WebSocket listener implementation
     */
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(tag, "WebSocket connection opened")
            isConnecting.set(false)
            connectionAttempts = 0
            
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Connected)
            }
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(tag, "WebSocket message received: $text")
            try {
                val message = parseMessage(text)
                if (message != null) {
                    coroutineScope.launch {
                        _messageFlow.emit(message)
                    }
                } else {
                    Log.e(tag, "Failed to parse WebSocket message: $text")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error processing WebSocket message", e)
            }
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(tag, "WebSocket closing: $code, $reason")
            webSocket.close(1000, null)
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(tag, "WebSocket closed: $code, $reason")
            isConnecting.set(false)
            
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Disconnected(code, reason))
            }
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(tag, "WebSocket failure: ${t.message}", t)
            isConnecting.set(false)
            
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Error(t.message ?: "Unknown error"))
                
                // Try to reconnect if we haven't exceeded max attempts
                if (connectionAttempts < maxReconnectAttempts) {
                    val backoffDelay = calculateBackoffDelay(connectionAttempts)
                    connectionAttempts++
                    Log.d(tag, "Will retry connection (attempt $connectionAttempts) after $backoffDelay ms")
                    
                    reconnectJob = launch {
                        delay(backoffDelay)
                        connect()
                    }
                }
            }
        }
    }
    
    /**
     * Parse WebSocket message
     */
    private fun parseMessage(json: String): WebSocketMessage? {
        try {
            // Use existing WebSocketMessage parser
            return WebSocketMessage.fromJson(json)
        } catch (e: Exception) {
            Log.e(tag, "Error parsing message", e)
            return null
        }
    }
    
    /**
     * Create a new TicTacToeClient
     */
    companion object {
        fun create(host: String, port: Int, coroutineScope: CoroutineScope): TicTacToeClient {
            if (host.isEmpty()) {
                throw IllegalArgumentException("Host cannot be empty")
            }
            
            if (port <= 0 || port > 65535) {
                throw IllegalArgumentException("Port must be between 1 and 65535")
            }
            
            val url = "ws://$host:$port"
            Log.d("TicTacToeClient", "Creating new WebSocket client with URL: $url")
            return TicTacToeClient(url, coroutineScope)
        }
    }
} 