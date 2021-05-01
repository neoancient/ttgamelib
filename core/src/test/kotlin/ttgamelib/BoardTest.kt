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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.HashMap
import kotlin.math.sqrt

internal class BoardTest {

    private val defaultHex = Terrain(TerrainType.GRASSLAND, 0, 0)

    @Test
    fun defaultHexFactoryGeneratesCorrectHex() {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords = board.createCoords(5, 5)
        val (terrain, depth, elevation) = board[coords]
        Assertions.assertAll(
            { Assertions.assertEquals(board.defaultHex.terrain, terrain) },
            { Assertions.assertEquals(board.defaultHex.depth, depth) },
            { Assertions.assertEquals(board.defaultHex.elevation, elevation) })
    }

    @Test
    fun testHexFactory() {
        val testBoard = HexBoard(
            10, 10,
            defaultHex = Terrain(TerrainType.ROCKS, 0, 2)
        )
        val (terrain, depth, elevation) = testBoard.getHex(4, 4)
        Assertions.assertAll(
            { Assertions.assertEquals(terrain, TerrainType.ROCKS) },
            { Assertions.assertEquals(depth, 0) },
            { Assertions.assertEquals(elevation, 2) })
    }

    @Test
    fun testTerrainInitialization() {
        val initMap: MutableMap<HexCoords, Terrain> = HashMap()
        initMap[HexCoords(1, 1, true)] = Terrain(TerrainType.ROCKS, 0, 4)
        initMap[HexCoords(1, 2, true)] = Terrain(TerrainType.REEF, 2, 0)
        val testBoard = HexBoard(
            10, 10, initHexes = initMap,
            defaultHex = Terrain(TerrainType.SEA, DEPTH_DEEP_SEA, 0)
        )
        Assertions.assertAll(
            { Assertions.assertEquals(testBoard.getHex(1, 1).terrain, TerrainType.ROCKS) },
            { Assertions.assertEquals(testBoard.getHex(1, 2).terrain, TerrainType.REEF) },
            { Assertions.assertEquals(testBoard.getHex(4, 4).terrain, TerrainType.SEA) })
    }

    @Test
    fun verticalGridHasSameOffsetXForSameColumn() {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = board.createCoords(4, 6)
        Assertions.assertEquals(board.getOffsetCoordX(coords1), board.getOffsetCoordX(coords2))
    }

    @Test
    fun testVerticalGridOffsetYOddOffset() {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = coords1.adjacent(V_DIR_SE)
        Assertions.assertEquals(board.getOffsetCoordY(coords1), board.getOffsetCoordY(coords2))
    }

    @Test
    fun testVerticalGridOffsetYEvenOffset() {
        val board = HexBoard(10, 10, oddOffset = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = coords1.adjacent(V_DIR_NE)
        Assertions.assertEquals(board.getOffsetCoordY(coords1), board.getOffsetCoordY(coords2))
    }

    @Test
    fun horizontalGridHasSameOffsetYForSameRow() {
        val board = HexBoard(10, 10, verticalGrid = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = board.createCoords(6, 4)
        Assertions.assertEquals(board.getOffsetCoordY(coords1), board.getOffsetCoordY(coords2))
    }

    @Test
    fun testHorizontalGridOffsetXOddOffset() {
        val board = HexBoard(10, 10, verticalGrid = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = coords1.adjacent(H_DIR_NE)
        Assertions.assertEquals(board.getOffsetCoordX(coords1), board.getOffsetCoordX(coords2))
    }

    @Test
    fun testHorizontalGridOffsetXEvenOffset() {
        val board = HexBoard(10, 10, verticalGrid = false, oddOffset = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = coords1.adjacent(H_DIR_NW)
        Assertions.assertEquals(board.getOffsetCoordX(coords1), board.getOffsetCoordX(coords2))
    }

    @Test
    fun verticalGridCartesianColumnWidth() {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords1 = board.createCoords(2, 2)
        val coords2 = coords1.adjacent(V_DIR_SE)
        Assertions.assertEquals(
            board.getCartesianX(coords1) + sqrt(3.0) / 2.0,
            board.getCartesianX(coords2), 0.001
        )
    }

    @Test
    fun verticalGridCartesianRowHeight() {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords1 = board.createCoords(2, 2)
        val coords2 = coords1.adjacent(V_DIR_SE)
        Assertions.assertEquals(
            board.getCartesianY(coords1) + 0.5,
            board.getCartesianY(coords2), 0.001
        )
    }

    @Test
    fun horizontalGridCartesianColumnWidth() {
        val board = HexBoard(10, 10, verticalGrid = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(2, 2)
        val coords2 = coords1.adjacent(H_DIR_SE)
        Assertions.assertEquals(
            board.getCartesianX(coords1) + 0.5,
            board.getCartesianX(coords2), 0.001
        )
    }

    @Test
    fun horizontalGridCartesianRowHeight() {
        val board = HexBoard(10, 10, verticalGrid = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(2, 2)
        val coords2 = coords1.adjacent(H_DIR_SE)
        Assertions.assertEquals(
            board.getCartesianY(coords1) + sqrt(3.0) / 2.0,
            board.getCartesianY(coords2), 0.001
        )
    }
}