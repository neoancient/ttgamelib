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

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

// Direction indices with vertical (flat-topped) orientation
public const val V_DIR_N: Int = 0
public const val V_DIR_NE: Int = 1
public const val V_DIR_SE: Int = 2
public const val V_DIR_S: Int = 3
public const val V_DIR_SW: Int = 4
public const val V_DIR_NW: Int = 5

// Direction indices with horizontal (point-topped) orientation
public const val H_DIR_NW: Int = 0
public const val H_DIR_NE: Int = 1
public const val H_DIR_E: Int = 2
public const val H_DIR_SE: Int = 3
public const val H_DIR_SW: Int = 4
public const val H_DIR_W: Int = 5

public interface Coords {
    public val col: Int
    public val row: Int

    /**
     * @return The x coordinate in a rectangular coordinate system
     */
    public fun cartesianX(): Double

    /**
     * @return The y coordinate in a rectangular coordinate system
     */
    public fun cartesianY(): Double

    /**
     * Calculates the range to another set of coordinates
     *
     * @param other  The other coordinates
     * @return       The range in hexes or squares
     */
    public fun distance(other: Coords): Int

    /**
     * Computes the direction from these coordinates to another location. If the coordinates are the
     * same, returns 0 as a default value.
     *
     * @param coords        The other coordinates
     * @return The direction in radians, with zero at the top of the map and proceeding clockwise.
     */
    public fun directionTo(coords: Coords): Double

    /**
     * The direction to another position in degrees, rounded normally. If the coordinates are the same,
     * returns 0.
     *
     * @param coords The other position
     * @return       The direction in degrees, with zero at the top of the map and proceeding clockwise.
     */
    public fun degreesTo(coords: Coords): Int

    /**
     * Computes the direction to another position relative to a given facing rather than relative to the top of
     * the board.
     *
     * @param facing The reference facing
     * @param coords The other position
     * @return The direction in radians, with 0 being the same as the reference facing.
     */
    public fun relativeBearing(facing: Int, coords: Coords): Double

    /**
     * Computes the direction to another position relative to a given facing rather than relative to the top of
     * the board.
     *
     * @param facing The reference facing
     * @param coords The other position
     * @return The direction in degrees, with 0 being the same as the reference facing.
     */
    public fun relativeBearingDegrees(facing: Int, coords: Coords): Int

    /**
     * Finds the adjacent position in a given direction.
     * @param direction The direction to move
     * @return The coordinates of the adjacent position.
     */
    public fun adjacent(direction: Int): Coords = translate(direction)

    /**
     * Translates the position in the given direction.
     * @param direction The direction to move.
     * @return          The position in the direction to translate
     */
    public fun translate(direction: Int): Coords

    /**
     * Translates the position a number of columns and rows
     *
     * @param dCol The number of columns to move the hex
     * @param dRow The number of rows to move the hex
     * @return     The position in the given direction
     */
    public fun translate(dCol: Int, dRow: Int): Coords

    /**
     * Rotates the hex multiple times around a center hex. The amount of rotation depends on the
     * type of grid - 60 degrees for hexes, 45 for squares
     *
     * @param change The number of rotations. Positive for clockwise and negative
     *               for anticlockwise
     * @param center The position at the center of the rotation.
     * @return       The location of the rotated position
     */
    public fun rotate(change: Int, center: Coords): Coords
}

/**
 * Hexagonal grid coordinates with the origin at the top left.
 * The grid uses an axial coordinate system, where there is a 60 degree angle between
 * the axes instead of 90, so that each axis is parallel to a line connecting opposite corners of the
 * hexes. With a vertical orientation (the hexes are aligned in columns) the X axis is rotated clockwise
 * 30 degress so that it runs from the top right toward the bottom left and rows are aligned in a roughly
 * WNW-ESE direction. With a horizontal orientation, the entire grid is rotated 30 degrees anticlockwise
 * from the vertical rotation so that the rows run straight W-E the columns are aligned roughly NNW-SSE.
 *
 * This makes the math easier, though orientation requires some adjustments when calculating absolute
 * angles.
 *
 * Note that there is a third unused axis which is not tracked because two axes are sufficient to
 * determine hex location. This third axis is used in calculating distance between two hexes but can
 * be calculated as needed since the sum of all three coordinates is always zero.
 *
 * See http://www.redblobgames.com/grids/hexagons
 *
 * @property col The X axial coordinate of the hex
 * @property row The Y axial coordinate of the hex
 * @property verticalGrid Whether the grid is aligned by column (flat-topped hexes)
 */
@Serializable
public class HexCoords(
    override val col: Int,
    override val row: Int,
    public val verticalGrid: Boolean) : Coords {

    public constructor(other: HexCoords): this(other.col, other.row, other.verticalGrid)

    /**
     * Converts the internal representation to an offset coordinate system.
     *
     * @param oddOffset If true, the odd-numbered columns (vertical grid) or rows (horizontal)
     *                  are offset by +0.5
     * @return The x coordinate in an offset coordinate system
     */
    public fun offsetX(oddOffset: Boolean): Int {
        return if (verticalGrid) {
            col
        } else {
            if (oddOffset) col + (row - (row and 1)) / 2
            else col + (row + (row and 1)) / 2
        }
    }

    /**
     * Converts the internal representation to an offset coordinate system.
     *
     * @param oddOffset If true, the odd-numbered columns (vertical grid) or rows (horizontal)
     *                  are offset by +0.5
     * @return The y coordinate in an offset coordinate system
     */
    public fun offsetY(oddOffset: Boolean): Int {
        return if (verticalGrid) {
            if (oddOffset) row + (col - (col and 1)) / 2
            else row + (col + (col and 1)) / 2
        } else {
            row
        }
    }

    override fun cartesianX(): Double {
        return if (verticalGrid) {
            sqrt(3.0) * col / 2.0
        } else {
            row / 2.0 + col
        }
    }

    override fun cartesianY(): Double {
        return if (verticalGrid) {
            col / 2.0 + row
        } else {
            sqrt(3.0) * row / 2.0
        }
    }

    override fun distance(other: Coords): Int {
        /*
		 * We use the three coordinate system to calculate distance, using the property
		 * x + y + z = 0 to find the third coordinate. Just as we can count squares moved to
		 * get from A to B by calculating dx + dy, we can count hexes by calculating dx + dy + dz.
		 * Each move to an adjacent hex will leave one coordinate the same but change the others
		 * in opposite directions, so we will need to divide the result by two to get the
		 * actual number of hexes moved.
		 */
        return (abs(col - other.col)
                + abs(col + row - other.col - other.row)
                + abs(row - other.row)) / 2
    }

    override fun directionTo(coords: Coords): Double {
        if (cartesianX() == coords.cartesianX()) {
            return if (cartesianY() < coords.cartesianY()) {
                Math.PI
            } else {
                0.0
            }
        }
        var retVal = atan2(coords.cartesianX() - cartesianX(),
            cartesianY() - coords.cartesianY())
        if (retVal < 0) {
            retVal += Math.PI * 2.0
        }
        return retVal
    }

    override fun degreesTo(coords: Coords): Int = (directionTo(coords) * 180.0 / Math.PI + 0.5).toInt()

    override fun relativeBearing(facing: Int, coords: Coords): Double {
        // The rotation in a horizontal grid applies equally to the grid and the facing
        // so we can get the same results by applying neither.
        var retVal = directionTo(coords) - facing * Math.PI / 3.0
        if (retVal < 0) {
            retVal += Math.PI * 2.0
        }
        if (retVal > Math.PI * 2.0) {
            retVal -= Math.PI * 2.0
        }
        return retVal
    }

    override fun relativeBearingDegrees(facing: Int, coords: Coords): Int {
        var retVal = (directionTo(coords) * 180.0 / Math.PI + 0.5).toInt()
        retVal -= facing * 60
        if (retVal < 0) {
            retVal += 360
        }
        if (retVal >= 360) {
            retVal -= 360
        }
        return retVal
    }


    override fun adjacent(direction: Int): HexCoords = translate(direction)

    override fun translate(direction: Int): HexCoords =
        when (direction) {
            0 -> HexCoords(col, row - 1, verticalGrid)
            1 -> HexCoords(col + 1, row - 1, verticalGrid)
            2 -> HexCoords(col + 1, row, verticalGrid)
            3 -> HexCoords(col, row + 1, verticalGrid)
            4 -> HexCoords(col - 1, row + 1, verticalGrid)
            5 -> HexCoords(col - 1, row, verticalGrid)
            else -> this
        }


    override fun translate(dCol: Int, dRow: Int): HexCoords =
        HexCoords(col + dCol, row + dRow, verticalGrid)

    override fun rotate(change: Int, center: Coords): HexCoords {
        /*
		 * Each rotation that is a multiple of 60 degrees simply involves shifting the positions of the
		 * three cubic coordinates (relative to the center hex) one place and changing the signs.
		 * The cubic coordinates are (col, -col-row, row). A rotation one step clockwise would result
		 * in (-row, -col, col+row). Translating that back into axial coordinates results in (-row, col + row).
		 */
        var delta = change
        while (delta < 0) {
            delta += 6
        }
        // Work with coordinates relative the hex at the center of the rotation
        val dCol = col - center.col
        val dRow = row - center.row
        // Find the new offsets from the center hex
        var rotatedCol = dCol
        var rotatedRow = dRow
        if (delta % 3 == 1) {
            rotatedCol = -dRow
            rotatedRow = dCol + dRow
        } else if (delta % 3 == 2) {
            rotatedCol = -dCol - dRow
            rotatedRow = dCol
        }
        // Rotating 180 degrees uses the coordinates in the same position but swaps the sign
        if (delta % 6 > 2) {
            rotatedCol = -rotatedCol
            rotatedRow = -rotatedRow
        }
        // Update the coordinates
        return HexCoords(center.col + rotatedCol, center.row + rotatedRow, verticalGrid)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is HexCoords
                && col == other.col
                && row == other.row
                && verticalGrid == other.verticalGrid
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + col
        result = prime * result + row
        result = prime * result + if (verticalGrid) 1231 else 1237
        return result
    }


    public companion object {
        /**
         * Constructs axial coordinates from offset coordinates.
         *
         * @param x          The position on an axis increasing from left to right.
         * @param y          The position on an axis increasing from top to bottom.
         * @param vertical   If true, the hexes are arranged in columns with flat-top hexes. If false,
         * the hexes are in rows with pointy-top hexes.
         * @param offsetOdd  If true, the odd columns (vertical) or rows (horizontal) are shifted
         * half a hex in the positive direction on the appropriate axis. Otherwise the even
         * columns are shifted.
         */
        public fun createFromOffset(x: Int, y: Int, vertical: Boolean, offsetOdd: Boolean): HexCoords {
            return if (vertical) {
                if (offsetOdd) {
                    HexCoords(x, y - (x - (x and 1)) / 2, true)
                } else {
                    HexCoords(x, y - (x + (x and 1)) / 2, true)
                }
            } else {
                if (offsetOdd) {
                    HexCoords(x - (y - (y and 1)) / 2, y, false)
                } else {
                    HexCoords(x - (y + (y and 1)) / 2, y, false)
                }
            }
        }
    }

}
