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

internal class HexCoordsTest {

    @Test
    fun sameColumnForSameXVertical() {
        val coords1 = HexCoords.createFromOffset(3, 3, vertical = true, offsetOdd = true)
        val coords2 = HexCoords.createFromOffset(3, 5, vertical = true, offsetOdd = true)
        Assertions.assertEquals(coords1.col, coords2.col)
    }

    @Test
    fun sameRowForSameYHorizontal() {
        val coords1 = HexCoords.createFromOffset(3, 3, vertical = false, offsetOdd = true)
        val coords2 = HexCoords.createFromOffset(5, 3, vertical = false, offsetOdd = true)
        Assertions.assertEquals(coords1.row, coords2.row)
    }

    @Test
    fun testOddColumnOffset() {
        val evenColumn = HexCoords.createFromOffset(0, 0, vertical = true, offsetOdd = true)
        val oddColumn = HexCoords.createFromOffset(1, 0, vertical = true, offsetOdd = true)
        Assertions.assertTrue(evenColumn.cartesianY() < oddColumn.cartesianY())
    }

    @Test
    fun testEvenColumnOffset() {
        val evenColumn = HexCoords.createFromOffset(0, 0, vertical = true, offsetOdd = false)
        val oddColumn = HexCoords.createFromOffset(1, 0, vertical = true, offsetOdd = false)
        Assertions.assertTrue(evenColumn.cartesianY() > oddColumn.cartesianY())
    }

    @Test
    fun testOddRowOffset() {
        val evenRow = HexCoords.createFromOffset(0, 0, vertical = false, offsetOdd = true)
        val oddRow = HexCoords.createFromOffset(0, 1, vertical = false, offsetOdd = true)
        Assertions.assertTrue(evenRow.cartesianX() < oddRow.cartesianX())
    }

    @Test
    fun testEvenRowOffset() {
        val evenRow = HexCoords.createFromOffset(0, 0, vertical = false, offsetOdd = false)
        val oddRow = HexCoords.createFromOffset(0, 1, vertical = false, offsetOdd = false)
        Assertions.assertTrue(evenRow.cartesianX() > oddRow.cartesianX())
    }


    @Test
    fun distanceToSameHexIsZero() {
        val coords = HexCoords(8, 8, true)
        Assertions.assertEquals(coords.distance(coords), 0)
    }

    @Test
    fun distanceInSameColumnVerticalIsDifferenceInY() {
        val coords1 = HexCoords.createFromOffset(8, 8, vertical = true, offsetOdd = true)
        val coords2 = HexCoords.createFromOffset(8, 6, vertical = true, offsetOdd = true)
        Assertions.assertAll(
            { Assertions.assertEquals(coords1.distance(coords2), 2) },
            { Assertions.assertEquals(coords2.distance(coords1), 2) }
        )
    }

    @Test
    fun distanceInSameRowHorizontalIsDifferenceInX() {
        val coords1 = HexCoords.createFromOffset(8, 8, vertical = false, offsetOdd = true)
        val coords2 = HexCoords.createFromOffset(6, 8, vertical = false, offsetOdd = true)
        Assertions.assertAll(
            { Assertions.assertEquals(coords1.distance(coords2), 2) },
            { Assertions.assertEquals(coords2.distance(coords1), 2) }
        )
    }

    @Test
    fun testDistanceNonStraightLine() {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
            .translate(1)
            .translate(1)
            .translate(0)
        Assertions.assertAll(
            { Assertions.assertEquals(coords1.distance(coords2), 3) },
            { Assertions.assertEquals(coords2.distance(coords1), 3) }
        )
    }

    @Test
    fun directionFromSameHexIsZeroDegreesWithVerticalOrientation() {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(8, 8, true)
        Assertions.assertEquals(coords1.degreesTo(coords2), 0)
    }

    @Test
    fun directionFromSameHexIsZeroDegreesWithHorizontalOrientation() {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(8, 8, false)
        Assertions.assertEquals(coords1.degreesTo(coords2), 0)
    }

    @Test
    fun directionToHexAboveIsZeroDegreesWithVerticalOrientation() {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
        coords2.translate(V_DIR_N)
        coords2.translate(V_DIR_N)
        Assertions.assertEquals(coords1.degreesTo(coords2), 0)
    }

    @Test
    fun directionToHexAboveIsZeroDegreesWithHorizontalOrientation() {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(coords1)
        coords2.translate(H_DIR_NE)
        coords2.translate(H_DIR_NW)
        Assertions.assertEquals(coords1.degreesTo(coords2), 0)
    }

    @Test
    fun degreesToHexBelowIs180DegreesWithVerticalOrientation() {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
            .translate(V_DIR_S)
            .translate(V_DIR_S)
        Assertions.assertEquals(coords1.degreesTo(coords2), 180)
    }

    @Test
    fun degreesToHexBelowIs180DegreesWithHorizontalOrientation() {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(coords1)
            .translate(H_DIR_SE)
            .translate(H_DIR_SW)
        Assertions.assertEquals(coords1.degreesTo(coords2), 180)
    }

    @Test
    fun degreesToHexToRightIs90DegreesWithVerticalOrientation() {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
            .translate(V_DIR_NE)
            .translate(V_DIR_SE)
        Assertions.assertEquals(coords1.degreesTo(coords2), 90)
    }

    @Test
    fun degreesToHexToRightIs90DegreesWithHorizontalOrientation() {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(coords1)
            .translate(H_DIR_E)
            .translate(H_DIR_E)
        Assertions.assertEquals(coords1.degreesTo(coords2), 90)
    }

    @Test
    fun degreesToHexToLeftIs270DegreesWithVerticalOrientation() {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1)
            .translate(V_DIR_NW)
            .translate(V_DIR_SW)
        Assertions.assertEquals(coords1.degreesTo(coords2), 270)
    }

    @Test
    fun degreesToHexToLeftIs270DegreesWithHorizontalOrientation() {
        val coords1 = HexCoords(8, 8, false)
        val coords2 = HexCoords(coords1)
            .translate(H_DIR_W)
            .translate(H_DIR_W)
        Assertions.assertEquals(coords1.degreesTo(coords2), 270)
    }

    @Test
    fun relativeBearingFromNEToNWIs240() {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1).translate(V_DIR_NW)
        Assertions.assertEquals(coords1.relativeBearingDegrees(V_DIR_NE, coords2), 240)
    }

    @Test
    fun relativeBearingFromNWToNEIs120() {
        val coords1 = HexCoords(8, 8, true)
        val coords2 = HexCoords(coords1).translate(V_DIR_NE)
        Assertions.assertEquals(coords1.relativeBearingDegrees(V_DIR_NW, coords2), 120)
    }

    @Test
    fun rotatePositiveSteps() {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(2, center)
        Assertions.assertEquals(center.degreesTo(testHex), 180)
    }

    @Test
    fun rotatePositiveStepsPast180() {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(4, center)
        Assertions.assertEquals(300, center.degreesTo(testHex))
    }

    @Test
    fun rotateZero() {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(0, center)
        Assertions.assertEquals(center.degreesTo(testHex), 60)
    }

    @Test
    fun rotateMoreThanSixSteps() {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(7, center)
        Assertions.assertEquals(center.degreesTo(testHex), 120)
    }

    @Test
    fun rotateNegativeSteps() {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(-2, center)
        Assertions.assertEquals(center.degreesTo(testHex), 300)
    }

    @Test
    fun rotateMoreThanSixNegativeSteps() {
        val center = HexCoords(8, 8, true)
        val testHex = HexCoords(center)
            .translate(1)
            .translate(1)
            .rotate(-9, center)
        Assertions.assertEquals(center.degreesTo(testHex), 240)
    }

}