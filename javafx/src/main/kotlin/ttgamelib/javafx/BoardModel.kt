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
 *
 */
public interface BoardModel : Board {
    public val widthProperty: IntegerProperty
    public val heightProperty: IntegerProperty
    public val dirty: ObservableBooleanValue
}

public class HexBoardModel(board: HexBoard) : BoardModel {
    override val widthProperty: IntegerProperty = SimpleIntegerProperty(board.width)
    override var width: Int by widthProperty
    override val heightProperty: IntegerProperty = SimpleIntegerProperty(board.height)
    override val height: Int by heightProperty
    public val verticalGridProperty: BooleanProperty = SimpleBooleanProperty(board.verticalGrid)
    public var verticalGrid: Boolean by verticalGridProperty
    public val oddOffsetProperty: BooleanProperty = SimpleBooleanProperty(board.oddOffset)
    public var oddOffset: Boolean by oddOffsetProperty
    public val defaultHexProperty: ObjectProperty<Terrain> = SimpleObjectProperty(board.defaultHex)
    public var defaultHex: Terrain by defaultHexProperty
    public val hexes: ObservableMap<HexCoords, Terrain> = board.exportHexes().toObservable()

    override val dirty: ObservableBooleanValue = widthProperty.isNotEqualTo(board.width)
        .or(heightProperty.isNotEqualTo(board.height))
        .or(verticalGridProperty.eq(board.verticalGrid).not())
        .or(oddOffsetProperty.eq(board.oddOffset).not())
        .or(!hexes.equals(board.exportHexes()))

    override fun getTerrain(col: Int, row: Int): Terrain =
        get(HexCoords.createFromOffset(col, row, verticalGrid, oddOffset))

    override fun get(coords: Coords): Terrain = hexes[coords] ?: defaultHex

    override fun distanceToEdge(coords: Coords, edge: BoardEdge): Int {
        return createBoard().distanceToEdge(coords, edge)
    }

    public fun createBoard(): HexBoard =
        HexBoard(width, height, verticalGrid, oddOffset, defaultHex, hexes)
}