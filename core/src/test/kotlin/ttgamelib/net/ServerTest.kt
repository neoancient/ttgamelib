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
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainValue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import io.ktor.http.cio.websocket.*
import io.mockk.*
import kotlinx.serialization.json.Json

private inline fun<reified T: Packet> List<Frame>.decode(): List<T> {
    return map {
        Json.decodeFromString(Packet.serializer(), (it as Frame.Text).readText()) as T
    }
}

private fun createConnection(userName: String, clientId: Int): Pair<ClientConnection, MutableList<Frame>> {
    val sentPackets = mutableListOf<Frame>()
    val session = mockk<DefaultWebSocketSession>().also {
        coEvery {
            it.send(capture(sentPackets))
        } just Runs
    }
    val connection = ClientConnection(session).apply {
        name = userName
        id = clientId
    }
    return connection to sentPackets
}

internal class ServerTest : FunSpec({

    val engine = mockk<GameEngine>(relaxed = true)
    val (connection, sentPackets) = createConnection("player1", 1)
    lateinit var server: Server

    beforeEach {
        sentPackets.clear()
    }

    context("SendNamePacket") {
        val userName = "New User"
        server = Server("localhost", 1000, engine)

        test("should create new user if name is not in use") {
            server.handlePacket(SendNamePacket(userName), connection)
            val sent = sentPackets.decode<Packet>()

            sent[0].shouldBeInstanceOf<InitClientPacket>()
            (sent[0] as InitClientPacket).clientId shouldBe connection.id
            sent[1].shouldBeInstanceOf<ChatMessagePacket>()
            coVerify {
                engine.playerConnected(connection.id, userName)
            }
        }

        test("should request another name if in use") {
            val (connection2, sentPackets2) = createConnection("player2", 2)
            server.handlePacket(SendNamePacket(userName), connection2)
            val sent = sentPackets2.decode<SuggestNamePacket>()

            with (sent[0]) {
                name shouldNotBe userName
                taken.shouldContain(userName)
                disconnected.shouldBeFalse()
            }
        }

        test("should check for reconnection if disconnected") {
            server.connections.remove(connection.id)
            server.handlePacket(SendNamePacket(userName), connection)
            val sent = sentPackets.decode<SuggestNamePacket>()

            with (sent[0]) {
                name shouldNotBe userName
                taken.shouldContain(userName)
                disconnected.shouldBeTrue()
            }
        }

        test("should replace player on reconnection") {
            server.handlePacket(SendNamePacket(userName, true), connection)
            val sent = sentPackets.decode<Packet>()

            sent[0].shouldBeInstanceOf<InitClientPacket>()
            (sent[0] as InitClientPacket).clientId shouldBe connection.id
            sent[1].shouldBeInstanceOf<ChatMessagePacket>()
            coVerify {
                engine.playerReconnected(connection.id)
            }
        }

        test("should not replace player that is connected") {
            server.handlePacket(SendNamePacket(userName, true), connection)
            val sent = sentPackets.decode<SuggestNamePacket>()

            with (sent[0]) {
                name shouldNotBe userName
                disconnected.shouldBeFalse()
            }
        }
    }

    test("TextPacket should be sent to game engine") {
        val packet = TextPacket("json goes here")
        server.handlePacket(packet, connection)

        sentPackets.shouldBeEmpty()
        coVerify {
            engine.handle(packet.text)
        }
    }

    context("chat command") {
        server = Server("localhost", 1000, engine)
        val connections = mutableListOf<ClientConnection>()
        val outgoing = mutableListOf<MutableList<Frame>>()
        for (i in 1..3) {
            with (createConnection("player$i", i)) {
                connections += first
                outgoing += second
                server.handlePacket(SendNamePacket(first.name), first)
            }
        }

        beforeEach {
            outgoing.forEach { it.clear() }
        }

        test("there should be three connected players") {
            connections.forAll {
                server.connections.shouldContainValue(it)
            }
        }

        test("emote should send message to all users") {
            forAll(
                row("/em is here"),
                row("/me is here")
            ) { msg ->
                server.handlePacket(ChatCommandPacket(connection.id, msg), connection)
            }
            outgoing.forEach {
                with (it.decode<ChatMessagePacket>()) {
                    shouldHaveSize(2)
                    forAll { it.message.shouldBeTypeOf<EmoteMessage>() }
                }
            }
        }

        test("whisper should send message to one user") {
            server.handlePacket(ChatCommandPacket(connections[0].id, "/w player2 a secret"), connections[0])

            outgoing[0].shouldHaveSize(1)
            outgoing[1].shouldHaveSize(1)
            outgoing[2].shouldBeEmpty()
        }

        test("whisper to unknown user should send info message to sender") {
            server.handlePacket(ChatCommandPacket(connections[1].id, "/w nobody a secret"), connections[1])

            outgoing[0].shouldHaveSize(0)
            outgoing[2].shouldHaveSize(0)
            outgoing[1].shouldHaveSize(1)
            outgoing[1].decode<ChatMessagePacket>().forAll {
                it.message.shouldBeTypeOf<InfoMessage>()
            }
        }

        test("unknown command should send info message to sender") {
            server.handlePacket(ChatCommandPacket(connections[1].id, "/foo bar baz"), connections[1])

            outgoing[0].shouldHaveSize(0)
            outgoing[2].shouldHaveSize(0)
            outgoing[1].shouldHaveSize(1)
            outgoing[1].decode<ChatMessagePacket>().forAll {
                it.message.shouldBeTypeOf<InfoMessage>()
            }
        }
    }
})