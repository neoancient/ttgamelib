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

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import ttgamelib.net.AddEntityPacket
import ttgamelib.net.Packet

internal class PacketTest : FunSpec({

    val module = SerializersModule {
        polymorphic(Entity::class) {
            subclass(TestEntity::class, TestEntity.serializer())
        }
    }

    test("AddEntityPacket should deserialize Entity") {
        val entity = TestEntity().apply {
            entityId = 2
            playerId = 4
        }

        val encoder = Json { serializersModule = module }
        val json = encoder.encodeToString(Packet.serializer(), AddEntityPacket(entity))
        val packet = encoder.decodeFromString(Packet.serializer(), json)

        packet.shouldBeTypeOf<AddEntityPacket>()
        packet.entity.entityId shouldBe entity.entityId
        packet.entity.playerId shouldBe entity.playerId
    }
})