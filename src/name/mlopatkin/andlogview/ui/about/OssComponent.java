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

import java.net.URI;

class OssComponent implements Comparable<OssComponent> {
    private final int id;
    private final String name;
    private final String version;
    private final URI homepage;

    private final String license;
    private final String licenseText;

    public OssComponent(int id, String name, String version, URI homepage, String license, String licenseText) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.homepage = homepage;
        this.license = license;
        this.licenseText = licenseText;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public URI getHomepage() {
        return homepage;
    }

    public String getLicense() {
        return license;
    }

    public String getLicenseText() {
        return licenseText;
    }

    @Override
    public int compareTo(OssComponent o) {
        // Name-based ordering.
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return String.format("OssComponent(id=%d, name=%s version=%s)", id, name, version);
    }
}
