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

package ttgamelib.javafx

import javafx.scene.image.Image
import ttgamelib.Terrain
import ttgamelib.TerrainType
import ttgamelib.WindStrength

private const val IMAGE_DIR = "images"

private val terrainHexes = HashMap<TerrainType, Image>()

/**
 * Retrieves an image for a hex with the terrain type, or null if none is available.
 */
public fun TerrainType.hexFor(): Image? {
    return terrainHexes[this] ?:
        Image(ClassLoader.getSystemResourceAsStream("$IMAGE_DIR/terrain/${name.toLowerCase()}.png")).apply {
            terrainHexes[this@hexFor] = this
        }
    }

/**
 * Retrieves an image for an icon indicating wind speed, or null if none is available.
 */
public fun WindStrength.imageFor(): Image? {
    return ClassLoader.getSystemResourceAsStream("$IMAGE_DIR/wind_strength/${name.toLowerCase()}.png")?.let {
        Image(it)
    }
}

/**
 * Retrieves an image for an icon indicating wind direction for a facing, or null if none is available.
 *
 * @param rectangular Whether the facing is on a rectangular grid rather than hex
 * @param verticalGrid For hex grids this indicates whether the grid has a vertical alignment
 *                     (flat-topped hexes). For rectangular grids it is ignored.
 */
public fun Int.imageForWindDirection(rectangular: Boolean, verticalGrid: Boolean): Image? {
    val names = if (rectangular) {
        listOf("n", "ne_r", "e", "se_r", "s", "sw_r", "w", "nw_r")
    } else if (verticalGrid) {
        listOf("n", "ne_v", "se_v", "s", "sw_v", "nw_v")
    } else {
        listOf("e", "se_h", "sw_h", "w", "nw_h", "ne_h")
    }
    return ClassLoader.getSystemResourceAsStream("$IMAGE_DIR/wind_direction/wind_${names[this]}.png")?.let {
        Image(it)
    }
}
