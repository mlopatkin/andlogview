/*
 * Copyright 2024 the Andlogview authors
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

package name.mlopatkin.andlogview.config;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A JSON object serializer that supports polymorphism.
 *
 * @param <B> the base type for the serialized instances
 */
public class PolymorphicTypeAdapterFactory<B> implements TypeAdapterFactory {
    private final String typeIdField;
    private final Class<B> baseClass;
    private final Map<String, Class<? extends B>> typeById = new HashMap<>();
    private final Map<Class<? extends B>, String> idByType = new HashMap<>();

    public PolymorphicTypeAdapterFactory(String typeIdField, Class<B> baseClass) {
        this.typeIdField = typeIdField;
        this.baseClass = baseClass;
    }

    public <T extends B> PolymorphicTypeAdapterFactory<B> subtype(Class<T> cls, String typeId) {
        var prevType = typeById.putIfAbsent(typeId, cls);
        if (prevType != null) {
            throw new IllegalStateException(
                    String.format(
                            "Cannot bind id `%s` to `%s` because it is already bound to `%s`", typeId, cls, prevType));
        }
        // A type can have multiple typeIds, first one wins.
        idByType.putIfAbsent(cls, typeId);
        return this;
    }

    @Override
    public <T> @Nullable TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (!baseClass.equals(rawType)) {
            return null;
        }

        // At this point we know that the type T is actually B.
        @SuppressWarnings("unchecked")
        TypeAdapter<T> result = (TypeAdapter<T>) new PolymorphicTypeAdapter(gson);
        return result;
    }

    private class PolymorphicTypeAdapter extends TypeAdapter<B> {
        private final TypeAdapter<JsonElement> jsonElementAdapter;
        private final Map<String, TypeAdapter<? extends B>> typeAdapterById;
        private final Map<Class<?>, TypeAdapter<? extends B>> typeAdapterByCls;

        public PolymorphicTypeAdapter(Gson gson) {
            jsonElementAdapter = gson.getAdapter(JsonElement.class);

            typeAdapterById = typeById.entrySet().stream().collect(toImmutableMap(
                    Map.Entry::getKey,
                    entry -> findDelegate(gson, entry.getValue())
            ));

            typeAdapterByCls = idByType.keySet().stream().collect(toImmutableMap(
                    cls -> cls,
                    cls -> findDelegate(gson, cls)
            ));
        }

        private <T> TypeAdapter<T> findDelegate(Gson gson, Class<T> cls) {
            return gson.getDelegateAdapter(PolymorphicTypeAdapterFactory.this, TypeToken.get(cls));
        }

        @Override
        public void write(JsonWriter out, B value) throws IOException {
            // I'm not sure why it compiles
            writeImpl(out, classOf(value), value);
        }

        private <T extends B> void writeImpl(JsonWriter out, Class<T> implClass, T value) throws IOException {
            TypeAdapter<T> adapter = typeAdapterForClass(implClass);
            var jsonObject = adapter.toJsonTree(value).getAsJsonObject();
            if (jsonObject.has(typeIdField)) {
                throw parseFailure(
                        "Cannot serialize type `%s` because it already defines `%s`", implClass.getName(), typeIdField);
            }
            jsonObject.addProperty(typeIdField, typeIdForClass(implClass));

            jsonElementAdapter.write(out, jsonObject);
        }

        @Override
        public B read(JsonReader in) throws IOException {
            JsonElement element = jsonElementAdapter.read(in);
            if (!element.isJsonObject()) {
                throw parseFailure("Expected the element to be a JsonObject, got `%s`", element);
            }
            // An underlying reader may be traversing a caller-provided Json tree. Modifying the returned elements may
            // modify the tree in this case.
            JsonObject jsonObject = element.getAsJsonObject().deepCopy();
            JsonElement typeId = jsonObject.remove(typeIdField);
            if (typeId == null) {
                throw parseFailure(
                        "The type id field `%s` is missing in JSON data for type `%s`",
                        typeIdField,
                        baseClass.getName());
            }
            if (!(typeId.isJsonPrimitive() && typeId.getAsJsonPrimitive().isString())) {
                throw parseFailure(
                        "The type id field `%s` is not a String", typeIdField
                );
            }
            TypeAdapter<? extends B> adapter = typeAdapterForId(typeId.getAsString());
            if (adapter == null) {
                throw parseFailure("Cannot find type adapter for type id `%s`", typeId.getAsString());
            }
            return adapter.fromJsonTree(jsonObject);
        }

        private <T extends B> TypeAdapter<T> typeAdapterForClass(Class<T> cls) {
            // The map is Class<T> -> TypeAdapter<T> by construction.
            @SuppressWarnings("unchecked")
            var adapter = (TypeAdapter<T>) typeAdapterByCls.get(cls);

            return Objects.requireNonNull(adapter);
        }

        private @Nullable TypeAdapter<? extends B> typeAdapterForId(String id) {
            return typeAdapterById.get(id);
        }

        @FormatMethod
        private JsonParseException parseFailure(@FormatString String format, Object... args) {
            throw new JsonParseException(String.format(format, args));
        }
    }

    private String typeIdForClass(Class<? extends B> cls) {
        var id = idByType.get(cls);
        assert id != null;
        return id;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> classOf(T value) {
        return (Class<? extends T>) value.getClass();
    }
}
