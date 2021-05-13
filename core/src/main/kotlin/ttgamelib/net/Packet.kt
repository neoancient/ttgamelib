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

import kotlinx.serialization.Serializable
import ttgamelib.*

internal const val ALL_CLIENTS = -1

/**
 * Package for data exchange between the server and the clients
 */
@Serializable
public sealed class Packet

/**
 * Sent by the server to request the name to use for the client
 */
@Serializable
internal object RequestNamePacket : Packet()

/**
 * Sent by the client to request a user name
 */
@Serializable
internal class SendNamePacket(
    /** The requested name */
    val name: String,
    /** Whether this is an attempt to reconnect using an existing name */
    val reconnect: Boolean = false
) : Packet()

/**
 * Sent by the server when the requested name is already taken
 */
@Serializable
internal class SuggestNamePacket(
    /** The suggested replacement name */
    val name: String,
    /** The names that are currently taken */
    val taken: Set<String>,
    /** Whether the requested name belongs to a disconnected user */
    val disconnected: Boolean
) : Packet()

/**
 * Sent by the server to complete the handshake and send the [clientId] that identifies the
 * connection
 */
@Serializable
internal class InitClientPacket(val clientId: Int) : Packet()

/**
 * Wrapper for game-specific commands
 */
@Serializable
public class GameCommandPacket(public val command: GameCommand) : Packet()

/**
 * Sent by the client to the server when a user enters a chat command
 */
@Serializable
public class ChatCommandPacket(public val clientId: Int, public val text: String) : Packet()

/**
 * Sent by the server to any clients that should see the [message]
 */
@Serializable
public class ChatMessagePacket(public val message: ChatMessage) : Packet()

/**
 * Sent by the server to the client to update the game state on completion of handshake
 * or reconnection.
 */
@Serializable
public class SendGamePacket(public val game: Game<*, *>) : Packet()

/**
 * Sent by the server to all clients when a player is added to the game.
 */
@Serializable
public class AddPlayerPacket(public val player: Player) : Packet()

/**
 * Sent by the server to all clients when a player is removed from the game.
 */
@Serializable
public class RemovePlayerPacket(public val playerId: Int) : Packet()

/**
 * Sent by the client to the server to change the settings of the player identified with the client.
 * Sent by the server to all clients to propagate the changes.
 */
@Serializable
public class UpdatePlayerPacket(public val player: Player) : Packet()

/**
 * Sent by the client to the server to notify that the player's ready status has changed.
 * Sent by the server to the clients when settings have changed and the ready status needs
 * to be reset.
 */
@Serializable
public class PlayerReadyPacket(public val playerId: Int, public val ready: Boolean) : Packet()

/**
 * Sent by the server to all clients when a player has become disconnected or reconnected.
 */
@Serializable
public class PlayerDisconnectionPacket(public val playerId: Int, public val disconnected: Boolean) : Packet()

/**
 * Sent by the client to the server to request the [entity] be added to the player's force.
 * Sent by the server to all clients when the entity is added.
 */
@Serializable
public class AddEntityPacket(public val entity: Entity) : Packet()

/**
 * Sent by the client to the server when a player has removed an entity from the force.
 * Sent by the server to the clients to notify of the change.
 */
@Serializable
public class RemoveEntityPacket(public val entityId: Int) : Packet()

/**
 * Sent by the client to the server when a user has changed the game board.
 * Sent by the server to the clients to notify of the change.
 */
@Serializable
public class SetBoardPacket(public val board: Board) : Packet()

/**
 * Sent by the client to the server when a user has changed the weather settings.
 * Sent by the server to the clients to notify of the change.
 */
@Serializable
public class SetWeatherPacket(public val weather: Weather) : Packet()

/**
 * Implemented by classes used to transmit game-specific commands between the server and client.
 *
 * Implementing classes need to be serializable.
 */
public interface GameCommand