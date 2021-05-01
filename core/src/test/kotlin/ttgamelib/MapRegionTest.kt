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
import org.junit.jupiter.api.Test

internal class MapRegionTest {
    @Test
    fun testNorthRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.NORTH

        val evenColIn = board.createCoordsFromOffset(3, 2)
        val evenColOut = board.createCoordsFromOffset(3, 3)
        val oddColIn = board.createCoordsFromOffset(6, 2)
        val oddColOut = board.createCoordsFromOffset(6, 3)

        assertAll(
            { assertTrue(region.isInRegion(evenColIn, board, 3)) },
            { assertTrue(region.isInRegion(oddColIn, board, 3)) },
            { assertFalse(region.isInRegion(evenColOut, board, 3)) },
            { assertFalse(region.isInRegion(oddColOut, board, 3)) },
        )
    }

    @Test
    fun testSouthRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.SOUTH

        val evenColIn = board.createCoordsFromOffset(3, 7)
        val evenColOut = board.createCoordsFromOffset(3, 6)
        val oddColIn = board.createCoordsFromOffset(6, 7)
        val oddColOut = board.createCoordsFromOffset(6, 6)

        assertAll(
            { assertTrue(region.isInRegion(evenColIn, board, 3)) },
            { assertTrue(region.isInRegion(oddColIn, board, 3)) },
            { assertFalse(region.isInRegion(evenColOut, board, 3)) },
            { assertFalse(region.isInRegion(oddColOut, board, 3)) },
        )
    }

    @Test
    fun testWestRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.WEST

        val evenRowIn = board.createCoordsFromOffset(2, 3)
        val evenRowOut = board.createCoordsFromOffset(3, 3)
        val oddRowIn = board.createCoordsFromOffset(2, 6)
        val oddRowOut = board.createCoordsFromOffset(3, 6)

        assertAll(
            { assertTrue(region.isInRegion(evenRowIn, board, 3)) },
            { assertTrue(region.isInRegion(oddRowIn, board, 3)) },
            { assertFalse(region.isInRegion(evenRowOut, board, 3)) },
            { assertFalse(region.isInRegion(oddRowOut, board, 3)) },
        )
    }

    @Test
    fun testEastRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.EAST

        val evenRowIn = board.createCoordsFromOffset(9, 3)
        val evenRowOut = board.createCoordsFromOffset(8, 3)
        val oddRowIn = board.createCoordsFromOffset(9, 6)
        val oddRowOut = board.createCoordsFromOffset(8, 6)

        assertAll(
            { assertTrue(region.isInRegion(evenRowIn, board, 3)) },
            { assertTrue(region.isInRegion(oddRowIn, board, 3)) },
            { assertFalse(region.isInRegion(evenRowOut, board, 3)) },
            { assertFalse(region.isInRegion(oddRowOut, board, 3)) },
        )
    }

    @Test
    fun testNorthEastRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.NORTHEAST

        val topRowIn = board.createCoordsFromOffset(6, 2)
        val topRowOut = board.createCoordsFromOffset(5, 2)
        val rightColIn = board.createCoordsFromOffset(9, 4)
        val rightColOut = board.createCoordsFromOffset(9, 5)
        val cornerIn = board.createCoordsFromOffset(9, 2)
        val cornerOut = board.createCoordsFromOffset(8, 3)

        assertAll(
            { assertTrue(region.isInRegion(topRowIn, board, 3)) },
            { assertTrue(region.isInRegion(rightColIn, board, 3)) },
            { assertTrue(region.isInRegion(cornerIn, board, 3)) },
            { assertFalse(region.isInRegion(topRowOut, board, 3)) },
            { assertFalse(region.isInRegion(rightColOut, board, 3)) },
            { assertFalse(region.isInRegion(cornerOut, board, 3)) },
        )
    }

    @Test
    fun testSouthWestRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.SOUTHWEST

        val bottomRowIn = board.createCoordsFromOffset(5, 7)
        val bottomRowOut = board.createCoordsFromOffset(6, 7)
        val leftColIn = board.createCoordsFromOffset(2, 5)
        val leftColOut = board.createCoordsFromOffset(2, 4)
        val cornerIn = board.createCoordsFromOffset(2, 7)
        val cornerOut = board.createCoordsFromOffset(3, 6)

        assertAll(
            { assertTrue(region.isInRegion(bottomRowIn, board, 3)) },
            { assertTrue(region.isInRegion(leftColIn, board, 3)) },
            { assertTrue(region.isInRegion(cornerIn, board, 3)) },
            { assertFalse(region.isInRegion(bottomRowOut, board, 3)) },
            { assertFalse(region.isInRegion(leftColOut, board, 3)) },
            { assertFalse(region.isInRegion(cornerOut, board, 3)) },
        )
    }

    @Test
    fun testSouthEastRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.SOUTHEAST

        val bottomRowIn = board.createCoordsFromOffset(6, 7)
        val bottomRowOut = board.createCoordsFromOffset(5, 7)
        val rightColIn = board.createCoordsFromOffset(9, 5)
        val rightColOut = board.createCoordsFromOffset(9, 4)
        val cornerIn = board.createCoordsFromOffset(9, 7)
        val cornerOut = board.createCoordsFromOffset(8, 6)

        assertAll(
            { assertTrue(region.isInRegion(bottomRowIn, board, 3)) },
            { assertTrue(region.isInRegion(rightColIn, board, 3)) },
            { assertTrue(region.isInRegion(cornerIn, board, 3)) },
            { assertFalse(region.isInRegion(bottomRowOut, board, 3)) },
            { assertFalse(region.isInRegion(rightColOut, board, 3)) },
            { assertFalse(region.isInRegion(cornerOut, board, 3)) },
        )
    }

    @Test
    fun testNorthWestRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.NORTHWEST

        val topRowIn = board.createCoordsFromOffset(5, 2)
        val topRowOut = board.createCoordsFromOffset(6, 2)
        val leftColIn = board.createCoordsFromOffset(2, 4)
        val leftColOut = board.createCoordsFromOffset(2, 5)
        val cornerIn = board.createCoordsFromOffset(2, 2)
        val cornerOut = board.createCoordsFromOffset(3, 3)

        assertAll(
            { assertTrue(region.isInRegion(topRowIn, board, 3)) },
            { assertTrue(region.isInRegion(leftColIn, board, 3)) },
            { assertTrue(region.isInRegion(cornerIn, board, 3)) },
            { assertFalse(region.isInRegion(topRowOut, board, 3)) },
            { assertFalse(region.isInRegion(leftColOut, board, 3)) },
            { assertFalse(region.isInRegion(cornerOut, board, 3)) },
        )
    }

    @Test
    fun testCenterRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.CENTER

        val topRowIn = board.createCoordsFromOffset(5, 3)
        val topRowOut = board.createCoordsFromOffset(5, 2)
        val bottomRowIn = board.createCoordsFromOffset(5, 6)
        val bottomRowOut = board.createCoordsFromOffset(5, 7)
        val leftColIn = board.createCoordsFromOffset(3, 5)
        val leftColOut = board.createCoordsFromOffset(2, 5)
        val rightColIn = board.createCoordsFromOffset(8, 5)
        val rightColOut = board.createCoordsFromOffset(9, 5)

        assertAll(
            { assertTrue(region.isInRegion(topRowIn, board, 3)) },
            { assertTrue(region.isInRegion(bottomRowIn, board, 3)) },
            { assertTrue(region.isInRegion(leftColIn, board, 3)) },
            { assertTrue(region.isInRegion(rightColIn, board, 3)) },
            { assertFalse(region.isInRegion(topRowOut, board, 3)) },
            { assertFalse(region.isInRegion(bottomRowOut, board, 3)) },
            { assertFalse(region.isInRegion(leftColOut, board, 3)) },
            { assertFalse(region.isInRegion(rightColOut, board, 3)) },
        )
    }

    @Test
    fun testAnyEdgeRegion() {
        val board = HexBoard(12, 10)
        val region = MapRegion.ANY_EDGE

        val topRowIn = board.createCoordsFromOffset(5, 2)
        val topRowOut = board.createCoordsFromOffset(5, 3)
        val bottomRowIn = board.createCoordsFromOffset(5, 7)
        val bottomRowOut = board.createCoordsFromOffset(5, 6)
        val leftColIn = board.createCoordsFromOffset(2, 5)
        val leftColOut = board.createCoordsFromOffset(3, 5)
        val rightColIn = board.createCoordsFromOffset(9, 5)
        val rightColOut = board.createCoordsFromOffset(8, 5)

        assertAll(
            { assertTrue(region.isInRegion(topRowIn, board, 3)) },
            { assertTrue(region.isInRegion(bottomRowIn, board, 3)) },
            { assertTrue(region.isInRegion(leftColIn, board, 3)) },
            { assertTrue(region.isInRegion(rightColIn, board, 3)) },
            { assertFalse(region.isInRegion(topRowOut, board, 3)) },
            { assertFalse(region.isInRegion(bottomRowOut, board, 3)) },
            { assertFalse(region.isInRegion(leftColOut, board, 3)) },
            { assertFalse(region.isInRegion(rightColOut, board, 3)) },
        )
    }
}
