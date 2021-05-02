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

internal const val ALL_CLIENTS = -1

/**
 * Commands used by the client and the server to negotiate the connection.
 */
@Serializable
internal sealed class Packet

/**
 * Sent by the server to request the name to use for the client
 */
@Serializable
internal class RequestNamePacket : Packet()

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

@Serializable
internal class InitClientPacket(val clientId: Int) : Packet()

/**
 * Wrapper for text data
 */
@Serializable
internal class TextPacket(val text: String) : Packet()

/**
 * Wrapper for binary data
 */
@Serializable
internal class BinaryPacket(val data: ByteArray) : Packet()

@Serializable
internal class ChatCommandPacket(val clientId: Int, val text: String) : Packet()

@Serializable
internal class ChatMessagePacket(val message: ChatMessage) : Packet()

