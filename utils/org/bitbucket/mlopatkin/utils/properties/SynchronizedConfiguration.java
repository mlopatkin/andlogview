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

package org.bitbucket.mlopatkin.utils.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SynchronizedConfiguration implements Configuration {

    private Configuration cfg;

    public SynchronizedConfiguration(Configuration cfg) {
        this.cfg = cfg;
    }

    @Override
    public synchronized boolean hasProperty(String name) {
        return cfg.hasProperty(name);
    }

    @Override
    public synchronized <T> T get(String key) {
        return cfg.get(key);
    }

    @Override
    public synchronized <K extends Enum<K>, V> V get(String key, K enumKey) {
        return cfg.get(key, enumKey);
    }

    @Override
    public synchronized <T> void set(String key, T value) {
        cfg.set(key, value);
    }

    @Override
    public synchronized <K extends Enum<K>, V> void set(String key, K enumKey, V value) {
        cfg.set(key, enumKey, value);
    }

    @Override
    public synchronized void save(OutputStream output, String comments) throws IOException {
        cfg.save(output, comments);
    }

    @Override
    public synchronized void load(InputStream input) throws IOException,
            IllegalConfigurationException {
        cfg.load(input);
    }

}
