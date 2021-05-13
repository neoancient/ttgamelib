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
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.spyk
import ttgamelib.*

private class TestGame : AbstractGame<HexBoard, TestEntity>() {
    override var board = HexBoard(10, 10)
}

private class TestEngine : AbstractGameEngine<HexBoard, TestEntity, TestGame>("localhost", 1000) {
    override val game = TestGame()
    override suspend fun handleCommand(clientId: Int, command: GameCommand) {
    }
}

internal class AbstractGameEngineTest : FunSpec({
    val engine = spyk(TestEngine())
    val clientId = 1
    val player = Player(clientId, "Player1")
    engine.game.addPlayer(player)

    test("UpdatePlayer command sets player values") {
        val newSettings = Player(player.id, player.name, team = 3)
        engine.handle(clientId, UpdatePlayerPacket(newSettings))

        player.team shouldBe newSettings.team
    }

    test("PlayerReadyPacket should toggle ready status") {
        engine.game.playerReady(clientId, false)
        engine.handle(clientId, PlayerReadyPacket(clientId, true))

        engine.game.getPlayer(clientId)?.ready shouldBe true
    }

    test("AddEntityPacket should add entity to player's force") {
        val entity = TestEntity()
        engine.handle(clientId, AddEntityPacket(entity))

        entity.playerId shouldBe clientId
        engine.game.getEntity(entity.entityId) shouldBe entity
    }

    test("RemoveEntityPacket should remove entity from game") {
        val entity = TestEntity()
        engine.game.addUnit(entity, clientId)

        engine.handle(clientId, RemoveEntityPacket(entity.entityId))

        engine.game.getEntity(entity.entityId) shouldBe null
    }

    test("SetBoardPacket should change board") {
        val newBoard = HexBoard(25, 30)

        engine.handle(clientId, SetBoardPacket(newBoard))

        with (engine.game.board) {
            width shouldBe 25
            height shouldBe 30
        }
    }

    test("SetWeatherPacket should change weather") {
        val newWeather = Weather(2, WindStrength.STRONG_WIND)

        engine.handle(clientId, SetWeatherPacket(newWeather))

        with (engine.game.weather) {
            windDirection shouldBe 2
            windStrength shouldBe WindStrength.STRONG_WIND
        }
    }

    test("GameCommandPacket should process command") {
        val command = object : GameCommand {}

        engine.handle(clientId, GameCommandPacket(command))

        coVerify {
            engine.handleCommand(clientId, command)
        }
    }
})