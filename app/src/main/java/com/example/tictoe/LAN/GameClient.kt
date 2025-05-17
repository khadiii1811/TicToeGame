package com.example.tictoe.LAN

import java.io.*
import java.net.Socket
import kotlin.concurrent.thread

class GameClient(private val host: String, private val port: Int = 8888) {
    private var socket: Socket? = null
    private var output: PrintWriter? = null
    private var input: BufferedReader? = null

    fun connect(onMessage: (Any) -> Unit) {
        thread {
            socket = Socket(host, port)
            output = PrintWriter(socket!!.getOutputStream(), true)
            input = BufferedReader(InputStreamReader(socket!!.getInputStream()))

            while (socket != null) {
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

    fun disconnect() {
        output?.println(MSG_DISCONNECT)
        socket?.close()
    }
}