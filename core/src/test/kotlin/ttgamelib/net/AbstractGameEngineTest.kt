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
import ttgamelib.*

private class TestEntity : Entity {
    override var unitId: Int = -1
    override var name: String = "Test Entity"
    override var playerId: Int = -1
    override var facing: Int = 0
    override var primaryPosition: Coords? = null
    override var elevation: Int = 0
}

private class TestGame : AbstractGame<HexBoard, TestEntity>() {
    override var board = HexBoard(10, 10)
}

private class TestEngine : AbstractGameEngine<HexBoard, TestEntity, TestGame>("localhost", 1000) {
    override val game = TestGame()
    override suspend fun handleCommand(clientId: Int, command: GameCommand) {
    }
}

internal class AbstractGameEngineTest : FunSpec({
    val connector = TestEngine()
    val clientId = 1
    val player = Player(clientId, "Player1")
    connector.game.addPlayer(player)

    test("UpdatePlayer command sets player values") {
        val newSettings = Player(player.id, player.name, team = 3)
        connector.handle(clientId, UpdatePlayerPacket(newSettings))

        player.team shouldBe newSettings.team
    }
})