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

import name.mlopatkin.andlogview.sdkrepo.SdkPackage.TargetOs;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import org.jspecify.annotations.Nullable;

import java.net.URI;

class SdkPackageBuilder {
    private final URI baseUri;
    private final String packageName;
    private @Nullable HashFunction checksumAlgorithm;
    private @Nullable HashCode checksum;
    private @Nullable URI uri;
    private @Nullable TargetOs hostOs;
    private @Nullable String license;

    public SdkPackageBuilder(URI baseUri, String packageName) {
        this.baseUri = baseUri;
        this.packageName = packageName;
    }

    public SdkPackageBuilder setChecksumType(HashFunction checksumAlgorithm) throws ManifestParseException {
        var checksum = this.checksum;
        if (checksum != null && checksum.bits() != checksumAlgorithm.bits()) {
            throw new ManifestParseException(
                    "Checksum " + checksum + " doesn't match checksum algorithm " + checksumAlgorithm);
        }
        this.checksumAlgorithm = checksumAlgorithm;
        return this;
    }

    public SdkPackageBuilder setChecksum(CharSequence checksumHex) throws ManifestParseException {
        try {
            var checksum = HashCode.fromString(checksumHex.toString());
            var checksumAlgorithm = this.checksumAlgorithm;
            if (checksumAlgorithm != null && checksum.bits() != checksumAlgorithm.bits()) {
                throw new ManifestParseException(
                        "Checksum " + checksum + " doesn't match checksum algorithm " + checksumAlgorithm);
            }
            this.checksum = checksum;
        } catch (IllegalArgumentException e) {
            throw new ManifestParseException("Failed to parse checksum string " + checksumHex, e);
        }
        return this;
    }

    public SdkPackageBuilder setUrl(CharSequence relativeUrl) throws ManifestParseException {
        try {
            this.uri = baseUri.resolve(relativeUrl.toString());
        } catch (IllegalArgumentException e) {
            throw new ManifestParseException("Failed to resolve URL " + relativeUrl, e);
        }
        return this;
    }

    public SdkPackageBuilder setHostOs(TargetOs hostOs) {
        this.hostOs = hostOs;
        return this;
    }

    public SdkPackageBuilder setLicense(String license) {
        this.license = license;
        return this;
    }

    public SdkPackage build() throws ManifestParseException {
        return new SdkPackage(
                packageName,
                ensureNonNull(license, "License must be present"),
                ensureNonNull(uri, "Download URL must be present"),
                ensureNonNull(hostOs, "Host OS must be present"),
                ensureNonNull(checksumAlgorithm, "Checksum algorithm must be present"),
                ensureNonNull(checksum, "Checksum must be present")
        );
    }

    private <T> T ensureNonNull(@Nullable T value, String onFailure) throws ManifestParseException {
        if (value == null) {
            throw new ManifestParseException(onFailure);
        }
        return value;
    }
}
