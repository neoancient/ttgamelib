/*
 * Tabletop Game Library
 * Copyright (c) 2021 Carl W Spain
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package ttgamelib.net

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Processes game-specific commands received by the server.
 */
public interface GameEngine {
    /**
     * Processes a command received by the server.
     */
    public suspend fun handle(json: String)

    /**
     * A player has connected to the server.
     *
     * @param id The player id
     * @param name The player name
     */
    public suspend fun playerConnected(id: Int, name: String)

    /**
     * A player has disconnected from the server.
     */
    public suspend fun playerDisconnected(id: Int)

    /**
     * A disconnected player has reconnected.
     */
    public suspend fun playerReconnected(id: Int)
}

/**
 * The game server.
 *
 * Listens for connections from clients and passes messages between the game engine
 * and the clients.
 *
 * @param address The server's hostname or IP address
 * @param serverPort The port the server listens on for new connections
 * @property gameEngine Responsible for handling game-specific commands
 */
public class Server(
    address: String, serverPort: Int,
    private val gameEngine: GameEngine
) {
    private val environment = applicationEngineEnvironment {
        module {
            module()
        }
        connector {
            host = address
            port = serverPort
        }
    }

    private val server = embeddedServer(Netty, environment)
    private val connections = ConcurrentHashMap<Int, ClientConnection>()
    private val users: Users = Users()

    /**
     * Starts the server.
     */
    public fun start(wait: Boolean = false) {
        server.start(wait)
    }

    /**
     * Stops the server.
     */
    public fun shutdown(gracePeriodMillis: Long, timeoutMillis: Long) {
        server.stop(gracePeriodMillis, timeoutMillis)
    }

    @Suppress("unused")
    private fun Application.module() {
        install(WebSockets)

        routing {
            webSocket("/") {
                val thisConnection = ClientConnection(this)
                try {
                    val packet = RequestNamePacket()
                    send(Json.encodeToString(Packet.serializer(), packet))
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        handlePacket(Json.decodeFromString(Packet.serializer(), frame.readText()), thisConnection)
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
                } finally {
                    println("Removing $thisConnection")
                    connections.remove(thisConnection.id)
                    gameEngine.playerDisconnected(thisConnection.id)
                }
            }
        }
    }

    private suspend fun handlePacket(packet: Packet, connection: ClientConnection) {
        when (packet) {
            is SendNamePacket ->
                if (packet.reconnect &&
                    users[packet.name]?.takeUnless {
                        connections.containsKey(it.connectionId)
                    } != null) {
                    connection.id = users[packet.name]!!.connectionId
                    connection.name = packet.name
                    connection.pending = false
                    connections[connection.id] = connection
                    gameEngine.playerReconnected(connection.id)
                    connection.send(InitClientPacket(connection.id))
                    send(packet = ChatMessagePacket(SystemMessage("${packet.name} reconnected")))
                } else if (packet.name in users) {
                    users.suggestAlternateName(packet.name).let {
                        connection.send(SuggestNamePacket(it.first, it.second,
                            connections.values.none {
                                it.name == packet.name
                            }))
                    }
                } else {
                    users += User(packet.name, connection.id)
                    connection.name = packet.name
                    connection.pending = false
                    connections[connection.id] = connection
                    gameEngine.playerConnected(connection.id, packet.name)
                    connection.send(InitClientPacket(connection.id))
                    send(packet = ChatMessagePacket(SystemMessage("${packet.name} joined")))
                }
            is TextPacket -> gameEngine.handle(packet.text)
            is ChatCommandPacket -> processChatMessage(packet)
        }
    }

    /**
     * Sends a [packet] to client with [id].
     *
     * If the id is < 0, the packet is sent to all connected clients.
     */
    internal suspend fun send(id: Int = ALL_CLIENTS, packet: Packet) {
        if (id < 0) {
            connections.values.forEach {
                it.send(packet)
            }
        } else {
            connections[id]?.send(packet)
        }
    }

    /**
     * Wraps the [data] in a [TextPacket] and sends it to client [id].
     */
    public suspend fun send(id: Int, data: String) {
        send(id, TextPacket(data))
    }

    private suspend fun processChatMessage(packet: ChatCommandPacket) {
        fun name(id: Int) = connections[id]?.name ?: "<unknown>"
        fun shiftToken(text: String): Pair<String, String> {
            if (text.startsWith("\"")) {
                text.substring(1).indexOf("\"").takeIf { it > 0 }?.let {
                    return text.substring(1, it + 1) to text.substring(it + 2).trim()
                } ?: return "" to text
            } else {
                return text.indexOf(" ").takeIf { it > 0 }?.let {
                    text.substring(0, it) to text.substring(it).trim()
                } ?: text to ""
            }
        }

        val response = if (packet.text.startsWith("/")) {
            val (command, remainder) = shiftToken(packet.text)

            when (command) {
                "/em", "/me" -> EmoteMessage(name(packet.clientId), remainder)
                "/w" -> {
                    val (recipient, message) = shiftToken(remainder)
                    users[recipient]?.let {
                        WhisperMessage(name(packet.clientId), it.name, message)
                    } ?: InfoMessage("Unknown user $recipient")
                }
                else -> InfoMessage("Unknown command")
            }
        } else {
            SimpleMessage(name(packet.clientId), packet.text)
        }
        val sendPacket = ChatMessagePacket(response)
        when (response) {
            is InfoMessage -> connections[packet.clientId]?.send(sendPacket)
            is WhisperMessage -> {
                connections[packet.clientId]?.send(sendPacket)
                users[response.recipient]?.connectionId?.takeUnless {
                    it == packet.clientId
                }?.let {
                    connections[it]?.send(sendPacket)
                }
            }
            else -> connections.values.forEach { it.send(sendPacket) }
        }
    }
}

private class ClientConnection(val session: DefaultWebSocketSession) {
    companion object {
        val lastId = AtomicInteger(0)
    }
    var id = lastId.getAndIncrement()
    var userId = id
    var name: String = "New Player"
    var pending: Boolean = true

    suspend fun send(packet: Packet) {
        session.send(Frame.Text(Json.encodeToString(Packet.serializer(), packet)))
    }
}

