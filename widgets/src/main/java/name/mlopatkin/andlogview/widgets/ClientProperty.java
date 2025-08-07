/*
 * Copyright 2022 the Andlogview authors
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

package name.mlopatkin.andlogview.widgets;

import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * Type-safe helper for {@link JComponent#putClientProperty(Object, Object)}. The instance of this class serves as a key
 * and provides type-safe accessors to set and retrieve the value. It is also possible to set up a global default value
 * in the {@link UIManager}.
 *
 * @param <V> the type of the value.
 */
public final class ClientProperty<V> {
    private final Class<V> cls;

    private ClientProperty(Class<V> cls) {
        this.cls = cls;
    }

    /**
     * Gets a client value stored in the component (if it is a JComponent) or the default value from the {@link
     * UIManager} if there is no value set on the component or the component is not a JComponent. Returns {@code null}
     * if neither value is set.
     *
     * @param c the component to get value from
     * @return the value stored in the component or the default value from the UIManager or {@code null}
     */
    public @Nullable V get(Component c) {
        V result = getFromComponent(c);
        if (result != null) {
            return result;
        }
        return getDefault();
    }

    /**
     * Gets a client value stored in the component (if it is a JComponent) or the default value from the {@link
     * UIManager} if there is no value set on the component or the component is not a JComponent. Returns
     * {@code fallbackValue} if neither value is set.
     *
     * @param c the component to get value from
     * @param fallbackValue the value to return if nothing is set on the component and in the UIManager
     * @return the value stored in the component or the default value from the UIManager or {@code fallbackValue}
     * @see #getWithUpcast(ClientProperty, Component, Object)
     */
    public V getOrElse(Component c, V fallbackValue) {
        @Nullable V value = get(c);
        return value != null ? value : fallbackValue;
    }

    /**
     * Returns the default value of this property set in the {@link UIManager}.
     *
     * @return the default value or {@code null}
     */
    public @Nullable V getDefault() {
        return tryDowncast(UIManager.get(this));
    }

    /**
     * Gets a client value stored in the component (if it is a JComponent) or the default value from the {@link
     * UIManager} if there is no value set on the component or the component is not a JComponent. Returns
     * {@code fallbackValue} if neither value is set. Unlike {@link #getOrElse(Component, Object)} this method allows to
     * use a subtype of the value as a fallback value.
     *
     * @param property the property to get the value of
     * @param c the component to get value from
     * @param fallbackValue the value to return if nothing is set on the component and in the UIManager
     * @return the value stored in the component or the default value from the UIManager or {@code fallbackValue}
     * @see #getOrElse(Component, Object)
     */
    public static <T> T getWithUpcast(ClientProperty<? extends T> property, Component c, T fallbackValue) {
        return ClientProperty.<T>upcastForRead(property).getOrElse(c, fallbackValue);
    }

    @SuppressWarnings("unchecked")
    private static <T> ClientProperty<T> upcastForRead(ClientProperty<? extends T> property) {
        // This isn't entirely safe conversion but for reading it is okay.
        return (ClientProperty<T>) property;
    }

    private @Nullable V tryDowncast(@Nullable Object obj) {
        if (cls.isInstance(obj)) {
            return cls.cast(obj);
        }
        return null;
    }

    private @Nullable V getFromComponent(Component c) {
        if (c instanceof JComponent) {
            @Nullable Object value = ((JComponent) c).getClientProperty(this);
            return tryDowncast(value);
        }
        return null;
    }


    /**
     * Sets the value of this property on the component.
     *
     * @param c the component to set the value on
     * @param value the value to set
     */
    public void put(JComponent c, V value) {
        c.putClientProperty(this, value);
    }

    /**
     * Sets the default value of this property in the {@link UIManager}.
     *
     * @param value the value to set as a default
     */
    public void setDefault(V value) {
        UIManager.put(this, value);
    }

    /**
     * Creates a property instance to hold values of class {@code V}.
     *
     * @param cls the class object for {@code V}
     * @param <V> the type of the value
     * @return the property instance
     */
    public static <V> ClientProperty<V> create(Class<V> cls) {
        return new ClientProperty<>(cls);
    }

    /**
     * Creates a property instance to hold colors.
     *
     * @return the property instance
     */

    public static ClientProperty<Color> colorProperty() {
        return create(Color.class);
    }

    /**
     * Creates a property instance to hold floats.
     *
     * @return the property instance
     */
    public static ClientProperty<Float> floatProperty() {
        return create(Float.class);
    }

    /**
     * Creates a property instance to hold integers.
     *
     * @return the property instance
     */
    public static ClientProperty<Integer> intProperty() {
        return create(Integer.class);
    }
}
