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

package ttgamelib

import org.slf4j.LoggerFactory
import ttgamelib.net.*
import ttgamelib.net.InitClientPacket
import ttgamelib.net.RequestNamePacket
import ttgamelib.net.SendNamePacket
import ttgamelib.net.SuggestNamePacket
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 */
public interface ClientController {

    public var clientId: Int

    /**
     * Processes a [packet] received from the server.
     */
    public suspend fun handle(packet: Packet)

    /**
     * Subscribes to notifications of connection events.
     */
    public fun addConnectionListener(listener: ClientListener)

    /**
     * Unsubscribes to notifications of connection events.
     */
    public fun removeConnectionListener(listener: ClientListener)
}

/**
 * Listener for events fired when client connects to or disconnects from the server.
 */
public interface ClientListener {
    /**
     * Called when client connects to the server.
     */
    public fun clientConnected(controller: ClientController)

    /**
     * Called when client disconnects from the server.
     */
    public fun clientDisconnected(controller: ClientController)

    /**
     * Called when the requested name is already taken. Provides a [suggestion] that
     * is guaranteed unique, as well as a collection of names that are already
     * [taken], and whether the requested name belongs to a [disconnected] user.
     */
    public fun nameTaken(controller: ClientController, suggestion: String, taken: Set<String>, disconnected: Boolean)

    /**
     * Called when the [Game] is set, either on initial connection or when reconnecting.
     */
    public fun gameChanged(old: Game<*, *>, new: Game<*, *>)
}

public abstract class AbstractClientController<B: Board, E: Entity, G: Game<B, E>>(
    clientName: String
) : ClientController {
    public abstract var game: G
    override var clientId: Int = -1
    private val client: Client = Client(clientName, this)
    private val clientListeners: MutableList<ClientListener> = CopyOnWriteArrayList()

    public suspend fun start(host: String, port: Int) {
        client.start(host, port)
    }

    public fun stop() {
        client.stop()
    }

    public fun player(): Player = game.getPlayer(clientId) ?: error("Game does not have player for client $clientId")

    @Suppress("UNCHECKED_CAST")
    override suspend fun handle(packet: Packet) {
        when (packet) {
            is RequestNamePacket -> send(SendNamePacket(client.clientName))
            is SuggestNamePacket ->
                clientListeners.forEach {
                    it.nameTaken(this, packet.name, packet.taken, packet.disconnected)
                }

            is InitClientPacket ->  {
                clientId = packet.clientId
                clientConnected()
                clientListeners.forEach {
                    it.clientConnected(this)
                }
            }
            is ChatMessagePacket -> game.appendChat(packet.message.toHtml())
            is SendGamePacket -> {
                with (packet.game as G) {
                    clientListeners.forEach { it.gameChanged(game, this) }
                    game.transferListeners(this)
                    game = this
                }
            }
            is AddPlayerPacket -> if (packet.player.id != clientId) game.addPlayer(packet.player)
            is RemovePlayerPacket -> game.removePlayer(packet.playerId)
            is UpdatePlayerPacket -> game.updatePlayer(packet.player.id, packet.player)
            is PlayerReadyPacket -> game.playerReady(packet.playerId, packet.ready)
            is PlayerDisconnectionPacket -> game.playerDisconnected(packet.playerId, packet.disconnected)
            is AddEntityPacket -> game.replaceUnit(packet.entity.entityId, packet.entity as E)
            is RemoveEntityPacket -> game.removeUnit(packet.entityId)
            is SetBoardPacket -> game.board = packet.board as B
            is SetWeatherPacket -> game.setWeather(packet.weather)
            is GameCommandPacket -> handle(packet.command)
            else -> LoggerFactory.getLogger(javaClass).warn("Received packet ${packet::class.simpleName} in ${this::class.simpleName}")
        }
    }

    internal suspend fun send(packet: Packet) {
        client.send(packet)
    }

    public suspend fun send(command: GameCommand) {
        client.send(GameCommandPacket(command))
    }

    override fun addConnectionListener(listener: ClientListener) {
        clientListeners.add(listener)
    }

    override fun removeConnectionListener(listener: ClientListener) {
        clientListeners.remove(listener)
    }

    /**
     * Processes a [command] received from the server.
     */
    public abstract suspend fun handle(command: GameCommand)

    /**
     * Changes the client name and notifies the server.
     */
    public suspend fun changeName(name: String) {
        client.changeName(name)
    }

    public open suspend fun clientConnected() {
    }

    /**
     * Attempts reconnect as an existing user.
     */
    public suspend fun reconnect() {
        send(SendNamePacket(client.clientName, true))
    }

    /**
     * Sends a chat message to the server.
     */
    public suspend fun sendChatMessage(text: String) {
        send(ChatCommandPacket(clientId, text))
    }

    /**
     * Sends new board settings to the server.
     */
    public suspend fun sendBoard(board: B) {
        send(SetBoardPacket(board))
    }

    /**
     * Sends new weather settings to the server.
     */
    public suspend fun sendWeather(weather: Weather) {
        send(SetWeatherPacket(weather))
    }

    public suspend fun sendReady(ready: Boolean) {
        send(PlayerReadyPacket(clientId, ready))
    }

    public suspend fun sendUpdatePlayer(player: Player) {
        send(UpdatePlayerPacket(player))
    }

    public suspend fun addEntity(entity: E) {
        send(AddEntityPacket(entity))
    }

    public suspend fun removeEntity(entityId: Int) {
        send(RemoveEntityPacket(entityId))
    }
}
