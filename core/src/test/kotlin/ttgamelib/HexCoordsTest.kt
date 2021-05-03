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
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

internal class HexCoordsTest : FunSpec({

    test("vertical grid should have same column for same offset x")  {
        val coords1 = HexCoords.createFromOffset(3, 3, vertical = true, offsetOdd = true)
        val coords2 = HexCoords.createFromOffset(3, 5, vertical = true, offsetOdd = true)

        coords1.col shouldBe coords2.col
    }

    test("horizontal grid should have same row for same offset y")  {
        val coords1 = HexCoords.createFromOffset(3, 3, vertical = false, offsetOdd = true)
        val coords2 = HexCoords.createFromOffset(5, 3, vertical = false, offsetOdd = true)

        coords1.row shouldBe coords2.row
    }

    test("oddColumn should have higher Cartesian Y in vertical offsetOdd grid") {
        val evenColumn = HexCoords.createFromOffset(0, 0, vertical = true, offsetOdd = true)
        val oddColumn = HexCoords.createFromOffset(1, 0, vertical = true, offsetOdd = true)

        oddColumn.cartesianY() shouldBeGreaterThan evenColumn.cartesianY()
    }

    test("evenColumn should have higher Cartesian Y in vertical offsetEven grid") {
        val evenColumn = HexCoords.createFromOffset(0, 0, vertical = true, offsetOdd = false)
        val oddColumn = HexCoords.createFromOffset(1, 0, vertical = true, offsetOdd = false)

        evenColumn.cartesianY() shouldBeGreaterThan oddColumn.cartesianY()
    }

    test("odd row should have higher Cartesian x in horizontal offsetOdd grid") {
        val evenRow = HexCoords.createFromOffset(0, 0, vertical = false, offsetOdd = true)
        val oddRow = HexCoords.createFromOffset(0, 1, vertical = false, offsetOdd = true)

        oddRow.cartesianX() shouldBeGreaterThan evenRow.cartesianY()
    }

    test("even row should have higher Cartesian x in horizontal offsetEven grid") {
        val evenRow = HexCoords.createFromOffset(0, 0, vertical = false, offsetOdd = false)
        val oddRow = HexCoords.createFromOffset(0, 1, vertical = false, offsetOdd = false)

        evenRow.cartesianX() shouldBeGreaterThan oddRow.cartesianX()
    }


    test("distance to same hex should be zero") {
        val coords = HexCoords(8, 8, true)

        coords.distance(coords) shouldBe 0
    }

    test("distance in same column in vertical grid should be difference in offsetY") {
        val coords1 = HexCoords.createFromOffset(8, 8, vertical = true, offsetOdd = true)
        val coords2 = HexCoords.createFromOffset(8, 6, vertical = true, offsetOdd = true)

        coords1.distance(coords2) shouldBe 2
        coords2.distance(coords1) shouldBe 2
    }

    test("distance in same row in horizontal grid should be difference in offsetX") {
        val coords1 = HexCoords.createFromOffset(8, 8, vertical = false, offsetOdd = true)
        val coords2 = HexCoords.createFromOffset(6, 8, vertical = false, offsetOdd = true)

        coords1.distance(coords2) shouldBe 2
        coords2.distance(coords1) shouldBe 2
    }

    test("distance should calculate in non-straight line") {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
            .translate(1)
            .translate(1)
            .translate(0)

        coords1.distance(coords2) shouldBe 3
        coords2.distance(coords1) shouldBe 3
    }

    test("direction to same hex defaults to zero") {
        forAll(
            row(true),
            row(false)
        ) { verticalGrid ->
            val coords1 = HexCoords(8, 8, verticalGrid = verticalGrid)
            val coords2 = HexCoords(8, 8, verticalGrid = verticalGrid)
            coords1.degreesTo(coords2) shouldBe 0
        }
    }

    test("direction to hex above should be zero (vertical)") {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
        coords2.translate(V_DIR_N)
        coords2.translate(V_DIR_N)

        coords1.degreesTo(coords2) shouldBe 0
    }

    test("direction to hex above should be zero (horizontal)") {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(coords1)
        coords2.translate(H_DIR_NE)
        coords2.translate(H_DIR_NW)

        coords1.degreesTo(coords2) shouldBe 0
    }

    test("direction to hex below should be 180 (vertical)") {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
            .translate(V_DIR_S)
            .translate(V_DIR_S)

        coords1.degreesTo(coords2) shouldBe 180
    }

    test("direction to hex below should be 180 (horizontal)") {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(coords1)
            .translate(H_DIR_SE)
            .translate(H_DIR_SW)

        coords1.degreesTo(coords2) shouldBe 180
    }

    test("direction to hex to right should be 90 (vertical)") {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
            .translate(V_DIR_NE)
            .translate(V_DIR_SE)

        coords1.degreesTo(coords2) shouldBe 90
    }

    test("direction to hex to right should be 90 (horizontal)") {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(coords1)
            .translate(H_DIR_E)
            .translate(H_DIR_E)

        coords1.degreesTo(coords2) shouldBe 90
    }

    test("direction to hex to left should be 270 (vertical)") {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
            .translate(V_DIR_NW)
            .translate(V_DIR_SW)

        coords1.degreesTo(coords2) shouldBe 270
    }

    test("direction to hex to left should be 270 (horizontal)") {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(coords1)
            .translate(H_DIR_W)
            .translate(H_DIR_W)

        coords1.degreesTo(coords2) shouldBe 270
    }

    test("relative bearing from NE to NW should be 240") {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1).translate(V_DIR_NW)

        coords1.relativeBearingDegrees(V_DIR_NE, coords2) shouldBe 240
    }

    test("relative bearing from NW to NE should be 240") {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1).translate(V_DIR_NE)

        coords1.relativeBearingDegrees(V_DIR_NW, coords2) shouldBe 120
    }

    test("positive rotations should add 60 degrees to bearing") {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(2, center)

        center.degreesTo(testHex) shouldBe 180
    }

    test("rotating past 180 degrees should work correctly") {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(4, center)

        center.degreesTo(testHex) shouldBe 300
    }

    test("zero rotation should not change bearing") {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(0, center)

        center.degreesTo(testHex) shouldBe 60
    }

    test("rotating more than six arcs should be the same as rotating % 6") {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(7, center)

        center.degreesTo(testHex) shouldBe 120
    }

    test("negative rotations should move the bearing anti-clockwise") {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(-2, center)

        center.degreesTo(testHex) shouldBe 300
    }

    test("negative rotations more than six steps should bring the bearing back around") {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(-9, center)

        center.degreesTo(testHex) shouldBe 240
    }
})