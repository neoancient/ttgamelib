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


public const val NO_TEAM: Int = -1

/**
 * A participant in the game. Each player should have an id that is assigned by the {@link Game}
 * to ensure that it is unique.
 */
@Serializable
public class Player(
    public val id: Int,
    public val name: String,
    public var team: Int = NO_TEAM,
    public var color: PlayerColor = PlayerColor.BLUE,
    public var homeEdge: MapRegion = MapRegion.ANY,
    public var ready: Boolean = false,
    public var disconnected: Boolean = false
) {
    /**
     * Determines whether this player is allowed to make changes to another player's configuration.
     * This can be used for things like GM mode or team leaders
     */
    public fun canEdit(otherPlayerId: Int): Boolean = id == otherPlayerId

    /**
     * Copies variable properties from another [Player] instance
     */
    public fun set(other: Player) {
        team = other.team
        color = other.color
        homeEdge = other.homeEdge
    }
}

public enum class PlayerColor(public val rgb: Int) {
    BLUE(0x534DC2),
    RED(0xCC443C),
    GREEN(0x35C23D),
    ORANGE(0xF59223),
    VIOLET(0x804E77),
    YELLOW(0xF5DF65);

    public fun red(): Double = (rgb shr 16) / 256.0
    public fun green(): Double = ((rgb and 0xff00) shr 8) / 256.0
    public fun blue(): Double = (rgb and 0xff) / 256.0
}