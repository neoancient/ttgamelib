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
import io.ktor.util.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

public interface ClientHandler {
    /**
     * Processes a command received from the server.
     */
    public suspend fun handle(command: GameCommand)

    /**
     * The requested name is already taken on the server.
     *
     * @param suggestion An alternate name suggestion
     * @param taken A set of names that are already taken.
     * @param disconnected If true, the requested name belongs to a disconnected player, and
     *                     the client may still attempt to connect under that name by
     *                     calling [Client.reconnect]
     */
    public suspend fun nameConflict(suggestion: String, taken: Set<String>, disconnected: Boolean)

    /**
     * The handshake is complete and user is logged in.
     *
     * @param clientId The id assigned to this client by the server
     */
    public suspend fun connectionEstablished(clientId: Int)

    /**
     * A chat message has been received from the server.
     *
     * @param html The chat message encoded in HTML
     */
    public suspend fun receiveChatMessage(html: String)
}

/**
 * Handles connection to server.
 *
 * The client negotiates the connection and passes and receives commands. Any command
 * that is not handled by the [Client] is passed to the [handler].
 *
 * @property name The client's username
 * @property handler The object responsible for handling game-related commands
 */
public class Client internal constructor(
    private var name: String,
    private val handler: ClientHandler,
    private val queue: Channel<Packet>
) {
    public constructor(name: String, handler: ClientHandler) :
            this(name, handler, Channel<Packet>())

    private val logger = LoggerFactory.getLogger(javaClass)

    public val clientName: String by this::name

    @KtorExperimentalAPI
    private val client = HttpClient {
        install(WebSockets)
    }

    /**
     * Opens a connection to the server.
     *
     * @param host The server's hostname or IP address
     * @param port The server port
     */
    @KtorExperimentalAPI
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
    @KtorExperimentalAPI
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
                handlePacket(Json.decodeFromString(Packet.serializer(), frame.readText()))
            }
        } catch (e: Exception) {
            logger.error(e.localizedMessage)
        }
    }

    internal suspend fun handlePacket(packet: Packet) {
        when (packet) {
            is RequestNamePacket -> queue.send(SendNamePacket(name))
            is SuggestNamePacket -> handler.nameConflict(packet.name, packet.taken, packet.disconnected)
            is InitClientPacket -> handler.connectionEstablished(packet.clientId)
            is ChatMessagePacket -> handler.receiveChatMessage(packet.message.toHtml())
            is GameCommandPacket -> handler.handle(packet.command)
        }
    }

    /**
     * Sends string-encoded data to the server.
     */
    public suspend fun send(command: GameCommand) {
        queue.send(GameCommandPacket(command))
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
