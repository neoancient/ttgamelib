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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.*

internal class AbstractGameTest : FunSpec({
    lateinit var game: Game<HexBoard, TestEntity>

    beforeAny {
        game = TestGame()
    }

    test("findPlayerByID should find the player") {
        val playerName = "Test Player"
        val p = game.newPlayer(0, playerName)

        game.getPlayer(p.id)?.name shouldBe playerName
    }

    test("newPlayer should add player to the game") {
        val p1 = game.newPlayer(0, "Player 1")
        val p2 = game.newPlayer(1, "Player 2")
        val all: Collection<Player?> = game.allPlayers()

        all.shouldContainExactlyInAnyOrder(p1, p2)
    }

    test("unit lookup should find unit by id") {
        val unit = TestEntity()
        val id = game.addUnit(unit, 0)

        game.getEntity(id) shouldBe unit
    }

    test("addUnit should add units to the game") {
        val unit1 = TestEntity()
        val unit2 = TestEntity().apply {
            name = "Test Entity 2"
        }
        game.addUnit(unit1, 0)
        game.addUnit(unit2, 0)

        game.allUnits().shouldContainExactlyInAnyOrder(unit1, unit2)
    }

    test("addPlayer should notify listener") {
        val listener = mockk<GameListener>(relaxUnitFun = true)
        game.addListener(listener)
        val p = game.newPlayer(0, "Test Player")

        verify {
            listener.playerAdded(p.id)
        }
    }

    test("removePlayer should notify listener") {
        val listener = mockk<GameListener>(relaxUnitFun = true)
        game.addListener(listener)
        val p = game.newPlayer(0, "Test Player")
        game.removePlayer(p.id)

        verify {
            listener.playerRemoved(p.id)
        }
    }

    test("should be able to remove player from the game") {
        val p1 = game.newPlayer(0, "Player 1")
        val p2 = game.newPlayer(1, "Player 2")
        game.removePlayer(p1.id)

        game.allPlayers().shouldContainExactly(p2)
    }
})