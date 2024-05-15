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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Objects;

class PolymorphicTypeAdapterFactoryTest {
    private final PolymorphicTypeAdapterFactory<Base> factory =
            new PolymorphicTypeAdapterFactory<>("className", Base.class)
                    .subtype(BaseImpl1.class, "Impl1")
                    .subtype(BaseImpl2.class, "Impl2");

    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(factory).setPrettyPrinting().create();

    @Test
    void canSerializeValue() {
        var initial = new BaseImpl1("impl1");
        var value = roundTrip(initial);

        assertThat(value).isEqualTo(initial);
    }

    @Test
    void canSerializeValueWithExplicitType() {

        var initial = new BaseImpl1("impl1");
        var value = roundTripAs(initial, BaseImpl1.class);

        assertThat(value).isEqualTo(initial);
    }

    @Test
    void canSerializeSubclassNotRegisteredInFactory() {
        var initial = new Unregistered("impl1");
        var deserialized = roundTripAs(initial, Unregistered.class);

        assertThat(deserialized).isEqualTo(initial);
    }

    @Test
    void canSerializedPolymorphicContainer() {
        var initial = ImmutableList.of(new BaseImpl1("impl1_1"), new BaseImpl2("impl2_2"), new BaseImpl1("impl1_3"));
        var deserialized = roundTripContainer(initial);

        assertThat(deserialized).isEqualTo(initial);
    }

    @Test
    void throwsIfTypeIdFieldClashes() {
        factory.subtype(Incompatible.class, "incompatible");

        assertThatThrownBy(() -> gson.toJson(new Incompatible(), Base.class)
        ).isInstanceOf(JsonParseException.class)
                .hasMessage("Cannot serialize type `" + Incompatible.class.getName()
                        + "` because it already defines `className`");
    }

    @Test
    void throwsIfClassNameIsMissing() {
        assertThatThrownBy(() -> parseValue("""
                { "value": "someValue" }
                """
        )).isInstanceOf(JsonParseException.class)
                .hasMessage("The type id field `className` is missing in JSON data for type `" + Base.class.getName()
                        + "`");

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "{ \"foo\": 1}",
            "[]",
            "null",
            "false"
    })
    void throwsIfClassNameIsNotAString(String classNameValue) {
        assertThatThrownBy(() -> parseValue(String.format("""
                { "value": "someValue", "className": %s }
                """, classNameValue)
        )).isInstanceOf(JsonParseException.class)
                .hasMessage("The type id field `className` is not a String");

    }

    @Test
    void throwsIfClassNameIsNotRecognized() {
        assertThatThrownBy(() -> parseValue("""
                { "value": "someValue", "className": "not recognized" }
                """
        )).isInstanceOf(JsonParseException.class)
                .hasMessage("Cannot find type adapter for type id `not recognized`");
    }

    @Test
    void factoryLeavesJsonDataIntact() {
        var value = new BaseImpl1("value");
        JsonElement json = gson.toJsonTree(value, Base.class);

        var read1 = gson.fromJson(json, Base.class);
        var read2 = gson.fromJson(json, Base.class);

        assertThat(read1).isEqualTo(value);
        assertThat(read2).isEqualTo(value);
    }

    private Base roundTrip(Base value) {
        return parseValue(gson.toJson(value, Base.class));
    }

    private List<Base> roundTripContainer(List<? extends Base> value) {
        TypeToken<List<Base>> token = new TypeToken<>() {};
        return gson.fromJson(gson.toJson(value, token.getType()), token);
    }

    private Base parseValue(String json) {
        return gson.fromJson(json, Base.class);
    }

    private <T> T roundTripAs(T value, Class<T> cls) {
        return gson.fromJson(gson.toJson(value), cls);
    }

    interface Base {
        String getValue();
    }

    abstract static class Impl implements Base {
        private final String value;

        public Impl(String value) {
            this.value = value;
        }

        @Override
        public final String getValue() {
            return value;
        }

        @Override
        @SuppressWarnings("EqualsGetClass")
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Impl impl = (Impl) o;
            return Objects.equals(getValue(), impl.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }
    }

    static class BaseImpl1 extends Impl {
        public BaseImpl1(String value) {
            super(value);
        }
    }

    static class BaseImpl2 extends Impl {
        public BaseImpl2(String value) {
            super(value);
        }
    }

    static class Unregistered extends Impl {
        public Unregistered(String value) {
            super(value);
        }
    }

    static class Incompatible implements Base {
        String className = "incompatible";

        @Override
        public String getValue() {
            return className;
        }
    }
}
