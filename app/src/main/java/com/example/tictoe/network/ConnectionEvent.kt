package com.example.tictoe.network

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