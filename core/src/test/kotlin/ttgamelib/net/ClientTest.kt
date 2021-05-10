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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.yield

internal class ClientTest : FunSpec({
    val playerName = "player"
    val handler = mockk<ClientHandler>(relaxUnitFun = true)
    val slot = CapturingSlot<Packet>()
    val queue = mockk<Channel<Packet>>()
    val client = Client(playerName, handler, queue)

    coEvery { queue.send(capture(slot)) } just Runs

    beforeEach {
        slot.clear()
    }

    test("RequestNamePacket should be answered with SendNamePacket") {
        client.handlePacket(RequestNamePacket)

        with (slot.captured) {
            shouldBeTypeOf<SendNamePacket>()
            name shouldBe playerName
            reconnect.shouldBeFalse()
        }
    }

    test("name conflict should notify handler") {
        val packet = SuggestNamePacket("newName", setOf("player1", "player2"), false)
        client.handlePacket(packet)

        coVerify {
            handler.nameConflict(packet.name, packet.taken, packet.disconnected)
        }
    }

    test("init client should notify handler of established connection") {
        val packet = InitClientPacket(42)
        client.handlePacket(packet)

        coVerify {
            handler.connectionEstablished(packet.clientId)
        }
    }

    test("text packet should be passed to handler") {
        val command = object : GameCommand {}
        val packet = GameCommandPacket(command)
        client.handlePacket(packet)

        coVerify {
            handler.handle(command)
        }
    }

    test("chat message should be sent to handler as html") {
        val packet = ChatMessagePacket(InfoMessage("This is a message"))
        client.handlePacket(packet)

        coVerify {
            handler.receiveChatMessage(packet.message.toHtml())
        }
    }

    test("send should encode data as GameCommandPacket") {
        val data = object : GameCommand {}
        client.send(data)

        with(slot.captured) {
            shouldBeTypeOf<GameCommandPacket>()
            command shouldBe data
        }
    }

    test("reconnect should send reconnect request to server") {
        client.reconnect()

        with (slot.captured) {
            shouldBeTypeOf<SendNamePacket>()
            name shouldBe client.clientName
            reconnect.shouldBeTrue()
        }
    }

    test("changeName should change client name and notify server") {
        val newName = "new name"
        client.changeName(newName)

        client.clientName shouldBe newName
        with (slot.captured) {
            shouldBeTypeOf<SendNamePacket>()
            name shouldBe newName
            reconnect.shouldBeFalse()
        }
    }

    test("sendChatMessage should send chat command to server") {
        val message = "/em is chatting"
        client.sendChatMessage(10, message)

        with (slot.captured) {
            shouldBeTypeOf<ChatCommandPacket>()
            clientId shouldBe 10
            message shouldBe message
        }
    }
})