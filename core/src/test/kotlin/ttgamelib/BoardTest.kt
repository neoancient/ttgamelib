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
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import java.util.HashMap
import kotlin.math.sqrt

internal class BoardTest : FunSpec({

    val defaultHex = Terrain(TerrainType.GRASSLAND, 0, 0)

    test("default hex factory should generate correct hex") {
        val testBoard = HexBoard(
            10, 10,
            defaultHex = Terrain(TerrainType.ROCKS, 0, 2)
        )
        val (terrain, depth, elevation) = testBoard.getTerrain(4, 4)

        terrain shouldBe TerrainType.ROCKS
        depth shouldBe 0
        elevation shouldBe 2
    }

    test("hex lookup should return initialized values") {
        val initMap: MutableMap<HexCoords, Terrain> = HashMap()
        initMap[HexCoords(1, 1, true)] = Terrain(TerrainType.ROCKS, 0, 4)
        initMap[HexCoords(1, 2, true)] = Terrain(TerrainType.REEF, 2, 0)
        val testBoard = HexBoard(
            10, 10, initHexes = initMap,
            defaultHex = Terrain(TerrainType.SEA, DEPTH_DEEP_SEA, 0)
        )

        forAll(
            row(1, 1, TerrainType.ROCKS),
            row(1, 2, TerrainType.REEF),
            row(4, 4, TerrainType.SEA)
        ) { col, row, terrain ->
            testBoard.getTerrain(col, row).terrain shouldBe terrain
        }
    }

    test("vertical grid should have same offsetX for same column") {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = board.createCoords(4, 6)

        board.getOffsetCoordX(coords1) shouldBe board.getOffsetCoordX(coords2)
    }

    test("vertical grid with odd offset should place odd columns lower") {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = coords1.adjacent(V_DIR_SE)

        board.getOffsetCoordY(coords1) shouldBe board.getOffsetCoordY(coords2)
    }

    test("vertical grid with odd offset should place odd columns higher") {
        val board = HexBoard(10, 10, oddOffset = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = coords1.adjacent(V_DIR_NE)

        board.getOffsetCoordY(coords1) shouldBe board.getOffsetCoordY(coords2)
    }

    test("vertical grid should have same offsetY for same row") {
        val board = HexBoard(10, 10, verticalGrid = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = board.createCoords(6, 4)

        board.getOffsetCoordY(coords1) shouldBe board.getOffsetCoordY(coords2)
    }

    test("horizontal grid with odd offset should place odd rows to the right") {
        val board = HexBoard(10, 10, verticalGrid = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = coords1.adjacent(H_DIR_NE)

        board.getOffsetCoordX(coords1) shouldBe board.getOffsetCoordX(coords2)
    }

    test("horizontal grid with even offset should place odd rows to the left") {
        val board = HexBoard(10, 10, verticalGrid = false, oddOffset = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(4, 4)
        val coords2 = coords1.adjacent(H_DIR_NW)

        board.getOffsetCoordX(coords1) shouldBe board.getOffsetCoordX(coords2)
    }

    test("vertical grid column width should be height * sqrt(3) / 2") {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords1 = board.createCoords(2, 2)
        val coords2 = coords1.adjacent(V_DIR_SE)

        board.getCartesianX(coords2)
            .shouldBe((board.getCartesianX(coords1) + sqrt(3.0) / 2.0) plusOrMinus 0.001)
    }

    test("vertical grid adjacent columns should be offset by 0.5") {
        val board = HexBoard(10, 10, defaultHex = defaultHex)
        val coords1 = board.createCoords(2, 2)
        val coords2 = coords1.adjacent(V_DIR_SE)

        board.getCartesianY(coords2).shouldBe((board.getCartesianY(coords1) + 0.5) plusOrMinus 0.001)
    }

    test("horizontal grid adjacent rows should be offset by 0.5") {
        val board = HexBoard(10, 10, verticalGrid = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(2, 2)
        val coords2 = coords1.adjacent(H_DIR_SE)

        board.getCartesianX(coords2).shouldBe((board.getCartesianX(coords1) + 0.5) plusOrMinus 0.001)
    }

    test("horizontal grid row height should be width * sqrt(3) / 2") {
        val board = HexBoard(10, 10, verticalGrid = false, defaultHex = defaultHex)
        val coords1 = board.createCoords(2, 2)
        val coords2 = coords1.adjacent(H_DIR_SE)

        board.getCartesianY(coords2)
            .shouldBe((board.getCartesianY(coords1) + sqrt(3.0) / 2.0) plusOrMinus 0.001)
    }
})