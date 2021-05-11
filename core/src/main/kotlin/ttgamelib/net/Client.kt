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

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ttgamelib.ClientController
import ttgamelib.Configuration

/**
 * Handles connection to server.
 *
 * The client negotiates the connection and passes and receives commands. Any command
 * that is not handled by the [Client] is passed to the [controller].
 *
 * @property name The client's username
 * @property controller The object responsible for handling game-related commands
 */
public class Client internal constructor(
    private var name: String,
    private val controller: ClientController,
    private val queue: Channel<Packet>
) {
    public constructor(name: String, controller: ClientController) :
            this(name, controller, Channel<Packet>())

    private val logger = LoggerFactory.getLogger(javaClass)

    public val clientName: String by this::name

    private val client = HttpClient {
        install(WebSockets)
    }

    /**
     * Opens a connection to the server.
     *
     * @param host The server's hostname or IP address
     * @param port The server port
     */
    public suspend fun start(host: String, port: Int) {
        client.webSocket(method = HttpMethod.Get, host = host, port = port, path = "/") {
            val sendRoutine = launch { sendPackets() }
            val receiveRoutine = launch { receivePackets() }

            receiveRoutine.join()
            sendRoutine.cancelAndJoin()
        }
        client.close()
        logger.info("Client closed")
    }

    /**
     * Closes the connection to the server.
     */
    public fun stop() {
        client.close()
    }

    private suspend fun DefaultClientWebSocketSession.sendPackets() {
        for (packet in queue) {
            send(Json.encodeToString(Packet.serializer(), packet))
        }
    }

    private suspend fun DefaultClientWebSocketSession.receivePackets() {
        try {
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                controller.handle(Json {
                    Configuration.serializersModule
                }.decodeFromString(Packet.serializer(), frame.readText()))
            }
        } catch (e: Exception) {
            logger.error(e.localizedMessage)
        }
    }

    /**
     * Sends a packet to the server.
     */
    public suspend fun send(packet: Packet) {
        queue.send(packet)
    }

    /**
     * Changes the client name and notifies the server.
     */
    public suspend fun changeName(name: String) {
        this.name = name
        queue.send(SendNamePacket(name))
    }

    /**
     * Attempts reconnect as an existing user.
     *
     * This is sent after the connection is established.
     */
    public suspend fun reconnect() {
        queue.send(SendNamePacket(name, true))
    }

    /**
     * Sends a chat message to the server.
     */
    public suspend fun sendChatMessage(clientId: Int, text: String) {
        queue.send(ChatCommandPacket(clientId, text))
    }
}
