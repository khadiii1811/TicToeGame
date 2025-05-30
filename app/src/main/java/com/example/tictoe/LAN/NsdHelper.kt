package com.example.tictoe.LAN

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class NsdHelper(
        private val context: Context,
        private val onServiceFound: (room: RoomInfo) -> Unit = {},
        private val roomInfo: RoomInfo = RoomInfo()
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: NsdManager.ResolveListener? = null

    // Call this for host
    fun registerService() {
        val serviceInfo =
                NsdServiceInfo().apply {
                    this.serviceName = roomInfo.host // Use unique host string as serviceName
                    this.serviceType = "_http._tcp."
                    this.port = roomInfo.port
                    setAttribute("roomName", roomInfo.roomName)
                    setAttribute("hostName", roomInfo.hostName)
                    setAttribute("status", roomInfo.status)
                    setAttribute("createAt", roomInfo.createAt.toString())
                }

        registrationListener =
                object : NsdManager.RegistrationListener {
                    override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                        Log.d("NsdHelper", "Service registered: ${serviceInfo.serviceName}")
                    }

                    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e("NsdHelper", "Registration failed: $errorCode")
                    }

                    override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                        Log.d("NsdHelper", "Service unregistered: ${serviceInfo.serviceName}")
                    }

                    override fun onUnregistrationFailed(
                            serviceInfo: NsdServiceInfo,
                            errorCode: Int
                    ) {
                        Log.e("NsdHelper", "Unregistration failed: $errorCode")
                    }
                }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    // Call this for clients
    fun discoverServices() {
        discoveryListener =
                object : NsdManager.DiscoveryListener {
                    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                        if (serviceInfo.serviceType == "_http._tcp." &&
                                        serviceInfo.serviceName.startsWith("CaroRoom_")
                        ) {
                            nsdManager.resolveService(serviceInfo, createResolveListener())
                        }
                    }

                    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                        Log.e("NsdHelper", "Discovery start failed: $errorCode")
                    }

                    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                        Log.e("NsdHelper", "Discovery stop failed: $errorCode")
                    }

                    override fun onDiscoveryStarted(serviceType: String) {
                        Log.d("NsdHelper", "Discovery started")
                    }

                    override fun onDiscoveryStopped(serviceType: String) {
                        Log.d("NsdHelper", "Discovery stopped")
                    }

                    override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                        Log.w("NsdHelper", "Service lost: ${serviceInfo.serviceName}")
                    }
                }

        nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    private fun createResolveListener(): NsdManager.ResolveListener {
        return object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e("NsdHelper", "Resolve failed: $errorCode")
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        val host = serviceInfo.host?.hostAddress ?: return
                        val port = serviceInfo.port
                        val attributes = serviceInfo.attributes
                        val roomName =
                                attributes["roomName"]?.let { String(it, Charsets.UTF_8) }
                                        ?: "Unknown"
                        val hostName =
                                attributes["hostName"]?.let { String(it, Charsets.UTF_8) }
                                        ?: "Unknown"
                        val status =
                                attributes["status"]?.let { String(it, Charsets.UTF_8) }
                                        ?: "available"
                        val createAt =
                                attributes["createAt"]?.let {
                                    String(it, Charsets.UTF_8).toLongOrNull()
                                }
                                        ?: System.currentTimeMillis()

                        val room =
                                RoomInfo(
                                        roomName = roomName,
                                        hostName = hostName,
                                        status = status,
                                        host = host,
                                        port = port,
                                        createAt = createAt
                                )

                        Log.d("NsdHelper", "Resolved service: $roomName @ $host:$port")
                        onServiceFound(room)
                    }
                }
                .also { resolveListener = it }
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Log.e("NsdHelper", "Failed to stop discovery: ${e.message}")
            }
        }
        discoveryListener = null
    }

    fun unregisterService() {
        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: Exception) {
                Log.e("NsdHelper", "Failed to unregister: ${e.message}")
            }
        }
        registrationListener = null
    }
}
