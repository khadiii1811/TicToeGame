package com.example.tictoe.network

import android.util.Log
import com.example.tictoe.model.WebSocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

/**
 * WebSocket client for Tic Tac Toe game
 */
class TicToeWebSocketClient(
    private val serverUri: URI,
    private val coroutineScope: CoroutineScope
) : WebSocketClient(serverUri) {
    private val tag = "TicToeWebSocketClient"
    
    // Flow for received messages
    private val _messageFlow = MutableSharedFlow<WebSocketMessage>()
    val messageFlow: SharedFlow<WebSocketMessage> = _messageFlow
    
    // Flow for connection events
    private val _connectionFlow = MutableSharedFlow<ConnectionEvent>()
    val connectionFlow: SharedFlow<ConnectionEvent> = _connectionFlow
    
    // Reconnection parameters
    private var reconnectJob: kotlinx.coroutines.Job? = null
    private var shouldReconnect = true
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val baseReconnectDelayMs = 1000L // 1 second
    
    init {
        // We handle reconnection manually
        this.connectionLostTimeout = 30 // 30 seconds to detect connection loss
    }
    
    /**
     * When connection is established
     */
    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d(tag, "WebSocket connected: ${handshakedata?.httpStatus}, ${handshakedata?.httpStatusMessage}")
        // Reset reconnect attempts on successful connection
        reconnectAttempts = 0
        
        coroutineScope.launch {
            _connectionFlow.emit(ConnectionEvent.Connected)
        }
    }
    
    /**
     * When message is received
     */
    override fun onMessage(message: String?) {
        Log.d(tag, "WebSocket message received: $message")
        if (message == null) return
        
        val parsedMessage = WebSocketMessage.fromJson(message)
        if (parsedMessage != null) {
            coroutineScope.launch {
                _messageFlow.emit(parsedMessage)
            }
        } else {
            Log.e(tag, "Failed to parse WebSocket message: $message")
        }
    }
    
    /**
     * When connection is closed
     */
    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d(tag, "WebSocket closed: $code, $reason, remote=$remote")
        
        coroutineScope.launch {
            _connectionFlow.emit(ConnectionEvent.Disconnected(code, reason ?: "Unknown reason"))
        }
        
        // Try to reconnect if it was remote close and we should reconnect
        if (remote && shouldReconnect) {
            attemptReconnect()
        }
    }
    
    /**
     * When error occurs
     */
    override fun onError(ex: Exception?) {
        Log.e(tag, "WebSocket error: ${ex?.message}", ex)
        coroutineScope.launch {
            _connectionFlow.emit(ConnectionEvent.Error(ex?.message ?: "Unknown error"))
        }
    }
    
    /**
     * Send message
     */
    fun sendMessage(message: WebSocketMessage) {
        try {
            if (!isOpen) {
                Log.w(tag, "Attempted to send message while connection is closed. Reconnecting...")
                attemptReconnect()
                return
            }
            
            val json = WebSocketMessage.toJson(message)
            Log.d(tag, "Sending WebSocket message: $json")
            send(json)
        } catch (e: Exception) {
            Log.e(tag, "Error sending WebSocket message", e)
            
            // Try to reconnect if there was an error sending
            if (shouldReconnect) {
                attemptReconnect()
            }
        }
    }
    
    /**
     * Attempt to reconnect to the server with exponential backoff
     */
    private fun attemptReconnect() {
        // Cancel existing reconnect job if any
        reconnectJob?.cancel()
        
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.e(tag, "Maximum reconnect attempts reached ($maxReconnectAttempts). Giving up.")
            coroutineScope.launch {
                _connectionFlow.emit(ConnectionEvent.Error("Failed to reconnect after $maxReconnectAttempts attempts"))
            }
            return
        }
        
        // Calculate delay with exponential backoff
        val delayMs = baseReconnectDelayMs * (1 shl reconnectAttempts.coerceAtMost(10))
        reconnectAttempts++
        
        Log.d(tag, "Scheduling reconnect attempt $reconnectAttempts in ${delayMs}ms")
        
        reconnectJob = coroutineScope.launch {
            kotlinx.coroutines.delay(delayMs)
            
            try {
                Log.d(tag, "Attempting to reconnect...")
                // Need to create a new client because the old one is closed
                val newClient = TicToeWebSocketClient(serverUri, coroutineScope)
                
                // Copy over the flows
                coroutineScope.launch {
                    newClient.connectionFlow.collect { event ->
                        _connectionFlow.emit(event)
                    }
                }
                
                coroutineScope.launch {
                    newClient.messageFlow.collect { message ->
                        _messageFlow.emit(message)
                    }
                }
                
                newClient.connect()
                
                Log.d(tag, "Reconnect initiated")
                
                coroutineScope.launch {
                    _connectionFlow.emit(ConnectionEvent.Reconnecting(reconnectAttempts))
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to reconnect: ${e.message}", e)
                // Try again with next backoff
                attemptReconnect()
            }
        }
    }
    
    /**
     * Stop reconnection attempts
     */
    override fun close() {
        shouldReconnect = false
        reconnectJob?.cancel()
        super.close()
    }
    
    companion object {
        /**
         * Create WebSocket connection
         */
        fun createClient(host: String, port: Int, path: String = "", coroutineScope: CoroutineScope): TicToeWebSocketClient {
            val uri = URI("ws://$host:$port${if (path.isNotEmpty()) "/$path" else ""}")
            return TicToeWebSocketClient(uri, coroutineScope)
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