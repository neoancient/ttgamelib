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

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.layout.Pane
import javafx.scene.transform.Affine
import tornadofx.*
import kotlin.math.min


public class BoardView<B: BoardModel> : Fragment() {
    internal val board: BoardModel by param()

    private val viewportWidthProperty = SimpleDoubleProperty()
    private val viewportHeightProperty = SimpleDoubleProperty()
    private val viewportWidth by viewportWidthProperty
    internal val viewportHeight by viewportHeightProperty

    private val scaleProperty = SimpleDoubleProperty(1.0)
    private var scale by scaleProperty
    private var translateX = 0.0
    private var translateY = 0.0
    private var mouseX = 0.0
    private var mouseY = 0.0

    private val layers = ArrayList<BoardViewLayer<B>>()
    internal val layerWidth = board.widthProperty.multiply(HEX_WIDTH).add(MAP_BORDER * 2)
    internal val layerHeight = board.heightProperty.add(0.5).multiply(HEX_HEIGHT).add(MAP_BORDER * 2)
    internal val minScale = doubleBinding(
        viewportWidthProperty,
        viewportHeightProperty,
        layerWidth,
        layerHeight
    ) {
        min(
            viewportWidth / layerWidth.value,
            viewportHeight / layerHeight.value
        )
    }
    override val root: Pane = pane {
        parentProperty().onChangeOnce { p ->
            (p as? Pane)?.let {
                viewportWidthProperty.unbind()
                viewportHeightProperty.unbind()
                viewportWidthProperty.bind(it.widthProperty())
                viewportHeightProperty.bind(it.heightProperty())
                viewportWidthProperty.onChangeOnce {
                    scale = minScale.value.coerceAtMost(1.0)
                    redrawLayers()
                }
                viewportHeightProperty.onChangeOnce {
                    scale = minScale.value.coerceAtMost(1.0)
                    redrawLayers()
                }
            }
        }
    }

    init {
        board.widthProperty.onChange {
            redrawLayers()
        }
        board.heightProperty.onChange {
            redrawLayers()
        }
    }

    public fun setLayers(list: List<BoardViewLayer<B>>) {
        list.forEach {
            layers.add(it)
            it.widthProperty().bind(layerWidth)
            it.heightProperty().bind(layerHeight)
        }
        redrawLayers()
        addListeners(list.last())
        root.children.setAll(list)
    }

    /**
     * Calculates the limit on scrolling left and right
     * If < 0, this is the minimum and the maximum is 0.0.
     * If > 0, this is the maximum and the minimum is 0.0.
     */
    internal val limitX = doubleBinding(viewportWidthProperty, layerWidth, scaleProperty) {
        (value - layerWidth.value * scale)
    }
    /**
     * Calculates the limit on scrolling up and down
     * If < 0, this is the minimum and the maximum is 0.0.
     * If > 0, this is the maximum and the minimum is 0.0.
     */
    internal val limitY = doubleBinding(viewportHeightProperty, layerHeight, scaleProperty) {
        (value - layerHeight.value * scale)
    }

    private fun Double.boundsCheck(limit: Double) =
        coerceAtLeast(limit.coerceAtMost(0.0))
            .coerceAtMost(limit.coerceAtLeast(0.0))

    private fun addListeners(layer: BoardViewLayer<B>) {
        layer.setOnScroll {
            val factor = if (it.deltaY < 0.0) {
                0.95
            } else {
                1.05
            }
            (scale * factor).takeIf {
                it <= 1.0 && it >= minScale.value
            }?.let { newScale ->
                translateX = (translateX - (it.x - translateX) * (newScale - scale) / scale)
                    .boundsCheck(limitX.value)
                translateY = (translateY - (it.y - translateY) * (newScale - scale) / scale)
                    .boundsCheck(limitY.value)
                scale = newScale
            }
            redrawLayers()
        }
        layer.setOnMousePressed {
            mouseX = it.sceneX
            mouseY = it.sceneY
        }
        layer.setOnMouseDragged {
            translateX = (translateX + (it.sceneX - mouseX) * scale)
                .boundsCheck(limitX.value)
            translateY = (translateY + (it.sceneY - mouseY) * scale)
                .boundsCheck(limitY.value)
            redrawLayers()
        }
    }

    private fun redrawLayers() {
        val transform = Affine(scale, 0.0, translateX,
            0.0, scale, translateY)
        layers.forEach {
            it.graphicsContext2D.transform = transform
            it.redraw(
                -translateX / scale, -translateY / scale,
                viewportWidth / scale, viewportHeight / scale
            )
        }
    }
}