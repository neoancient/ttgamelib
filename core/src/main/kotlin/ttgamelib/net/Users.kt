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

package ttgamelib.net

import io.ktor.auth.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Matches a user name with a connection id.
 */
internal data class User(
    val name: String,
    var connectionId: Int = -1,
) : Principal

/**
 * Database of all connected users.
 *
 * This is used to track what usernames are in use and to suggest alternate names
 * when one is taken.
 */
internal class Users {
    private val users: MutableMap<String, User> = ConcurrentHashMap()
    // We're going to track names that have been suggested to make sure they don't
    // get suggested to multiple users.
    private val suggestedNames = HashSet<String>()

    operator fun get(key: String): User? = users[key.toLowerCase()]
    operator fun contains(key: String): Boolean = key.toLowerCase() in users
    operator fun plusAssign(user: User) {
        users[user.name.toLowerCase()] = user
    }
    operator fun minusAssign(user: User) {
        users.remove(user.name.toLowerCase())
    }

    internal fun suggestAlternateName(requested: String): Pair<String, Set<String>> {
        var append = 0
        var suggested: String
        val taken = HashSet<String>()
        synchronized (users) {
            taken += users.values.map(User::name)
            taken += suggestedNames
            do {
                append++
                suggested = "$requested.$append"
            } while (taken.contains(suggested))
            suggestedNames += suggested
        }
        return suggested to taken
    }
}