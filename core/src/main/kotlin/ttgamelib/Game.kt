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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

public interface Game<B: Board, E: Entity> {
    public var board: B

    /**
     * @return a collection of the [Player]s in the [Game]
     */
    public fun allPlayers(): Collection<Player>

    /**
     * Adds a [player] to the game and notifies listeners
     */
    public fun addPlayer(player: Player)

    /**
     * Generates a new player with [id], [name], and an unused [PlayerColor] and adds the
     * player to the game.
     *
     * @return the generated Player
     */
    public fun newPlayer(id: Int, name: String): Player

    /**
     * @return the next available [PlayerColor]
     */
    public fun selectColor(): PlayerColor

    /**
     * Removes the player from the game and notifies listeners.
     *
     * @param playerId The id of the player to be removed
     * @return The player that was removed, or null if the player was not in the game
     */
    public fun removePlayer(playerId: Int): Player?

    /**
     * Copies the configuration from one player to another.
     *
     * @param playerId the player to modify
     * @param newValues the values to copy
     *
     * @see [Player.set]
     */
    public fun updatePlayer(playerId: Int, newValues: Player)

    /**
     * Sets or clears the ready property for a player
     *
     * @param playerId the id of the player
     * @param ready whether the player is ready
     */
    public fun playerReady(playerId: Int, ready: Boolean)

    /**
     * Sets or clears the disconnected property for a player
     *
     * @param playerId the id of the player
     * @param disconnected whether the player is disconnected
     */
    public fun playerDisconnected(playerId: Int, disconnected: Boolean)

    /**
     * Lookup for player by [playerId]
     *
     * @return the player with [playerId], or null if there is no player with that id in the game
     */
    public fun getPlayer(playerId: Int): Player?

    /**
     * Initializes a [unit], assigns a unit id and a [playerId], and adds it to the game.
     *
     * @return the id assigned to the unit
     */
    public fun addUnit(unit: E, playerId: Int): Int

    /**
     * Add an initialized [unit] to the game with [unitId]. If there is already
     * a unit with this id, it is replaced.
     *
     * @return the id of the [unit]
     */
    public fun replaceUnit(unitId: Int, unit: E): Int

    /**
     * Removes a unit from the game and notifies listeners
     *
     * @param unitId the id of the unit to remove
     * @return the unit removed, or null if there was no unit in the game with that id
     */
    public fun removeUnit(unitId: Int): E?

    /**
     * Appends [text] to the game chat
     */
    public fun appendChat(text: String)

    /**
     * Lookup for unit by id.
     *
     * @return the unit with [unitId], or null if there is no unit in the game with that id
     */
    public fun getEntity(unitId: Int): E?

    /**
     * @return a [Collection] of all the units in the game
     */
    public fun allUnits(): Collection<E>

    /**
     * Adds a listener to be notified of game events.
     *
     * If a listener is added multiple times it will receive multiple events.
     */
    public fun addListener(l: GameListener)

    /**
     * Removes a game listener.
     *
     * If a listener has been added more than once, only one addition will be removed.
     */
    public fun removeListener(l: GameListener)
}

@Serializable
public abstract class AbstractGame<B: Board, E: Entity> : Game<B, E> {
    public val weather: Weather = Weather()
    @Serializable(with = AtomicIntegerAsIntSerializer::class)
    private val nextUnitId = AtomicInteger(1)
    private val players: MutableMap<Int, Player> = ConcurrentHashMap()
    private val entities: MutableMap<Int, E> = ConcurrentHashMap()
    private val listeners: MutableList<GameListener> = CopyOnWriteArrayList()

    public fun setWeather(weather: Weather) {
        with (this.weather) {
            windDirection = weather.windDirection
            windStrength = weather.windStrength
        }
        listeners.forEach {
            it.weatherChanged()
        }
    }

    override fun allPlayers(): Collection<Player> = players.values

    override fun addPlayer(player: Player) {
        if (player.id !in players) {
            players[player.id] = player
            listeners.forEach { it.playerAdded(player.id) }
        }
    }

    override fun newPlayer(id: Int, name: String): Player {
        val player = Player(id, name, color = selectColor())
        addPlayer(player)
        return player
    }

    override fun selectColor(): PlayerColor {
        val used = players.values.map { it.color }.toSet()
        return PlayerColor.values().find {
            it !in used
        } ?: PlayerColor.values()[0]
    }

    override fun removePlayer(playerId: Int): Player? {
        val p = players.remove(playerId)
        if (null != p) {
            listeners.forEach { it.playerRemoved(p.id) }
        }
        return p
    }

    override fun updatePlayer(playerId: Int, newValues: Player) {
        getPlayer(playerId)?.let {
            it.set(newValues)
            listeners.forEach { l -> l.playerChanged(playerId) }
        }
    }

    override fun playerReady(playerId: Int, ready: Boolean) {
        getPlayer(playerId)?.let {
            it.ready = ready
            listeners.forEach { l -> l.playerReady(playerId, ready) }
        }
    }

    override fun playerDisconnected(playerId: Int, disconnected: Boolean) {
        getPlayer(playerId)?.let {
            it.disconnected = disconnected
            listeners.forEach { l -> l.playerDisconnected(playerId, disconnected) }
        }
    }

    override fun getPlayer(playerId: Int): Player? = players[playerId]

    /**
     * Initializes a [unit], assigns a unit id and a [playerId], and adds it to the game.
     * Returns the assigned id.
     */
    override fun addUnit(unit: E, playerId: Int): Int {
        unit.initGameState(nextUnitId.getAndIncrement())
        unit.playerId = playerId
        return replaceUnit(unit.entityId, unit)
    }

    /**
     * Add an initialized [unit] to the game with [unitId]. If there is already
     * a unit with this id, it is replaced.
     */
    override fun replaceUnit(unitId: Int, unit: E): Int {
        unit.entityId = unitId
        entities[unitId] = unit
        listeners.forEach { it.unitAdded(unitId) }
        return unitId
    }

    override fun removeUnit(unitId: Int): E? {
        val unit = entities.remove(unitId)
        if (unit != null) {
            listeners.forEach { it.unitRemoved(unitId) }
        }
        return unit
    }

    override fun appendChat(text: String) {
        listeners.forEach { it.appendChat(text) }
    }

    override fun getEntity(unitId: Int): E? = entities[unitId]

    override fun allUnits(): Collection<E> = entities.values

    override fun addListener(l: GameListener) {
        listeners.add(l)
    }

    override fun removeListener(l: GameListener) {
        listeners.remove(l)
    }
}

/**
 * Implemented by classes that need to be notified of game events
 */
public interface GameListener {
    public fun playerAdded(playerId: Int)
    public fun playerRemoved(playerId: Int)
    public fun playerChanged(playerId: Int)
    public fun playerReady(playerId: Int, ready: Boolean) {}
    public fun playerDisconnected(playerId: Int, disconnected: Boolean) {}
    public fun unitAdded(unitId: Int)
    public fun unitRemoved(unitId: Int)
    public fun appendChat(text: String)
    public fun boardChanged() {}
    public fun weatherChanged() {}
}