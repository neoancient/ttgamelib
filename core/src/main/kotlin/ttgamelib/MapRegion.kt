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

/**
 * An area of the map that can be used for things like deployment zones and victory conditions
 */
@Serializable
public sealed class MapRegion {

    /**
     * Determines whether the given [coords] are within the zone on the [board]. The
     * [depth] determines the size of the region.
     */
    public abstract fun isInRegion(coords: Coords, board: Board, depth: Int = 3): Boolean

    /**
     * One or more rows of hexes at the top of the map
     */
    @Serializable
    public object NORTH : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean =
            board.distanceToEdge(coords, BoardEdge.TOP) <= depth
    }

    /**
     * One or more rows of hexes at the bottom of the map
     */
    @Serializable
    public object SOUTH : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean =
            board.distanceToEdge(coords, BoardEdge.BOTTOM) <= depth
    }

    /**
     * One or more columns of hexes at the left of the map
     */
    @Serializable
    public object WEST : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean =
            board.distanceToEdge(coords, BoardEdge.LEFT) <= depth
    }

    /**
     * One or more columns of hexes at the right of the map
     */
    @Serializable
    public object EAST : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean =
            board.distanceToEdge(coords, BoardEdge.RIGHT) <= depth
    }

    /**
     * The left half of [NORTH] and the top half of [WEST]
     */
    @Serializable
    public object NORTHWEST : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean {
            return board.distanceToEdge(coords, BoardEdge.RIGHT) > (board.width + 1) / 2
                    && board.distanceToEdge(coords, BoardEdge.BOTTOM) > (board.height + 1) / 2
                    && (NORTH.isInRegion(coords, board, depth)
                    || WEST.isInRegion(coords, board, depth))
        }
    }

    /**
     * The right half of [NORTH] and the top half of [WEST]
     */
    @Serializable
    public object NORTHEAST : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean {
            return board.distanceToEdge(coords, BoardEdge.LEFT) > (board.width + 1) / 2
                    && board.distanceToEdge(coords, BoardEdge.BOTTOM) > (board.height + 1) / 2
                    && (NORTH.isInRegion(coords, board, depth)
                    || EAST.isInRegion(coords, board, depth))
        }
    }

    /**
     * The left half of [SOUTH] and the bottom half of [WEST]
     */
    @Serializable
    public object SOUTHWEST : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean {
            return board.distanceToEdge(coords, BoardEdge.RIGHT) > (board.width + 1) / 2
                    && board.distanceToEdge(coords, BoardEdge.TOP) > (board.height + 1) / 2
                    && (SOUTH.isInRegion(coords, board, depth)
                    || WEST.isInRegion(coords, board, depth))
        }
    }

    /**
     * The right half of [SOUTH] and the bottom half of [EAST]
     */
    @Serializable
    public object SOUTHEAST : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean {
            return board.distanceToEdge(coords, BoardEdge.LEFT) > (board.width + 1) / 2
                    && board.distanceToEdge(coords, BoardEdge.TOP) > (board.height + 1) / 2
                    && (SOUTH.isInRegion(coords, board, depth)
                    || EAST.isInRegion(coords, board, depth))
        }
    }

    /**
     * The entire map
     */
    @Serializable
    public object ANY : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean = true
    }

    /**
     * None of the map
     */
    @Serializable
    public object NONE : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean = false
    }

    /**
     * Any hex that is on the edge of the map
     */
    @Serializable
    public object ANY_EDGE : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean =
            NORTH.isInRegion(coords, board, depth)
                    || SOUTH.isInRegion(coords, board, depth)
                    || WEST.isInRegion(coords, board, depth)
                    || EAST.isInRegion(coords, board, depth)
    }

    /**
     * Any hex that is not in one of the edge zones
     */
    @Serializable
    public object CENTER : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean =
            !ANY_EDGE.isInRegion(coords, board, depth)
    }

    /**
     * A custom region that consists of every hex within a certain radius of [hex]
     */
    @Serializable
    public class RangeOf(private val hex: HexCoords) : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean
                = coords.directionTo(hex) <= depth
    }

    /**
     * A custom region defined by the [predicate]
     */
    @Serializable
    public class Custom(private val predicate: (Coords, Board, Int) -> Boolean) : MapRegion() {
        override fun isInRegion(coords: Coords, board: Board, depth: Int): Boolean
                = predicate.invoke(coords, board, depth)
    }
}