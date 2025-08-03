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

package name.mlopatkin.andlogview.sdkrepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import name.mlopatkin.andlogview.base.AppResources;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

class SdkRepoManifestParserTest {
    @Test
    void canParseGoldenRepositoryFile() throws Exception {
        // Tries to parse the manifest served by the Android SDK update site.
        // Run `./gradlew updateAndroidSdkRepoManifestForTests` to update it to the latest version (requires curl).
        try (var parser = new SdkRepoManifestParser(
                URI.create("http://example.org/"),
                AppResources.getResource("sdkrepo/repository2-3.xml"))
        ) {
            var packages =
                    parser.read("platform-tools").stream().collect(Collectors.groupingBy(SdkPackage::getTargetOs));

            assertThat(packages.keySet()).containsExactlyInAnyOrder(SdkPackage.TargetOs.LINUX,
                    SdkPackage.TargetOs.MAC_OS, SdkPackage.TargetOs.WINDOWS);

            assertThat(packages.get(SdkPackage.TargetOs.LINUX))
                    .singleElement()
                    .extracting(this::downloadUrl, InstanceOfAssertFactories.STRING)
                    .endsWith("-linux.zip");
            assertThat(packages.get(SdkPackage.TargetOs.MAC_OS))
                    .singleElement()
                    .extracting(this::downloadUrl, InstanceOfAssertFactories.STRING)
                    .endsWith("-darwin.zip");
            assertThat(packages.get(SdkPackage.TargetOs.WINDOWS))
                    .singleElement()
                    .extracting(this::downloadUrl, InstanceOfAssertFactories.STRING)
                    .endsWith("-win.zip");
        }
    }

    @Test
    void canParseEmptyDocument() throws Exception {
        var packages = parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                </sdk:sdk-repository>
                """);

        assertThat(packages).isEmpty();
    }

    @Test
    void canParseMinimalDocument() throws Exception {
        var packages = parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <uses-license ref="dummy-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk:sdk-repository>
                """);

        assertThat(packages)
                .singleElement(sdkPackageAssert())
                .hasOs(SdkPackage.TargetOs.LINUX)
                .hasName("platform-tools")
                .hasLicense("Dummy License")
                .hasChecksum("f6406982a79d67e40b1ca3cb9e5e2cc783c0f232")
                .hasDownloadUrlPath("/platform-tools_r35.0.2-linux.zip");
    }

    @Test
    void canParseMinimalDocumentWithOrderVariations() throws Exception {
        var packages = parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <archives>
                            <archive>
                                <host-os>linux</host-os>
                                <complete>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                </complete>
                            </archive>
                        </archives>
                        <uses-license ref="dummy-license"/>
                    </remotePackage>
                </sdk:sdk-repository>
                """);

        assertThat(packages)
                .singleElement(sdkPackageAssert())
                .hasOs(SdkPackage.TargetOs.LINUX)
                .hasName("platform-tools")
                .hasLicense("Dummy License")
                .hasChecksum("f6406982a79d67e40b1ca3cb9e5e2cc783c0f232")
                .hasDownloadUrlPath("/platform-tools_r35.0.2-linux.zip");
    }

    @Test
    void cannotHaveLicenseAfterPackages() throws Exception {
        assertThatThrownBy(() -> parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <remotePackage path='platform-tools'>
                        <uses-license ref="dummy-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                    <license id="dummy-license" type="text">Dummy License</license>
                </sdk:sdk-repository>
                """)
        ).isInstanceOf(ManifestParseException.class);
    }

    @Test
    void failsWhenLicenseIdIsBogus() throws Exception {
        assertThatThrownBy(() -> parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <uses-license ref="android-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk:sdk-repository>
                """)
        ).isInstanceOf(ManifestParseException.class);
    }

    @Test
    void failsWhenNoLicense() throws Exception {
        assertThatThrownBy(() -> parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <archives>
                            <archive>
                                <complete>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk:sdk-repository>
                """)
        ).isInstanceOf(ManifestParseException.class);
    }

    @Test
    void skipsPackagesWithoutUrl() throws Exception {
        var packages = parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <uses-license ref="dummy-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                            <archive>
                                <complete>
                                    <checksum type="sha1">6d204cdff21bce8a39c1d2367084e6174f854c2c</checksum>
                                    <url>platform-tools_r35.0.2-win.zip</url>
                                </complete>
                                <host-os>windows</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk:sdk-repository>
                """);
        assertThat(packages).singleElement(sdkPackageAssert()).hasOs(SdkPackage.TargetOs.WINDOWS);
    }

    @Test
    void skipsPackagesWithoutChecksum() throws Exception {
        var packages = parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <uses-license ref="dummy-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                            <archive>
                                <complete>
                                    <url>platform-tools_r35.0.2-win.zip</url>
                                </complete>
                                <host-os>windows</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk:sdk-repository>
                """);
        assertThat(packages).singleElement(sdkPackageAssert()).hasOs(SdkPackage.TargetOs.LINUX);
    }

    @Test
    void skipsPackagesWithInvalidChecksum() throws Exception {
        var packages = parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <uses-license ref="dummy-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                    <checksum type="sha1">f64069</checksum>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                            <archive>
                                <complete>
                                    <checksum type="sha1">6d204cdff21bce8a39c1d2367084e6174f854c2c</checksum>
                                    <url>platform-tools_r35.0.2-win.zip</url>
                                </complete>
                                <host-os>windows</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk:sdk-repository>
                """);
        assertThat(packages).singleElement(sdkPackageAssert()).hasOs(SdkPackage.TargetOs.WINDOWS);
    }

    @Test
    void abortsParsingIfNamespaceDifferent() {
        assertThatThrownBy(() -> parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/04">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <uses-license ref="dummy-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk:sdk-repository>
                """)
        ).isInstanceOf(ManifestParseException.class);
    }

    @Test
    void abortsParsingIfNoNamespace() {
        assertThatThrownBy(() -> parsePackages("platform-tools", """
                <sdk-repository>
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <uses-license ref="dummy-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk-repository>
                """)
        ).isInstanceOf(ManifestParseException.class);
    }

    @Test
    void packageWithinBrokenPackageIsNotParsed() throws Exception {
        var packages = parsePackages("platform-tools", """
                <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                    <license id="dummy-license" type="text">Dummy License</license>
                    <remotePackage path='platform-tools'>
                        <uses-license ref="dummy-license"/>
                        <archives>
                            <archive>
                                <complete>
                                    <url>platform-tools_r35.0.2-linux.zip</url>
                                    <checksum type="sha1">invalid</checksum>
                                    <archive>
                                        <complete>
                                            <url>platform-tools_r35.0.2-win.zip</url>
                                            <checksum type="sha1">f6406982a79d67e40b1ca3cb9e5e2cc783c0f232</checksum>
                                        </complete>
                                        <host-os>windows</host-os>
                                    </archive>
                                </complete>
                                <host-os>linux</host-os>
                            </archive>
                        </archives>
                    </remotePackage>
                </sdk:sdk-repository>
                """);
        assertThat(packages).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("canSelectPackageBasedOnChannelXmls")
    void canSelectPackageBasedOnChannel(String xml) throws Exception {
        var packages = parsePackages("platform-tools", xml);

        assertThat(packages).singleElement(sdkPackageAssert())
                .hasLicense("Android SDK License")
                .hasChecksum("69ffc978ad66667c6b2eb7979a09f5af20f83aaa");
    }

    private static String[] canSelectPackageBasedOnChannelXmls() {
        return new String[] {
                xml("""
                        <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                            <license id="android-sdk-license" type="text">Android SDK License</license>
                            <license id="android-sdk-preview-license" type="text">Preview License</license>
                            <remotePackage path="platform-tools">
                                <display-name>Android SDK Platform-Tools</display-name>
                                <uses-license ref="android-sdk-preview-license"/>
                                <channelRef ref="channel-2"/>
                                <archives>
                                    <archive>
                                        <complete>
                                            <checksum type="sha1">ddb0cd76d952d9a1f4c8a32e4ec0e73d7a8bebb8</checksum>
                                            <url>platform-tools_r36.0.0-linux.zip</url>
                                        </complete>
                                        <host-os>linux</host-os>
                                    </archive>
                                </archives>
                            </remotePackage>
                            <remotePackage path="platform-tools">
                                <display-name>Android SDK Platform-Tools</display-name>
                                <uses-license ref="android-sdk-license"/>
                                <channelRef ref="channel-0"/>
                                <archives>
                                    <archive>
                                        <complete>
                                            <checksum type="sha1">69ffc978ad66667c6b2eb7979a09f5af20f83aaa</checksum>
                                            <url>platform-tools_r36.0.0-linux.zip</url>
                                        </complete>
                                        <host-os>linux</host-os>
                                    </archive>
                                </archives>
                            </remotePackage>
                        </sdk:sdk-repository>"""),
                xml("""
                        <sdk:sdk-repository xmlns:sdk="http://schemas.android.com/sdk/android/repo/repository2/03">
                            <license id="android-sdk-preview-license" type="text">Preview License</license>
                            <license id="android-sdk-license" type="text">Android SDK License</license>
                            <remotePackage path="platform-tools">
                                <display-name>Android SDK Platform-Tools</display-name>
                                <uses-license ref="android-sdk-license"/>
                                <channelRef ref="channel-0"/>
                                <archives>
                                    <archive>
                                        <complete>
                                            <checksum type="sha1">69ffc978ad66667c6b2eb7979a09f5af20f83aaa</checksum>
                                            <url>platform-tools_r36.0.0-linux.zip</url>
                                        </complete>
                                        <host-os>linux</host-os>
                                    </archive>
                                </archives>
                            </remotePackage>
                            <remotePackage path="platform-tools">
                                <display-name>Android SDK Platform-Tools</display-name>
                                <uses-license ref="android-sdk-preview-license"/>
                                <channelRef ref="channel-2"/>
                                <archives>
                                    <archive>
                                        <complete>
                                            <checksum type="sha1">ddb0cd76d952d9a1f4c8a32e4ec0e73d7a8bebb8</checksum>
                                            <url>platform-tools_r36.0.0-linux.zip</url>
                                        </complete>
                                        <host-os>linux</host-os>
                                    </archive>
                                </archives>
                            </remotePackage>
                        </sdk:sdk-repository>""")
        };
    }

    private ByteSource document(String document) {
        return CharSource.wrap(document).asByteSource(StandardCharsets.UTF_8);
    }

    private SdkRepoManifestParser forDocument(String document) throws ManifestParseException {
        return new SdkRepoManifestParser(URI.create("http://example.org/"), document(document));
    }

    private List<SdkPackage> parsePackages(String packageName, @Language("XML") String document)
            throws ManifestParseException {
        try (var parser = forDocument(document)) {
            return parser.read(packageName);
        }
    }

    private String downloadUrl(SdkPackage p) {
        return p.getDownloadUrl().toASCIIString();
    }

    public static class SdkPackageAssert extends AbstractAssert<SdkPackageAssert, SdkPackage> {

        public SdkPackageAssert(SdkPackage actual) {
            super(actual, SdkPackageAssert.class);
        }

        public static SdkPackageAssert assertThat(SdkPackage actual) {
            return new SdkPackageAssert(actual);
        }

        public SdkPackageAssert hasName(String expectedName) {
            isNotNull();
            if (!actual.getName().equals(expectedName)) {
                failWithMessage("Expected name to be <%s> but was <%s>", expectedName, actual.getName());
            }
            return this;
        }

        public SdkPackageAssert hasLicense(String expectedLicense) {
            isNotNull();
            if (!actual.getLicense().equals(expectedLicense)) {
                failWithMessage("Expected license to be <%s> but was <%s>", expectedLicense, actual.getLicense());
            }
            return this;
        }

        public SdkPackageAssert hasDownloadUrlPath(String expectedPath) {
            isNotNull();
            String actualPath = actual.getDownloadUrl().getPath();
            if (!actualPath.equals(expectedPath)) {
                failWithMessage("Expected download URL path to be <%s> but was <%s>", expectedPath, actualPath);
            }
            return this;
        }

        public SdkPackageAssert hasOs(SdkPackage.TargetOs expectedOs) {
            isNotNull();
            if (actual.getTargetOs() != expectedOs) {
                failWithMessage("Expected target OS to be <%s> but was <%s>", expectedOs, actual.getTargetOs());
            }
            return this;
        }

        public SdkPackageAssert hasChecksum(String expectedChecksumHex) {
            isNotNull();
            String actualChecksum = actual.getPackageChecksum().toString();
            if (!actualChecksum.equalsIgnoreCase(expectedChecksumHex)) {
                failWithMessage("Expected checksum to be <%s> but was <%s>", expectedChecksumHex, actualChecksum);
            }
            return this;
        }
    }

    private InstanceOfAssertFactory<SdkPackage, SdkPackageAssert> sdkPackageAssert() {
        return new InstanceOfAssertFactory<>(SdkPackage.class, SdkPackageAssert::new);
    }

    private static String xml(@Language("XML") String xml) {
        return xml;
    }
}
