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
    public val weather: Weather

    /**
     * Changes the current weather settings to match [newWeather].
     */
    public fun setWeather(newWeather: Weather)

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
     * Initializes a [entity], assigns a entity id and a [playerId], and adds it to the game.
     *
     * @return the id assigned to the entity
     */
    public fun addEntity(entity: E, playerId: Int): Int

    /**
     * Add an initialized [entity] to the game with [entityId]. If there is already
     * an entity with this id, it is replaced.
     *
     * @return the id of the [entity]
     */
    public fun replaceEntity(entityId: Int, entity: E): Int

    /**
     * Removes an entity from the game and notifies listeners
     *
     * @param entityId the id of the entity to remove
     * @return the entity removed, or null if there was no entity in the game with that id
     */
    public fun removeEntity(entityId: Int): E?

    /**
     * Appends [text] to the game chat
     */
    public fun appendChat(text: String)

    /**
     * Lookup for entity by id.
     *
     * @return the entity with [entityId], or null if there is no entity in the game with that id
     */
    public fun getEntity(entityId: Int): E?

    /**
     * @return a [Collection] of all the entities in the game
     */
    public fun allEntities(): Collection<E>

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

    /**
     * Adds listeners to another game and removes them from this one.
     */
    public fun transferListeners(other: Game<B, E>)
}

@Serializable
public abstract class AbstractGame<B: Board, E: Entity> : Game<B, E> {
    override val weather: Weather = Weather()
    @Serializable(with = AtomicIntegerAsIntSerializer::class)
    private val nextEntityId = AtomicInteger(1)
    protected val players: MutableMap<Int, Player> = ConcurrentHashMap()
    protected val entities: MutableMap<Int, E> = ConcurrentHashMap()
    protected val listeners: MutableList<GameListener> = CopyOnWriteArrayList()

    override fun setWeather(newWeather: Weather) {
        with (this.weather) {
            windDirection = newWeather.windDirection
            windStrength = newWeather.windStrength
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
     * Initializes a [entity], assigns an entity id and a [playerId], and adds it to the game.
     * Returns the assigned id.
     */
    override fun addEntity(entity: E, playerId: Int): Int {
        entity.initGameState(nextEntityId.getAndIncrement())
        entity.playerId = playerId
        return replaceEntity(entity.entityId, entity)
    }

    /**
     * Add an initialized [entity] to the game with [entityId]. If there is already
     * an entity with this id, it is replaced.
     */
    override fun replaceEntity(entityId: Int, entity: E): Int {
        entity.entityId = entityId
        entities[entityId] = entity
        listeners.forEach { it.entityAdded(entityId) }
        return entityId
    }

    override fun removeEntity(entityId: Int): E? {
        val entity = entities.remove(entityId)
        if (entity != null) {
            listeners.forEach { it.entityRemoved(entityId) }
        }
        return entity
    }

    override fun appendChat(text: String) {
        listeners.forEach { it.appendChat(text) }
    }

    override fun getEntity(entityId: Int): E? = entities[entityId]

    override fun allEntities(): Collection<E> = entities.values

    override fun addListener(l: GameListener) {
        listeners.add(l)
    }

    override fun removeListener(l: GameListener) {
        listeners.remove(l)
    }

    override fun transferListeners(other: Game<B, E>) {
        listeners.forEach {
            other.addListener(it)
        }
        listeners.clear()
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
    public fun entityAdded(entityId: Int)
    public fun entityRemoved(entityId: Int)
    public fun appendChat(text: String)
    public fun boardChanged() {}
    public fun weatherChanged() {}
}