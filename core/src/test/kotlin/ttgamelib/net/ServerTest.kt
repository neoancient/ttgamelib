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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.cio.websocket.*
import io.mockk.*
import kotlinx.serialization.json.Json

internal class ServerTest : FunSpec({

    val sentPackets = mutableListOf<Frame>()
    val session = mockk<DefaultWebSocketSession>().also {
        coEvery {
            it.send(capture(sentPackets))
        } just Runs
    }
    val engine = mockk<GameEngine>(relaxed = true)
    val connection = ClientConnection(session)
    lateinit var server: Server

    context("SendNamePacket") {
        val userName = "New User"
        server = Server("localhost", 1000, engine)

        beforeEach {
            sentPackets.clear()
        }

        test("should create new user if name is not in use") {
            server.handlePacket(SendNamePacket(userName), connection)
            val sent = sentPackets.map {
                Json.decodeFromString(Packet.serializer(), (it as Frame.Text).readText())
            }

            sent[0].shouldBeInstanceOf<InitClientPacket>()
            (sent[0] as InitClientPacket).clientId shouldBe connection.id
            sent[1].shouldBeInstanceOf<ChatMessagePacket>()
            coVerify {
                engine.playerConnected(connection.id, userName)
            }
        }

        test("should request another name if in use") {
            server.handlePacket(SendNamePacket(userName), ClientConnection(session))
            val sent = sentPackets.map {
                Json.decodeFromString(Packet.serializer(), (it as Frame.Text).readText())
            }

            with (sent[0] as SuggestNamePacket) {
                name shouldNotBe userName
                taken.shouldContain(userName)
                disconnected.shouldBeFalse()
            }
        }

        test("should check for reconnection if disconnected") {
            server.connections.remove(connection.id)
            server.handlePacket(SendNamePacket(userName), connection)
            val sent = sentPackets.map {
                Json.decodeFromString(Packet.serializer(), (it as Frame.Text).readText())
            }

            with (sent[0] as SuggestNamePacket) {
                name shouldNotBe userName
                taken.shouldContain(userName)
                disconnected.shouldBeTrue()
            }
        }

        test("should replace player on reconnection") {
            server.handlePacket(SendNamePacket(userName, true), connection)
            val sent = sentPackets.map {
                Json.decodeFromString(Packet.serializer(), (it as Frame.Text).readText())
            }

            sent[0].shouldBeInstanceOf<InitClientPacket>()
            (sent[0] as InitClientPacket).clientId shouldBe connection.id
            sent[1].shouldBeInstanceOf<ChatMessagePacket>()
            coVerify {
                engine.playerReconnected(connection.id)
            }
        }
    }
})