/*
 *  Tabletop Game Library
 *  Copyright (c) 2021 Carl W Spain
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package ttgamelib.javafx

import javafx.beans.property.*
import javafx.beans.value.ObservableBooleanValue
import javafx.collections.ObservableMap
import tornadofx.*
import ttgamelib.*

/**
 * The model for the game board, used for editing and display.
 */
public interface BoardModel : Board {

    public var currentBoard: HexBoard
    public val widthProperty: IntegerProperty
    public val heightProperty: IntegerProperty
    public val dirty: ObservableBooleanValue
    public fun createBoard(): Board
}

/**
 * [BoardModel] for a hex board
 */
public class HexBoardModel(override var currentBoard: HexBoard) : BoardModel {
    override val widthProperty: IntegerProperty = SimpleIntegerProperty(currentBoard.width)
    override var width: Int by widthProperty
    override val heightProperty: IntegerProperty = SimpleIntegerProperty(currentBoard.height)
    override val height: Int by heightProperty
    public val verticalGridProperty: BooleanProperty = SimpleBooleanProperty(currentBoard.verticalGrid)
    public var verticalGrid: Boolean by verticalGridProperty
    public val oddOffsetProperty: BooleanProperty = SimpleBooleanProperty(currentBoard.oddOffset)
    public var oddOffset: Boolean by oddOffsetProperty
    public val defaultHexProperty: ObjectProperty<Terrain> = SimpleObjectProperty(currentBoard.defaultHex)
    public var defaultHex: Terrain by defaultHexProperty
    public val hexes: ObservableMap<HexCoords, Terrain> = currentBoard.exportHexes().toObservable()

    override val dirty: ObservableBooleanValue = widthProperty.isNotEqualTo(currentBoard.width)
        .or(heightProperty.isNotEqualTo(currentBoard.height))
        .or(verticalGridProperty.eq(currentBoard.verticalGrid).not())
        .or(oddOffsetProperty.eq(currentBoard.oddOffset).not())
        .or(!hexes.equals(currentBoard.exportHexes()))

    override fun getTerrain(col: Int, row: Int): Terrain =
        get(HexCoords.createFromOffset(col, row, verticalGrid, oddOffset))

    override fun get(coords: Coords): Terrain = hexes[coords] ?: defaultHex

    override fun distanceToEdge(coords: Coords, edge: BoardEdge): Int {
        return createBoard().distanceToEdge(coords, edge)
    }

    override fun createBoard(): Board =
        HexBoard(width, height, verticalGrid, oddOffset, defaultHex, hexes)
}