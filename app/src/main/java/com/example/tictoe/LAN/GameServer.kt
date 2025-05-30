package com.example.tictoe.LAN

import android.util.Log
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class GameServer {
    private lateinit var serverSocket: ServerSocket
    private var clientSocket: Socket? = null
    private var output: PrintWriter? = null
    private var input: BufferedReader? = null

    val port: Int
        get() = if (::serverSocket.isInitialized) serverSocket.localPort else -1

    /** Start the server and listen for incoming connections */
    fun startServer(
            onServerStarted: (port: Int) -> Unit,
            onMessage: (Any) -> Unit,
            onClientConnected: (() -> Unit)
    ) {
        thread {
            try {
                serverSocket = ServerSocket(0) // Bind to any free port
                val actualPort = serverSocket.localPort
                //  Invoke the callback with the actual port
                //  Ensure this is called on the main thread if NsdHelper needs it
                onServerStarted(actualPort)

                clientSocket = serverSocket.accept()
                input = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
                output = PrintWriter(clientSocket!!.getOutputStream(), true)
                Log.d(
                        "GameServer",
                        "Client connected: ${clientSocket!!.inetAddress.hostAddress}. Starting message loop."
                )
                onClientConnected.invoke() // Notify that a client has connected

                while (clientSocket != null && !clientSocket!!.isClosed) {
                    val line = input?.readLine() ?: break
                    val parsed = parseMessage(line)
                    onMessage(parsed ?: line) // Send full line if not a parsed move
                    Log.d("GameServer", "Received message: $line")
                }
            } catch (e: Exception) {
                Log.e("GameServer", "Error in server loop: ${e.message}")
            }
        }
    }

    /** Send a message to the client */
    fun send(data: Any) {
        thread {
            try {
                val msg = messageToData(data)
                output?.println(msg)
                output?.flush()
                Log.d("GameServer", "Sent message: $msg")
            } catch (e: Exception) {
                Log.e("GameServer", "Error sending message", e)
            }
        }
    }

    /** Stop the server */
    fun stop() {
        try {
            clientSocket?.close()
            serverSocket.close()
            Log.d("GameServer", "Server stopped")
        } catch (e: Exception) {
            Log.e("GameServer", "Error stopping server", e)
        }
    }
}
