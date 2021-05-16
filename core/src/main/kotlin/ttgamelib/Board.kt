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

public enum class BoardEdge {
    TOP, LEFT, RIGHT, BOTTOM
}

/**
 * The playing area. Tracks dimensions and acts as a container for the individual hexes or squares.
 */
public interface Board {
    /** The number of columns of hexes */
    public val width: Int
    /** The number of rows of hexes */
    public val height: Int

    /**
     * Lookup for the features of the position at the given coordinates
     *
     * @param coords The coordinates of the position
     * @return       The map features at the given coordinates
     */
    public operator fun get(coords: Coords): Terrain

    /**
     * Lookup for the features of the position at the given coordinates
     *
     * @param col    The column index for the position
     * @param row    The row index for the position
     * @return       The map features at the given coordinates
     */
    public fun getTerrain(col: Int, row: Int): Terrain

    /**
     * Find the X coordinate of the position using Cartesian coordinates
     */
    public fun getCartesianX(coords: Coords): Double = coords.cartesianX()

    /**
     * Find the Y coordinate of the position using Cartesian coordinates
     */
    public fun getCartesianY(coords: Coords): Double = coords.cartesianY()

    /**
     * The number of hexes/squares to from [coords] to just off the [edge] of the board.
     */
    public fun distanceToEdge(coords: Coords, edge: BoardEdge): Int
}

/**
 * Implementation of a board using a hex grid
 */
@Serializable
public class HexBoard(
    override val width: Int,
    override val height: Int,
    public val verticalGrid: Boolean = true,
    public val oddOffset: Boolean = true,
    public val defaultHex: Terrain
) : Board {
    private val hexes: MutableMap<HexCoords, Terrain> = HashMap()

    public constructor(width: Int, height: Int, verticalGrid: Boolean = true, oddOffset: Boolean = true,
                       defaultHex: Terrain = Terrain(TerrainType.SEA, DEPTH_DEEP_SEA, 0),
                       initHexes: Map<out HexCoords, Terrain>):
            this(width, height, verticalGrid, oddOffset, defaultHex) {
        hexes.putAll(initHexes)
    }

    /**
     * For the convenience of unit tests
     */
    internal constructor(width: Int, height: Int) :
            this(width, height, defaultHex = Terrain(TerrainType.GRASSLAND, 0, 0))

    override operator fun get(coords: Coords): Terrain = hexes[coords] ?: defaultHex

    override fun getTerrain(col: Int, row: Int): Terrain = get(createCoords(col, row))

    override fun distanceToEdge(coords: Coords, edge: BoardEdge): Int {
        return when (edge) {
            BoardEdge.TOP -> getOffsetCoordY(coords as HexCoords) + 1
            BoardEdge.LEFT -> getOffsetCoordX(coords as HexCoords) + 1
            BoardEdge.RIGHT -> width - getOffsetCoordX(coords as HexCoords)
            BoardEdge.BOTTOM -> height - getOffsetCoordY(coords as HexCoords)
        }
    }

    /**
     * Find the X coordinate of the position using offset coordinates
     */
    public fun getOffsetCoordX(coords: HexCoords): Int = coords.offsetX(oddOffset)

    /**
     * Find the Y coordinate of the position using offset coordinates
     */
    public fun getOffsetCoordY(coords: HexCoords): Int = coords.offsetY(oddOffset)

    /**
     * Factory method to create a set of axial coordinates for this board
     *
     * @param col The hex column
     * @param row The hex row
     * @return    The coordinates for the hex
     */
    public fun createCoords(col: Int, row: Int): HexCoords = HexCoords(col, row, verticalGrid)

    /**
     * Factory method to create a set of axial coordinates for this board
     *
     * @param col The hex column
     * @param row The hex row
     * @return    The coordinates for the hex
     */
    public fun createCoordsFromOffset(col: Int, row: Int): HexCoords = HexCoords.createFromOffset(col, row, verticalGrid, oddOffset)

    /**
     * Returns a copy of the hex data
     */
    public fun exportHexes(): MutableMap<HexCoords, Terrain> = HashMap(hexes)
}
