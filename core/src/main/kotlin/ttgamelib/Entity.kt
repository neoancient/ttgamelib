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

/**
 * A unit that has a position on the board and is capable of acting
 */
public interface Entity {
    /** An ID that is unique in the game */
    public var unitId: Int

    /** An identifier shown to players. This should usually be unique in the game. */
    public var name: String
    /** The id of the player controlling this [Entity], or < 0 for none */
    public var playerId: Int
    /** The direction the [Entity] is facing.
     * Zero is the top of the board, and the value increases clockwise. The size of the arc with
     * each increase depends on the type of grid.
     */
    public var facing: Int

    /**
     * The position on the board, if placed
     */
    public var primaryPosition: Coords?

    /**
     * The number of elevation units above or below the board's reference elevation.
     */
    public var elevation: Int

    /**
     * Assigns the game ID and performs any necessary initialization
     */
    public fun initGameState(id: Int) {
        unitId = id
    }
}