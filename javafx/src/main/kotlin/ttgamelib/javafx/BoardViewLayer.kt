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

import javafx.scene.canvas.Canvas
import ttgamelib.Board
import kotlin.math.ceil
import kotlin.math.floor

internal const val HEX_WIDTH = 180.0
internal const val HEX_HEIGHT = 156.0
internal const val HEX_DX = HEX_WIDTH * 0.75
internal const val MAP_BORDER = HEX_WIDTH * 2

public abstract class BoardViewLayer<B: BoardModel>(protected val board: B) : Canvas() {
    protected abstract val borderX: List<Double>
    protected abstract val borderY: List<Double>

    public fun redraw(x: Double, y: Double, w: Double, h: Double) {
        with(graphicsContext2D) {
            clearRect(0.0, 0.0, width, height)
            save()
            beginPath()
            rect(x, y, w, h)
            clip()
            drawCells(
                floor(colFor(x)).toInt().coerceAtLeast(0),
                ceil(colFor(x + w)).toInt().coerceAtMost(board.width),
                floor(rowFor(y) - 1.0).toInt().coerceAtLeast(0),
                ceil(rowFor(y + h) + 1.0).toInt().coerceAtMost(board.height)
            )
            restore()
        }
    }

    abstract protected fun drawCells(firstCol: Int, lastCol: Int, firstRow: Int, lastRow: Int)

    protected fun colFor(x: Double): Double = (x - MAP_BORDER) / HEX_WIDTH
    protected fun rowFor(y: Double): Double = (y - MAP_BORDER) / HEX_HEIGHT
}

public abstract class HexBoardViewLayer(board: HexBoardModel) : BoardViewLayer<HexBoardModel>(board) {
    override val borderX: List<Double> = listOf(HEX_WIDTH * 0.25, HEX_WIDTH * 0.75, HEX_WIDTH,
        HEX_WIDTH * 0.75, HEX_WIDTH * 0.25, 0.0)
    override val borderY: List<Double> = listOf(0.0, 0.0, HEX_HEIGHT * 0.5, HEX_HEIGHT, HEX_HEIGHT, HEX_HEIGHT * 0.5)
}
