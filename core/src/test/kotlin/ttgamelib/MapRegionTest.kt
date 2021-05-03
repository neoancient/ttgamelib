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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MapRegionTest : FunSpec({

    val board = HexBoard(12, 10)

    test("region should include hexes within set distance of the edge") {
        forAll(
            row(MapRegion.NORTH,3, 2, true),
            row(MapRegion.NORTH,3, 3, false),
            row(MapRegion.NORTH,6, 2, true),
            row(MapRegion.NORTH,6, 3, false),

            row(MapRegion.SOUTH, 3, 7, true),
            row(MapRegion.SOUTH, 3, 6, false),
            row(MapRegion.SOUTH, 6, 7, true),
            row(MapRegion.SOUTH, 6, 6, false),

            row(MapRegion.WEST, 2, 3, true),
            row(MapRegion.WEST, 3, 3, false),
            row(MapRegion.WEST, 2, 6, true),
            row(MapRegion.WEST, 3, 6, false),

            row(MapRegion.EAST, 9, 3, true),
            row(MapRegion.EAST, 8, 3, false),
            row(MapRegion.EAST, 9, 6, true),
            row(MapRegion.EAST, 8, 6, false),
        ) { region, col, row, inRegion ->
            region.isInRegion(board.createCoordsFromOffset(col, row), board, 3) shouldBe inRegion
        }
    }

    test("corner regions include hexes in closest half of adjacent edges") {
        forAll(
            row(MapRegion.NORTHEAST,6, 2, true),
            row(MapRegion.NORTHEAST,5, 2, false),
            row(MapRegion.NORTHEAST,9, 4, true),
            row(MapRegion.NORTHEAST,9, 5, false),
            row(MapRegion.NORTHEAST,9, 2, true),
            row(MapRegion.NORTHEAST,8, 3, false),

            row(MapRegion.SOUTHWEST,5, 7, true),
            row(MapRegion.SOUTHWEST,6, 7, false),
            row(MapRegion.SOUTHWEST,2, 5, true),
            row(MapRegion.SOUTHWEST,2, 4, false),
            row(MapRegion.SOUTHWEST,2, 7, true),
            row(MapRegion.SOUTHWEST,3, 6, false),

            row(MapRegion.SOUTHEAST,6, 7, true),
            row(MapRegion.SOUTHEAST,5, 7, false),
            row(MapRegion.SOUTHEAST,9, 5, true),
            row(MapRegion.SOUTHEAST,9, 4, false),
            row(MapRegion.SOUTHEAST,9, 7, true),
            row(MapRegion.SOUTHEAST,8, 6, false),

            row(MapRegion.NORTHWEST,5, 2, true),
            row(MapRegion.NORTHWEST,6, 2, false),
            row(MapRegion.NORTHWEST,2, 4, true),
            row(MapRegion.NORTHWEST,2, 5, false),
            row(MapRegion.NORTHWEST,2, 2, true),
            row(MapRegion.NORTHWEST,3, 3, false),
        ) { region, col, row, inRegion ->
            region.isInRegion(board.createCoordsFromOffset(col, row), board, 3) shouldBe inRegion
        }
    }

    test("center region includes non-edge regions") {
        forAll(
            row(5, 3, true),
            row(5, 2, false),
            row(5, 6, true),
            row(5, 7, false),
            row(3, 5, true),
            row(2, 5, false),
            row(8, 5, true),
            row(9, 5, false),
        ) { col, row, inRegion ->
            MapRegion.CENTER.isInRegion(board.createCoordsFromOffset(col, row), board, 3) shouldBe inRegion
            MapRegion.ANY_EDGE.isInRegion(board.createCoordsFromOffset(col, row), board, 3) shouldNotBe inRegion
        }
    }
})
