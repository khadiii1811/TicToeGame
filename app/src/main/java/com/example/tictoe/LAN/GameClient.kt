package com.example.tictoe.LAN

import android.util.Log
import java.io.*
import java.net.Socket
import kotlin.concurrent.thread

class GameClient(private val host: String, private val port: Int) {
    private var socket: Socket? = null
    private var output: PrintWriter? = null
    private var input: BufferedReader? = null

    /** Connect to the server and start receiving messages */
    fun connect(playerName: String, onMessage: (Any) -> Unit, onConnected: (() -> Unit)) {
        thread {
            try {
                socket = Socket(host, port)
                output = PrintWriter(socket!!.getOutputStream(), true)
                input = BufferedReader(InputStreamReader(socket!!.getInputStream()))

                // Notify that the connection is established
                Log.d("GameClient", "Connected to server at $host:$port")
                onConnected.invoke()

                // Send a connection message to the server
                output?.println("$MSG_PLAYER_NAME $playerName") // Notify server of player name
                Log.d("GameClient", "Sent player name message")

                // Start reading messages from the server
                while (socket != null && !socket!!.isClosed) {
                    val line = input?.readLine() ?: break
                    parseMessage(line)?.let { onMessage(it) }
                    Log.d("GameClient", "Received message: $line")
                }
            } catch (e: IOException) {
                Log.e("GameClient", "Error in client loop: ${e.message}")
            } finally {
                // Clean up resources
                input?.close()
                output?.close()
                socket?.close()
                socket = null
            }
        }
    }

    /** Send a message to the server */
    fun send(data: Any) {
        thread {
            try {
                val msg = messageToData(data)
                output?.println(msg)
                output?.flush()
                Log.d("GameClient", "Sent message: $msg")
            } catch (e: Exception) {
                Log.e("GameClient", "Error sending message", e)
            }
        }
    }

    /** Disconnect from the server */
    fun disconnect() {
        try {
            output?.println(MSG_DISCONNECT)
            socket?.close()
            Log.d("GameClient", "Disconnected from server")
        } catch (e: Exception) {
            Log.e("GameClient", "Error disconnecting", e)
        }
    }
}
