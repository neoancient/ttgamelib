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
import javafx.beans.value.ObservableObjectValue
import tornadofx.getValue
import tornadofx.objectBinding
import tornadofx.setValue
import ttgamelib.MapRegion
import ttgamelib.Player
import ttgamelib.PlayerColor

/**
 * Conditions that affect whether the game can progress to the next phase.
 */
public enum class PlayerStatus {
    READY, NOT_READY, DISCONNECTED
}

/**
 * Model for a [Player] in the [Game].
 */
public class PlayerModel(player: Player) {
    public val id: Int = player.id
    public val nameProperty: StringProperty = SimpleStringProperty(player.name)
    public val name: String by nameProperty
    public val teamProperty: IntegerProperty =  SimpleIntegerProperty(player.team)
    public var team: Int by teamProperty
    public val colorProperty: ObjectProperty<PlayerColor> = SimpleObjectProperty(player.color)
    public var color: PlayerColor by colorProperty
    public val homeEdgeProperty: ObjectProperty<MapRegion> = SimpleObjectProperty(player.homeEdge)
    public var homeEdge: MapRegion by homeEdgeProperty
    public val readyProperty: BooleanProperty = SimpleBooleanProperty(player.ready)
    public var ready: Boolean by readyProperty
    public val disconnectedProperty: BooleanProperty = SimpleBooleanProperty(player.disconnected)
    public var disconnected: Boolean by disconnectedProperty
    public val status: ObservableObjectValue<PlayerStatus?> = objectBinding(readyProperty, disconnectedProperty) {
        if (disconnectedProperty.value) PlayerStatus.DISCONNECTED
        else if (readyProperty.value) PlayerStatus.READY
        else PlayerStatus.NOT_READY
    }

    public fun export(): Player = Player(id, name, team, color, homeEdge)
}