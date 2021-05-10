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
import ttgamelib.net.ALL_CLIENTS
import ttgamelib.net.AddPlayerPacket
import ttgamelib.net.PlayerDisconnectionPacket
import ttgamelib.net.SendGamePacket

/**
 * Processes game-specific commands received by the server.
 */
public interface GameEngine {
    /**
     * Processes a command received by the server.
     */
    public suspend fun handle(clientId: Int, packet: Packet)

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

public abstract class AbstractGameEngine<B: Board, E: Entity, G: Game<B, E>>(
    address: String,
    port: Int
) : GameEngine {
    public abstract val game: G
    private val server = Server(address, port,this)

    public fun startServer() {
        server.start()
    }

    public fun stopServer() {
        server.shutdown(1000, 1000)
    }

    override suspend fun playerConnected(id: Int, name: String) {
        val player = game.newPlayer(id, name)
        server.send(id, SendGamePacket(game))
        server.send(ALL_CLIENTS, AddPlayerPacket(player))
    }

    override suspend fun playerReconnected(id: Int) {
        server.send(id, SendGamePacket(game))
        server.send(ALL_CLIENTS, PlayerDisconnectionPacket(id, false))
    }

    override suspend fun playerDisconnected(id: Int) {
        game.getPlayer(id)?.disconnected = true
        server.send(ALL_CLIENTS, PlayerDisconnectionPacket(id, true))
    }

    override suspend fun handle(clientId: Int, packet: Packet) {
        when (packet) {
            is UpdatePlayerPacket -> updatePlayer(clientId, packet)
            is PlayerReadyPacket -> playerReady(clientId, packet)
            is AddEntityPacket -> addEntity(clientId, packet)
            is RemoveEntityPacket -> removeEntity(clientId, packet)
            is SetBoardPacket -> setBoard(clientId, packet)
            is GameCommandPacket -> handleCommand(clientId, packet.command)
            else -> LoggerFactory.getLogger(javaClass).warn("Received packet ${packet::class.simpleName} in GameEngine")
        }
    }

    public open suspend fun updatePlayer(clientId: Int, packet: UpdatePlayerPacket) {
        if (game.getPlayer(clientId)?.canEdit(packet.player.id) == true) {
            game.getPlayer(packet.player.id)?.set(packet.player)
            server.send(ALL_CLIENTS, packet)
        } else {
            LoggerFactory.getLogger(javaClass)
                .warn("Client $clientId attempted to update player ${packet.player.id}")
        }
    }

    public open suspend fun playerReady(clientId: Int, packet: PlayerReadyPacket) {
        game.getPlayer(packet.playerId)?.ready = packet.ready
    }

    @Suppress("UNCHECKED_CAST")
    public open suspend fun addEntity(clientId: Int, packet: AddEntityPacket) {
        try {
            game.addUnit(packet.entity as E, clientId)
            server.send(ALL_CLIENTS, packet)
        } catch (e: ClassCastException) {
            LoggerFactory.getLogger(javaClass)
                .error("Attempted to add Entity of class ${packet.entity::class.qualifiedName}")
        }
    }

    public open suspend fun removeEntity(clientId: Int, packet: RemoveEntityPacket) {
        if (game.getEntity(packet.entityId)?.playerId == clientId) {
            game.removeUnit(packet.entityId)
            server.send(ALL_CLIENTS, packet)
        } else if (game.getEntity(packet.entityId) != null) {
            LoggerFactory.getLogger(javaClass)
                .warn("Client $clientId attempted to remove unit belonging to player ${game.getEntity(packet.entityId)?.playerId}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    public open suspend fun setBoard(clientId: Int, packet: SetBoardPacket) {
        try {
            game.board = packet.board as B
            server.send(ALL_CLIENTS, packet)
        } catch (e: ClassCastException) {
            LoggerFactory.getLogger(javaClass)
                .error("Attempted to add Entity of class ${packet.board::class.qualifiedName}")
        }
    }

    public abstract suspend fun handleCommand(clientId: Int, command: GameCommand)
}
