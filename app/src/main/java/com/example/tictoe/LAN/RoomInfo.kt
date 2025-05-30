package com.example.tictoe.LAN

data class RoomInfo(
        val roomName: String = "",
        val hostName: String = "",
        val status: String = "available",
        val host: String = "",
        val port: Int = -1,
        val createAt: Long = System.currentTimeMillis()
)
