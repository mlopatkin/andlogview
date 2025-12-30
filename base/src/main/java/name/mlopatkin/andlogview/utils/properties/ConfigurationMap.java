/*
 * Copyright 2011 Mikhail Lopatkin
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

package name.mlopatkin.andlogview.utils.properties;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Class to work with properties files in more type-safe manner.
 */
@SuppressWarnings("TypeParameterUnusedInFormals")
public class ConfigurationMap implements Configuration {
    private final Map<String, Property<?>> properties = new LinkedHashMap<>();

    public <T extends @Nullable Object> ConfigurationMap property(String key, IPropertyBuilder<T> builder) {
        properties.put(key, builder.build());
        return this;
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Returns the value of the property.
     *
     * @param key the name of the property
     * @return the value of the property or null if there's nothing
     */
    @Override
    @SuppressWarnings({"NullAway", "DataFlowIssue"})  // Properties are too hard. Some are nullable and some aren't.
    public <T> T get(String key) {
        Property<T> p = getPropertyOrThrow(key);
        return p.getValue();
    }

    /**
     * Sets the new value to the mutable property.
     *
     * @param key the name of the property
     * @param value the new value of the property
     */
    @Override
    public <T> void set(String key, T value) {
        Property<T> p = getPropertyOrThrow(key);
        p.setValue(value);
    }

    @Override
    public void clear(String key) {
        Property<?> p = getPropertyOrThrow(key);
        p.setValue(null);
    }

    private <T> Property<T> getPropertyOrThrow(String key) {
        @SuppressWarnings("unchecked")
        Property<T> result = (Property<T>) properties.get(key);
        if (result == null) {
            throw new IllegalArgumentException("No such property: " + key);
        }
        return result;
    }

    /**
     * Set properties from {@link Properties} object.
     *
     * @param props the Properties to set these from
     */
    void assign(Properties props) {
        for (Map.Entry<String, Property<?>> propertyEntry : properties.entrySet()) {
            String key = propertyEntry.getKey();
            Property<?> property = propertyEntry.getValue();

            property.assign(key, props);
        }
    }

    private <K extends Enum<K>, V> EnumMapProperty<K, V> getEnumProperty(String key) {
        Property<?> property = getPropertyOrThrow(key);
        @SuppressWarnings("unchecked")
        EnumMapProperty<K, V> enumProperty = (EnumMapProperty<K, V>) property;
        return enumProperty;
    }

    @Override
    public <K extends Enum<K>, V> V get(String key, K enumKey) {
        EnumMapProperty<K, V> enumProperty = getEnumProperty(key);
        return enumProperty.getValue(enumKey);
    }

    @Override
    public <K extends Enum<K>, V> void set(String key, K enumKey, V value) {
        EnumMapProperty<K, V> enumProperty = getEnumProperty(key);
        enumProperty.setValue(enumKey, value);
    }

    @Override
    public void save(OutputStream output, String comments) throws IOException {
        Properties outputProperties = new Properties();

        for (Map.Entry<String, Property<?>> propertyEntry : properties.entrySet()) {
            String key = propertyEntry.getKey();
            Property<?> property = propertyEntry.getValue();

            property.write(key, outputProperties);
        }
        outputProperties.store(output, comments);
    }

    @Override
    public void load(InputStream input) throws IOException, IllegalConfigurationException {
        Properties props = PropertyUtils.getPropertiesFromStream(input);
        for (Map.Entry<String, Property<?>> propertyEntry : properties.entrySet()) {
            String key = propertyEntry.getKey();
            Property<?> property = propertyEntry.getValue();
            if (!property.isReadOnly()) {
                try {
                    property.assign(key, props);
                } catch (Exception e) { // OK to catch Exception here
                    throw new IllegalConfigurationException("Cannot read property: " + key, e);
                }
            }
        }
    }
}
