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

import name.mlopatkin.andlogview.utils.Try;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

class RuntimeJdkLicenseLoader {
    private final File legalDir;

    public RuntimeJdkLicenseLoader(File javaHome) {
        legalDir = new File(javaHome, "legal");
    }

    public OssComponent getBundledJdk() {
        var license = getBundledJdkLicense().toOptional()
                .orElse("Failed to load OpenJDK license. You can find it in " + legalDir.getAbsolutePath());
        return buildJdkComponent(license);
    }

    private Try<String> getBundledJdkLicense() {
        if (!legalDir.isDirectory()) {
            return Try.ofError(new FileNotFoundException("Licenses directory " + legalDir + " is not found"));
        }

        try (var files = Files.walk(legalDir.toPath())) {
            return files.filter(Files::isRegularFile)
                    .map(p -> Try.ofCallable(() -> Maps.immutableEntry(p.toFile().getName(), readText(p))))
                    .filter(RuntimeJdkLicenseLoader::isValidLicenseOrFailure)
                    .collect(Try.liftToList())
                    .map(ImmutableMap::copyOf)
                    .map(RuntimeJdkLicenseLoader::buildLicenseText);
        } catch (IOException ex) {
            return Try.ofError(ex);
        }
    }

    private static Boolean isValidLicenseOrFailure(Try<Map.Entry<String, String>> t) {
        return t.toOptional().map(l -> !l.getValue().startsWith("Please see ..")).orElse(true);
    }

    private static OssComponent buildJdkComponent(String license) {
        return new OssComponent(
                Integer.MAX_VALUE,
                System.getProperty("java.vendor") + " OpenJDK",
                System.getProperty("java.runtime.version"),
                URI.create(System.getProperty("java.vendor.url")),
                "runtime/",
                "GPL v2 + Classpath",
                license
        );
    }

    private static String buildLicenseText(Map<String, String> licenseFiles) {
        var sortedLicenseFiles = new TreeMap<>(licenseFiles);

        var result = new StringBuilder();
        result.append(Objects.requireNonNull(
                        sortedLicenseFiles.remove("LICENSE"), "LICENSE not found"))
                .append('\n');
        result.append(Objects.requireNonNull(
                        sortedLicenseFiles.remove("ASSEMBLY_EXCEPTION"), "ASSEMBLY_EXCEPTION not found"))
                .append('\n');
        result.append(Objects.requireNonNull(
                        sortedLicenseFiles.remove("ADDITIONAL_LICENSE_INFO"), "ADDITIONAL_LICENSE_INFO not found"))
                .append('\n');

        for (String license : sortedLicenseFiles.values()) {
            result.append(license).append('\n');
        }

        return result.toString();
    }

    private static String readText(Path p) throws IOException {
        // Can't use Files.readText as it is Java 11+
        return com.google.common.io.Files.asByteSource(p.toFile()).asCharSource(StandardCharsets.UTF_8).read();
    }
}
