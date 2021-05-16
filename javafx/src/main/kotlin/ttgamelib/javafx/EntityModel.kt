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
import tornadofx.*
import ttgamelib.Coords
import ttgamelib.Entity

/**
 * Base class for [Entity] models
 */
public open class EntityModel(public val entity: Entity) {
    public val entityId: Int by entity::entityId
    public val nameProperty: StringProperty = SimpleStringProperty().apply {
        bind(entity.observable(Entity::name))
    }
    public var name: String by nameProperty
    public val playerIdProperty: Property<Int> = entity.observable(Entity::playerId)
    public var playerId: Int by playerIdProperty
    public val facingProperty: Property<Int> = entity.observable(Entity::facing)
    public var facing: Int by facingProperty
    public val primaryPositionPropety: ObjectProperty<Coords?> = entity.observable(Entity::primaryPosition)
    public var primaryPosition: Coords? by primaryPositionPropety
}