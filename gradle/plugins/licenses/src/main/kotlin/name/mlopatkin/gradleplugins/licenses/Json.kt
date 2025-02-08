/*
 * Copyright 2025 the Andlogview authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.mlopatkin.gradleplugins.licenses

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.File


internal fun json(jsonFile: File, block: Gson.() -> JsonElement) {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val json = gson.block()

    jsonFile.bufferedWriter().use {
        gson.toJson(json, it)
    }
}

internal fun jsonObject(block: JsonObjectContext.() -> Unit): JsonObject {
    val obj = JsonObject()
    JsonObjectContext(obj).block()
    return obj
}

internal fun jsonArray(block: JsonArray.() -> Unit): JsonArray {
    val a = JsonArray()
    a.block()
    return a
}

internal class JsonObjectContext(private val json: JsonObject) {
    infix fun String.jsonTo(value: JsonElement) {
        json.add(this, value)
    }

    infix fun String.jsonTo(value: String) {
        json.add(this, JsonPrimitive(value))
    }

    infix fun String.jsonTo(value: Int) {
        json.add(this, JsonPrimitive(value))
    }
}
