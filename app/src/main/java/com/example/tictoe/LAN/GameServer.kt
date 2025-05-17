package com.example.tictoe.LAN

import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class GameServer(private val port: Int = 8888) {
    private lateinit var serverSocket: ServerSocket
    private var clientSocket: Socket? = null
    private var output: PrintWriter? = null
    private var input: BufferedReader? = null

    fun startServer(onMessage: (Any) -> Unit) {
        thread {
            serverSocket = ServerSocket(port)
            clientSocket = serverSocket.accept()
            input = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
            output = PrintWriter(clientSocket!!.getOutputStream(), true)

            while (clientSocket != null) {
                val line = input!!.readLine() ?: break
                parseMessage(line)?.let { onMessage(it) }
            }
        }
    }

    fun send(data: Any) {
        val msg = when (data) {
            is Pair<*, *> -> moveToMessage(data as Pair<Int, Int>)
            is String -> data
            else -> return
        }
        output?.println(msg)
    }

    fun stop() {
        clientSocket?.close()
        serverSocket.close()
    }
}