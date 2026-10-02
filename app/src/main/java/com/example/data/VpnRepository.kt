package com.example.data

import com.example.model.ServerLocation
import com.example.model.VpnSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class VpnRepository(private val dao: VpnSessionDao) {

    private val _servers = MutableStateFlow<List<ServerLocation>>(emptyList())
    val servers = _servers.asStateFlow()

    fun replaceServers(servers: List<ServerLocation>) {
        _servers.value = servers
    }

    fun toggleFavorite(serverId: String) {
        _servers.value = _servers.value.map { server ->
            if (server.id == serverId) server.copy(isFavorite = !server.isFavorite) else server
        }
    }

    val sessions: Flow<List<VpnSession>> = dao.getAllSessions()

    suspend fun saveSession(session: VpnSession): Long = dao.insertSession(session)

    suspend fun clearHistory() = dao.clearAll()
}
