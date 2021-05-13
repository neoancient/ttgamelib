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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.*
import ttgamelib.net.*

private class TestController : AbstractClientController<HexBoard, TestEntity, TestGame>("test") {
    override var game = TestGame()

    override suspend fun handle(command: GameCommand) {
    }
}

internal class AbstractClientControllerTest : FunSpec({
    val controller = spyk(TestController())
    val slot = slot<Packet>()
    coEvery {
        controller.send(capture(slot))
    } just Runs
    val clientListener = mockk<ClientListener>(relaxUnitFun = true)
    controller.addConnectionListener(clientListener)
    val gameListener = mockk<GameListener>(relaxUnitFun = true)
    controller.game.addListener(gameListener)

    test("RequestNamePacket should be answered with client name") {
        controller.handle(RequestNamePacket)

        with (slot.captured) {
            shouldBeTypeOf<SendNamePacket>()
            name shouldBe "test"
            reconnect.shouldBeFalse()
        }
    }

    test("SuggestNamePacket should notify listeners") {
        val packet = SuggestNamePacket("alternate", setOf("taken"), false)
        controller.handle(packet)

        coVerify {
            clientListener.nameTaken(controller, packet.name, packet.taken, packet.disconnected)
        }
    }

    test("InitClientPacket should set client id and notify listeners") {
        val packet = InitClientPacket(8)
        controller.handle(packet)

        controller.clientId shouldBe packet.clientId
        coVerify {
            clientListener.clientConnected(controller)
        }
    }

    test("SendGamePacket should replace game and move game listeners") {
        controller.game.addListener(gameListener)
        val newGame = TestGame()
        val newPlayer = newGame.newPlayer(6, "new player")
        val oldGame = controller.game

        controller.handle(SendGamePacket(newGame))
        controller.game.newPlayer(1, "testPlayer")

        controller.game shouldBe newGame
        controller.game.getPlayer(6) shouldBe newPlayer
        verify {
            gameListener.playerAdded(1)
            clientListener.gameChanged(oldGame, newGame)
        }
    }

    test("AddPlayerPacket should add player to game") {
        val newPlayer = Player(4, "added player")
        controller.handle(AddPlayerPacket(newPlayer))

        controller.game.getPlayer(4) shouldBe newPlayer
    }

    test("RemovePlayerPacket should remove player from the game") {
        val player = controller.game.newPlayer(10, "extra")

        controller.handle(RemovePlayerPacket(player.id))

        controller.game.getPlayer(player.id) shouldBe null
    }

    test("UpdatePlayerPacket should update player settings") {
        val player = controller.game.getPlayer(controller.clientId) ?:
            controller.game.newPlayer(controller.clientId, "testPlayer")
        val changed = Player(player.id, player.name, 2, PlayerColor.RED)

        controller.handle(UpdatePlayerPacket(changed))

        player.team shouldBe 2
        player.color shouldBe PlayerColor.RED
    }

    test("PlayerDisconnectedPacket should update player status") {
        val disconnect = controller.game.newPlayer(2, "badConnection")
        val reconnect = controller.game.newPlayer(3, "betterConnection").also {
            it.disconnected = true
        }

        controller.handle(PlayerDisconnectionPacket(disconnect.id, true))
        controller.handle(PlayerDisconnectionPacket(reconnect.id, false))

        controller.game.getPlayer(disconnect.id)?.disconnected shouldBe true
        controller.game.getPlayer(reconnect.id)?.disconnected shouldBe false
    }

    test("AddEntityPacket should add entity to game") {
        val entity = TestEntity().apply {
            entityId = 1
            playerId = controller.clientId
        }

        controller.handle(AddEntityPacket(entity))

        controller.game.getEntity(entity.entityId) shouldBe entity
    }

    test("RemoveEntityPacket should remove entity from game") {
        controller.handle(RemoveEntityPacket(1))

        controller.game.getEntity(1) shouldBe null
    }

    test("SetBoardPacket should update game board") {
        val newBoard = HexBoard(30, 10)

        controller.handle(SetBoardPacket(newBoard))

        with (controller.game.board) {
            width shouldBe newBoard.width
            height shouldBe newBoard.height
        }
    }

    test("SetWeatherPacket should change weather") {
        val newWeather = Weather(3, WindStrength.GALE)

        controller.handle(SetWeatherPacket(newWeather))

        with (controller.game.weather) {
            windDirection shouldBe 3
            windStrength shouldBe WindStrength.GALE
        }
        coVerify {
            gameListener.weatherChanged()
        }
    }

    test("GameCommandPacket should be sent to command handler") {
        val command = object : GameCommand {}

        controller.handle(GameCommandPacket(command))

        coVerify {
            controller.handle(command)
        }
    }
})