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

/**
 * A [Canvas] that draws a hex grid in the [BoardView].
 */
public class HexBoardViewGridLayer(board: HexBoardModel) : HexBoardViewLayer(board) {

    override fun drawCells(firstCol: Int, lastCol: Int, firstRow: Int, lastRow: Int) {
        with (graphicsContext2D) {
            var xPos = MAP_BORDER
            for (col in firstCol..lastCol) {
                var yPos = MAP_BORDER + if ((col % 2 == 1) == board.oddOffset) 0.0 else HEX_HEIGHT * 0.5
                for (row in firstRow..lastRow) {
                    val xCoords = borderX.map { xPos + it }.toDoubleArray()
                    val yCoords = borderY.map { yPos + it }.toDoubleArray()
                    stroke = javafx.scene.paint.Color.WHITE
                    lineWidth = 3.0
                    strokePolygon(xCoords, yCoords, 6)
                    stroke = javafx.scene.paint.Color.BLACK
                    lineWidth = 1.0
                    strokePolygon(xCoords, yCoords, 6)
                    yPos += HEX_HEIGHT
                }
                xPos += HEX_DX
            }
        }
    }
}