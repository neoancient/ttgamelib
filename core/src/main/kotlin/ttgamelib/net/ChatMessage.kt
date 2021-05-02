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

import kotlinx.serialization.Serializable
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.ins.InsExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

private val extensions = listOf(
    StrikethroughExtension.create(),
    AutolinkExtension.create(),
    InsExtension.create(),
)

private val parser = Parser.builder().extensions(extensions).build()
private val renderer = HtmlRenderer.builder().extensions(extensions).build()
private fun String.render(): String = renderer.render(parser.parse(this))

@Serializable
public sealed class ChatMessage {
    abstract public fun toHtml(): String
}

/**
 * A system message, often broadcast to all users.
 */
@Serializable
public data class SystemMessage(val text: String) : ChatMessage() {
    override fun toHtml(): String = "**$text**".render()
}

/**
 * A message to provide feedback to a user.
 */
@Serializable
public data class InfoMessage(val text: String) : ChatMessage() {
    override fun toHtml(): String = "*$text*".render()
}

/**
 * A basic chat message, visible to everyone.
 */
@Serializable
public data class SimpleMessage(val user: String, val text: String) : ChatMessage() {
    override fun toHtml(): String = "**$user:** $text".render()
}

/**
 * A message sent to another user.
 */
@Serializable
public data class WhisperMessage(val sender: String, val recipient: String, val text: String) : ChatMessage() {
    override fun toHtml(): String = "**$sender &gt; $recipient:** $text".render()
}

/**
 * A message that expresses an action rather than a statement.
 *
 * Example: "User1 is sad"
 */
@Serializable
public data class EmoteMessage(val user: String, val text: String) : ChatMessage() {
    override fun toHtml(): String = "$user $text".render()
}
