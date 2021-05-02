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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.*

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

internal class AbstractGameTest {
    private lateinit var game: Game<HexBoard, TestEntity>

    @BeforeEach
    private fun createGame() {
        game = TestGame()
    }

    @Test
    fun findPlayerByID() {
        val playerName = "Test Player"
        val p = game.newPlayer(0, playerName)
        assertEquals(playerName, game.getPlayer(p.id)?.name)
    }

    @Test
    fun allPlayersAdded() {
        val p1 = game.newPlayer(0, "Player 1")
        val p2 = game.newPlayer(1, "Player 2")
        val all: Collection<Player?> = game.allPlayers()
        assertAll(
            { assertEquals(2, all.size) },
            { all.contains(p1) },
            { all.contains(p2) })
    }

    @Test
    fun findUnitById() {
        val unit = TestEntity()
        val id = game.addUnit(unit, 0)
        assertEquals(game.getUnit(id), unit)
    }

    @Test
    fun allUnitsAdded() {
        val unit1 = TestEntity()
        val unit2 = TestEntity().apply {
            name = "Test Entity 2"
        }
        game.addUnit(unit1, 0)
        game.addUnit(unit2, 0)
        val all = game.allUnits()
        assertAll(
            { assertEquals(2, all.size) },
            { all.contains(unit1) },
            { all.contains(unit2) })
    }

    @Test
    fun addPlayerNotifiesListener() {
        val listener = mockk<GameListener>(relaxUnitFun = true)
        game.addListener(listener)
        val p = game.newPlayer(0, "Test Player")
        verify {
            listener.playerAdded(p.id)
        }
    }

    @Test
    fun removePlayerNotifiesListener() {
        val listener = mockk<GameListener>(relaxUnitFun = true)
        game.addListener(listener)
        val p = game.newPlayer(0, "Test Player")
        game.removePlayer(p.id)
        verify {
            listener.playerRemoved(p.id)
        }
    }

    @Test
    fun canRemovePlayer() {
        val p1 = game.newPlayer(0, "Player 1")
        game.newPlayer(1, "Player 2")
        game.removePlayer(p1.id)
        assertAll(
            { assertEquals(1, game.allPlayers().size) },
            { assertFalse(game.allPlayers().contains(p1)) })
    }
}