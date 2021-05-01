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
 * Base terrain types.
 *
 * Each terrain has an associated color value for rendering without textured images
 *
 * @property r The red component of the terrain color
 * @property g The green component of the terrain color
 * @property b The blue component of the terrain color
 */
public enum class TerrainType(
    public val r: Int,
    public val g: Int,
    public val b: Int
) {
    SEA (100, 180, 225), // generic terrain for water deep enough that the sea bed is irrelevant
    SAND (255, 211, 134),
    ROCKS (178, 138, 59),
    REEF (77, 63, 35),
    FOREST (100, 130, 69),
    GRASSLAND (150, 200, 100),
    MARSH (93, 142, 47),
    ICE (200, 230, 250);
}


/**
 * In the open ocean where the depth is not relevant, this value can be used for the depth.
 */
public const val DEPTH_DEEP_SEA: Int = Integer.MAX_VALUE

/**
 * Characteristics of an individual hex or square on the board. Tracks water depth, land elevation, and
 * type of terrain.
 *
 * @property terrain The base terrain in a hex. The terrain of a water hex is the seabed (usually SAND,
 *                   but could be ROCK or REEF). In cases where the water is deep enough that the seabed
 *                   is irrelevant, the generic SEA terrain can be used.
 * @property depth The depth of water or ice
 * @property elevation The relative elevation of the surface of the hex. The elevation of a hex is
 *                     independent of the depth, and it is possible for a hex to have both elevation
 *                     and depth > 0 in cases such as a mountain lake or a lock system, or a sea in a
 *                     depression.
 */
@Serializable
public data class Terrain(
    val terrain: TerrainType,
    val depth: Int,
    val elevation: Int
)