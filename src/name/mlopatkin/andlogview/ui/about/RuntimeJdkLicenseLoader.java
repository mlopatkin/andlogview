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

import static java.util.stream.Collectors.toMap;

import name.mlopatkin.andlogview.utils.Try;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

class RuntimeJdkLicenseLoader {
    private static final Logger log = LoggerFactory.getLogger(RuntimeJdkLicenseLoader.class);

    private final File legalDir;

    public RuntimeJdkLicenseLoader(File javaHome) {
        legalDir = new File(javaHome, "legal");
    }

    public OssComponent getBundledJdk() {
        var license = getBundledJdkLicense()
                .handleError(th -> log.error("Failed to load OpenJDK license", th))
                .toOptional()
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
                    .map(RuntimeJdkLicenseLoader::collectLicenseMap)
                    .map(RuntimeJdkLicenseLoader::buildLicenseText);
        } catch (IOException ex) {
            return Try.ofError(ex);
        }
    }

    private static SortedMap<String, String> collectLicenseMap(List<Map.Entry<String, String>> entries) {
        // We cannot use ImmutableMap, because it doesn't allow duplicates.
        return entries.stream().collect(
                toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> {
                            if (Objects.equals(v1, v2)) {
                                return v1;
                            } else {
                                throw new IllegalArgumentException("Duplicate entry with different contents found");
                            }
                        },
                        TreeMap::new));
    }

    private static Boolean isValidLicenseOrFailure(Try<Map.Entry<String, String>> t) {
        // We want to preserve the failures to e.g. log them. We don't want to collect incomplete license because of
        // that.
        // On at least Windows, some LICENSE files are just pointers to another one in java.base. We exclude these too.
        return !t.isPresent() || !t.get().getValue().startsWith("Please see ..");
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

    private static String buildLicenseText(SortedMap<String, String> licenseFiles) {
        var result = new StringBuilder();
        result.append(Objects.requireNonNull(
                        licenseFiles.remove("LICENSE"), "LICENSE not found"))
                .append('\n');
        result.append(Objects.requireNonNull(
                        licenseFiles.remove("ASSEMBLY_EXCEPTION"), "ASSEMBLY_EXCEPTION not found"))
                .append('\n');
        result.append(Objects.requireNonNull(
                        licenseFiles.remove("ADDITIONAL_LICENSE_INFO"), "ADDITIONAL_LICENSE_INFO not found"))
                .append('\n');

        for (String license : licenseFiles.values()) {
            result.append(license).append('\n');
        }

        return result.toString();
    }

    private static String readText(Path p) throws IOException {
        return Files.readString(p, StandardCharsets.UTF_8);
    }
}
