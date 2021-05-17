/*
 *  Sail and Oar
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

import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.ViewModel
import tornadofx.getValue
import tornadofx.setValue
import ttgamelib.*

/**
 * Model for the [Game] for this client.
 *
 * @param B The type of [Board] used with this game.
 * @param E The base type of [Entity] used with this game.
 * @param G The [Game] type
 * @property client The [ClientController] used with this client.
 * @param game The initial [Game] for this client.
 */

public abstract class GameModel<B: Board, E: Entity, G: Game<B, E>, C: ClientController>(
    public val client: C,
    game: G
) : GameListener, ClientListener {
    public val gameProperty: ObjectProperty<G> = SimpleObjectProperty(game)
    public val game: G by gameProperty
    public val boardProperty: ObjectProperty<B> = SimpleObjectProperty(game.board)
    public var board: B by boardProperty
    public val windDirectionProperty: IntegerProperty = SimpleIntegerProperty(game.weather.windDirection)
    public var windDirection: Int by windDirectionProperty
    public val windStrengthProperty: ObjectProperty<WindStrength> = SimpleObjectProperty(game.weather.windStrength)
    public var windStrength: WindStrength by windStrengthProperty

    public val players: ObservableList<PlayerModel> = FXCollections.observableArrayList {
        arrayOf (it.teamProperty, it.colorProperty, it.homeEdgeProperty, it.status)
    }
    public val playerReadyProperty: BooleanProperty = SimpleBooleanProperty(false)
    public var playerReady: Boolean by playerReadyProperty
    public val units: ObservableList<EntityModel> = FXCollections.observableArrayList()

    init {
        client.addConnectionListener(this)
        game.addListener(this)
        gameProperty.addListener { _, old, new ->
            old.removeListener(this)
            new.addListener(this)
            refreshGame()
        }
        players.setAll(game.allPlayers().map { PlayerModel(it) }.toList())
        units.setAll(game.allEntities().map { createModel(it) }.toList())
    }

    protected open fun refreshGame() {
        players.setAll(game.allPlayers().map { PlayerModel(it) }.toList())
        units.setAll(game.allEntities().map { createModel(it) }.toList())
    }

    public abstract fun createModel(entity: Entity): EntityModel

    override fun playerAdded(playerId: Int) {
        Platform.runLater {
            game.getPlayer(playerId)?.let {
                players.add(PlayerModel(it))
            }
        }
    }

    override fun playerRemoved(playerId: Int) {
        Platform.runLater {
            players.removeIf {
                it.id == playerId
            }
        }
    }

    override fun playerChanged(playerId: Int) {
        Platform.runLater {
            game.getPlayer(playerId)?.let { player ->
                players.find {
                    it.id == playerId
                }?.let {
                    it.team = player.team
                    it.color = player.color
                    it.homeEdge = player.homeEdge
                }
            }
        }
    }

    override fun playerDisconnected(playerId: Int, disconnected: Boolean) {
        players.find {
            it.id == playerId
        }?.let {
            it.disconnected = disconnected
        }
    }

    override fun entityAdded(entityId: Int) {
        Platform.runLater {
            game.getEntity(entityId)?.let {
                units.add(createModel(it))
            }
        }
    }

    override fun entityRemoved(entityId: Int) {
        Platform.runLater {
            units.removeIf {
                it.entityId == entityId
            }
        }
    }

    override fun appendChat(text: String) {
    }

    override fun playerReady(playerId: Int, ready: Boolean) {
        Platform.runLater {
            players.find {
                it.id == playerId
            }?.let {
                it.ready = ready
                if (client.clientId == playerId) {
                    playerReady = ready
                }
            }
        }
    }

    override fun boardChanged() {
        Platform.runLater {
            board = game.board
        }
    }

    override fun weatherChanged() {
        Platform.runLater {
            windDirection = game.weather.windDirection
            windStrength = game.weather.windStrength
        }
    }

    override fun clientConnected(controller: ClientController) {
    }

    override fun clientDisconnected(controller: ClientController) {
    }

    override fun nameTaken(
        controller: ClientController,
        suggestion: String,
        taken: Set<String>,
        disconnected: Boolean
    ) {
    }

    @Suppress("UNCHECKED_CAST")
    override fun gameChanged(old: Game<*, *>, new: Game<*, *>) {
        gameProperty.value = new as G
    }
}