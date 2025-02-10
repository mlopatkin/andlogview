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

package name.mlopatkin.andlogview.ui.about;

import static com.google.common.collect.ImmutableList.toImmutableList;

import name.mlopatkin.andlogview.base.AppResources;
import name.mlopatkin.andlogview.utils.LazyInstance;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A model of our third-party dependencies.
 */
class OssComponents {
    private final LazyInstance<List<OssComponent>> components = LazyInstance.lazy(
            OssComponents::loadComponents);

    public OssComponent getComponentById(int id) {
        return components.get()
                .stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public List<OssComponent> getComponents() {
        return components.get();
    }

    private static List<OssComponent> loadComponents() {
        var components = loadComponentsFrom(AppResources.getResource("ui/about/licenses.json")).stream();
        var isBundledRuntime = System.getProperty("jpackage.app-version") != null;
        if (isBundledRuntime) {
            components = Stream.concat(
                    components,
                    Stream.of(new RuntimeJdkLicenseLoader(new File(System.getProperty("java.home"))).getBundledJdk())
            );
        }
        return components
                .sorted(Comparator.comparing(OssComponent::getName))
                .collect(toImmutableList());
    }

    private static List<OssComponent> loadComponentsFrom(ByteSource source) {
        var gson = new Gson();
        try {
            return gson.fromJson(
                    source.asCharSource(StandardCharsets.UTF_8).read(),
                    new TypeToken<ImmutableList<OssComponent>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
